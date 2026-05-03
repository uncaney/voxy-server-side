package dev.xantha.vss.common.processing;

import dev.xantha.vss.common.PositionUtil;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.AbstractPlayerRequestState;
import dev.xantha.vss.common.processing.OffThreadProcessor;
import dev.xantha.vss.common.processing.PlayerStateAccess;
import dev.xantha.vss.common.processing.SendAction;
import dev.xantha.vss.common.processing.TickSnapshot;
import dev.xantha.vss.common.voxel.ColumnTimestampCache;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;

/* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/IncomingRequestRouter.class */
class IncomingRequestRouter<PS extends PlayerStateAccess> {
    private final ColumnTimestampCache timestampCache;
    private final DedupTracker dedupTracker;
    private final boolean diskReadingAvailable;
    private final boolean generationAvailable;
    private final ProcessingContext ctx;

    /* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/IncomingRequestRouter$DiskReadSubmitter.class */
    @FunctionalInterface
    interface DiskReadSubmitter {
        void submit(UUID uuid, int i, String str, int i2, int i3, long j);
    }

    /* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/IncomingRequestRouter$LoadedColumnSerializer.class */
    @FunctionalInterface
    interface LoadedColumnSerializer<PS> {
        boolean serializeAndEnqueue(PS ps, LoadedColumnData loadedColumnData, int i, long j, long j2, String str);
    }

    IncomingRequestRouter(ColumnTimestampCache timestampCache, DedupTracker dedupTracker, boolean diskReadingAvailable, boolean generationAvailable, ProcessingContext ctx) {
        this.timestampCache = timestampCache;
        this.dedupTracker = dedupTracker;
        this.diskReadingAvailable = diskReadingAvailable;
        this.generationAvailable = generationAvailable;
        this.ctx = ctx;
    }

    void routeAll(TickSnapshot snapshot, Map<UUID, PS> players, DiskReadSubmitter diskReadSubmitter, LoadedColumnSerializer<PS> loadedSerializer, long cycleNow) {
        PS state;
        for (Map.Entry<UUID, TickSnapshot.PlayerTickData> entry : snapshot.players().entrySet()) {
            if (!entry.getValue().dimensionChanged() && (state = players.get(entry.getKey())) != null && state.supportsVoxelColumns()) {
                processIncomingRequests(state, entry.getKey(), entry.getValue(), snapshot, diskReadSubmitter, loadedSerializer, cycleNow);
            }
        }
    }

    private void processIncomingRequests(PS state, UUID playerUuid, TickSnapshot.PlayerTickData playerData, TickSnapshot snapshot, DiskReadSubmitter diskReadSubmitter, LoadedColumnSerializer<PS> loadedSerializer, long cycleNow) {
        String dimension = playerData.dimension();
        Long2ObjectMap<LoadedColumnData> loadedProbes = snapshot.loadedChunkProbes().getOrDefault(playerUuid, Long2ObjectMaps.emptyMap());
        drainWaitingQueue(state, playerUuid, dimension, loadedProbes, snapshot, diskReadSubmitter, loadedSerializer, cycleNow);
        while (true) {
            IncomingRequest req = state.pollIncomingRequest();
            if (req != null) {
                if (!resolvedAsDuplicate(state, playerUuid, req)) {
                    if (!sendQueueFull(state, snapshot)) {
                        long packed = PositionUtil.packPosition(req.cx(), req.cz());
                        if (!resolvedFromTimestamp(state, playerUuid, req, packed, dimension) && !resolvedFromLoadedProbe(state, playerUuid, req, packed, loadedProbes, dimension, loadedSerializer, cycleNow)) {
                            RequestType type = req.clientTimestamp() == 0 ? RequestType.GENERATION : RequestType.SYNC;
                            ConcurrencyLimiter limiter = tryConcurrencyOrEnqueue(state, playerUuid, req, type, dimension);
                            if (limiter != null) {
                                submitToDiskOrGeneration(state, playerUuid, req, packed, dimension, type, limiter, diskReadSubmitter);
                            }
                        }
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }
        }
    }

    private void drainWaitingQueue(PS state, UUID playerUuid, String dimension, Long2ObjectMap<LoadedColumnData> loadedProbes, TickSnapshot snapshot, DiskReadSubmitter diskReadSubmitter, LoadedColumnSerializer<PS> loadedSerializer, long cycleNow) {
        ArrayDeque<AbstractPlayerRequestState.QueuedRequest> queue = state.getWaitingQueue();
        while (!queue.isEmpty()) {
            AbstractPlayerRequestState.QueuedRequest queued = queue.peek();
            IncomingRequest req = queued.request();
            long packed = PositionUtil.packPosition(req.cx(), req.cz());
            if (state.hasDiskReadDone(req.cx(), req.cz())) {
                queue.poll();
                this.ctx.sendActions().add(new SendAction.ColumnUpToDate(playerUuid, req.requestId()));
            } else if (resolvedFromTimestamp(state, playerUuid, req, packed, dimension)) {
                queue.poll();
            } else if (resolvedFromLoadedProbe(state, playerUuid, req, packed, loadedProbes, dimension, loadedSerializer, cycleNow)) {
                queue.poll();
            } else {
                ConcurrencyLimiter limiter = state.getRateLimiters().forRequest(queued.type(), this.diskReadingAvailable);
                if (limiter.tryAcquire()) {
                    queue.poll();
                    submitToDiskOrGeneration(state, playerUuid, req, packed, dimension, queued.type(), limiter, diskReadSubmitter);
                } else {
                    return;
                }
            }
        }
    }

    private boolean resolvedAsDuplicate(PS state, UUID playerUuid, IncomingRequest req) {
        if (state.hasDiskReadDone(req.cx(), req.cz())) {
            this.ctx.sendActions().add(new SendAction.ColumnUpToDate(playerUuid, req.requestId()));
            return true;
        }
        if (state.hasPendingRequest(req.cx(), req.cz())) {
            this.ctx.diagnostics().incrementSkippedDuplicate();
            return true;
        }
        return false;
    }

    private boolean sendQueueFull(PS state, TickSnapshot snapshot) {
        if (snapshot.maxSendQueueSize() > 0 && state.getSendQueueSize() >= snapshot.maxSendQueueSize()) {
            this.ctx.diagnostics().incrementQueueFull();
            return true;
        }
        return false;
    }

    private ConcurrencyLimiter tryConcurrencyOrEnqueue(PS state, UUID playerUuid, IncomingRequest req, RequestType type, String dimension) {
        RateLimiterSet limiters = state.getRateLimiters();
        ConcurrencyLimiter limiter = limiters.forRequest(type, this.diskReadingAvailable);
        if (limiter.tryAcquire()) {
            return limiter;
        }
        int queueCap = limiters.syncRateLimit() + limiters.genRateLimit();
        if (state.getWaitingQueueSize() < queueCap) {
            state.getWaitingQueue().add(new AbstractPlayerRequestState.QueuedRequest(req, type, dimension));
            this.ctx.diagnostics().incrementQueued();
            return null;
        }
        this.ctx.sendActions().add(new SendAction.RateLimited(playerUuid, req.requestId()));
        this.ctx.diagnostics().incrementRateLimited(type);
        if (VSSLogger.isDebugEnabled()) {
            VSSLogger.debug("Rate-limited " + String.valueOf(playerUuid) + " (" + String.valueOf(type) + "): queue full at " + queueCap + " for chunk [" + req.cx() + ", " + req.cz() + "] in " + dimension);
            return null;
        }
        return null;
    }

    private boolean resolvedFromLoadedProbe(PS state, UUID playerUuid, IncomingRequest req, long packed, Long2ObjectMap<LoadedColumnData> probes, String dimension, LoadedColumnSerializer<PS> loadedSerializer, long cycleNow) {
        LoadedColumnData probe = (LoadedColumnData) probes.get(packed);
        if (probe == null) {
            return false;
        }
        boolean sent = loadedSerializer.serializeAndEnqueue(state, probe, req.requestId(), cycleNow, this.ctx.sequence().next(), dimension);
        if (!sent) {
            this.ctx.sendActions().add(new SendAction.ColumnUpToDate(playerUuid, req.requestId()));
        }
        state.markDiskReadDone(req.cx(), req.cz());
        this.ctx.diagnostics().incrementInMemory();
        return true;
    }

    private boolean resolvedFromTimestamp(PS state, UUID playerUuid, IncomingRequest req, long packed, String dimension) {
        if (req.clientTimestamp() <= 0) {
            return false;
        }
        long cachedTs = this.timestampCache.get(dimension, packed);
        if (cachedTs > 0 && cachedTs <= req.clientTimestamp()) {
            state.markDiskReadDone(req.cx(), req.cz());
            this.ctx.sendActions().add(new SendAction.ColumnUpToDate(playerUuid, req.requestId()));
            this.ctx.diagnostics().incrementUpToDate();
            return true;
        }
        return false;
    }

    private void submitToDiskOrGeneration(PS state, UUID playerUuid, IncomingRequest req, long packed, String dimension, RequestType type, ConcurrencyLimiter limiter, DiskReadSubmitter diskReadSubmitter) {
        long order = this.ctx.sequence().next();
        if (type == RequestType.SYNC || this.diskReadingAvailable) {
            state.addPendingRequest(new PendingRequest(req.requestId(), req.cx(), req.cz(), type));
            boolean attached = this.dedupTracker.tryAttachOrCreate(packed, dimension, playerUuid, req.requestId(), order);
            if (!attached) {
                diskReadSubmitter.submit(playerUuid, req.requestId(), dimension, req.cx(), req.cz(), order);
            }
            this.ctx.diagnostics().incrementDiskQueued();
            return;
        }
        if (type == RequestType.GENERATION && this.generationAvailable) {
            state.addPendingRequest(new PendingRequest(req.requestId(), req.cx(), req.cz(), type));
            this.ctx.generationTicketRequests().add(new OffThreadProcessor.GenerationTicketRequest(playerUuid, req.requestId(), req.cx(), req.cz(), order));
        } else {
            this.ctx.sendActions().add(new SendAction.ColumnNotGenerated(playerUuid, req.requestId()));
            limiter.release();
        }
    }
}

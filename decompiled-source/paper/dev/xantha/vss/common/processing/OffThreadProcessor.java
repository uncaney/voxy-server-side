package dev.xantha.vss.common.processing;

import dev.xantha.vss.common.PositionUtil;
import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.DedupTracker;
import dev.xantha.vss.common.processing.PlayerStateAccess;
import dev.xantha.vss.common.processing.ReadResultAccess;
import dev.xantha.vss.common.processing.SendAction;
import dev.xantha.vss.common.processing.TickSnapshot;
import dev.xantha.vss.common.voxel.ColumnTimestampCache;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/OffThreadProcessor.class */
public abstract class OffThreadProcessor<PlayerState extends PlayerStateAccess, ReadResult extends ReadResultAccess> {
    private static final int SNAPSHOT_POLL_MS = 50;
    private static final int SHUTDOWN_JOIN_MS = 5000;
    private static final int EVICTION_INTERVAL_CYCLES = 1200;
    private static final int SAVE_INTERVAL_CYCLES = 6000;
    private TickSnapshot pendingSnapshot;
    private final Thread processingThread;
    private final ColumnTimestampCache timestampCache;
    private final Map<UUID, PlayerState> players;
    private final boolean diskReadingAvailable;
    private final boolean generationAvailable;
    private final ProcessingContext ctx;
    private final IncomingRequestRouter<PlayerState> requestRouter;
    private final Path dataDir;
    private int evictionCounter;
    private int saveCounter;
    private int consecutiveErrors;
    private long cycleNow;
    private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "VSS-TimestampSave");
        t.setDaemon(true);
        return t;
    });
    private final Object snapshotLock = new Object();
    private final ConcurrentLinkedQueue<SendAction> sendActions = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<GenerationTicketRequest> generationTicketRequests = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<TimestampInvalidation> timestampInvalidations = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<TickSnapshot.GenerationReadyData> droppedGenerationReady = new ConcurrentLinkedQueue<>();
    private final DedupTracker dedupTracker = new DedupTracker();

    protected abstract ReadResult pollDiskResult(PlayerState playerstate);

    protected abstract ReadResult pollGenerationResult(PlayerState playerstate);

    protected abstract void enqueueResultPayloads(PlayerState playerstate, ReadResult readresult);

    protected abstract void submitDiskRead(UUID uuid, int i, String str, int i2, int i3, long j);

    protected abstract void buildAndEnqueueColumnPayload(PlayerState playerstate, int i, int i2, String str, int i3, long j, long j2, byte[] bArr, int i4);

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest.class */
    public static final class GenerationTicketRequest extends Record {
        private final UUID playerUuid;
        private final int requestId;
        private final int cx;
        private final int cz;
        private final long submissionOrder;

        public GenerationTicketRequest(UUID playerUuid, int requestId, int cx, int cz, long submissionOrder) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
            this.cx = cx;
            this.cz = cz;
            this.submissionOrder = submissionOrder;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, GenerationTicketRequest.class), GenerationTicketRequest.class, "playerUuid;requestId;cx;cz;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, GenerationTicketRequest.class), GenerationTicketRequest.class, "playerUuid;requestId;cx;cz;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, GenerationTicketRequest.class, Object.class), GenerationTicketRequest.class, "playerUuid;requestId;cx;cz;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$GenerationTicketRequest;->submissionOrder:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public UUID playerUuid() {
            return this.playerUuid;
        }

        public int requestId() {
            return this.requestId;
        }

        public int cx() {
            return this.cx;
        }

        public int cz() {
            return this.cz;
        }

        public long submissionOrder() {
            return this.submissionOrder;
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/OffThreadProcessor$TimestampInvalidation.class */
    private static final class TimestampInvalidation extends Record {
        private final String dimension;
        private final long[] positions;

        private TimestampInvalidation(String dimension, long[] positions) {
            this.dimension = dimension;
            this.positions = positions;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, TimestampInvalidation.class), TimestampInvalidation.class, "dimension;positions", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$TimestampInvalidation;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$TimestampInvalidation;->positions:[J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, TimestampInvalidation.class), TimestampInvalidation.class, "dimension;positions", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$TimestampInvalidation;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$TimestampInvalidation;->positions:[J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, TimestampInvalidation.class, Object.class), TimestampInvalidation.class, "dimension;positions", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$TimestampInvalidation;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/OffThreadProcessor$TimestampInvalidation;->positions:[J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public String dimension() {
            return this.dimension;
        }

        public long[] positions() {
            return this.positions;
        }
    }

    protected OffThreadProcessor(Map<UUID, PlayerState> players, boolean diskReadingAvailable, boolean generationAvailable, Path dataDir, int perDimensionTimestampCacheSizeMB) {
        this.players = players;
        this.diskReadingAvailable = diskReadingAvailable;
        this.generationAvailable = generationAvailable;
        this.dataDir = dataDir;
        this.timestampCache = new ColumnTimestampCache(ColumnTimestampCache.mbToEntries(perDimensionTimestampCacheSizeMB));
        if (dataDir != null) {
            this.timestampCache.load(dataDir);
        }
        this.ctx = new ProcessingContext(this.sendActions, this.generationTicketRequests, new ProcessingDiagnostics(), new SequenceCounter());
        this.requestRouter = new IncomingRequestRouter<>(this.timestampCache, this.dedupTracker, diskReadingAvailable, generationAvailable, this.ctx);
        this.processingThread = new Thread(this::processingLoop, "VSS Processing Thread");
        this.processingThread.setDaemon(true);
        this.processingThread.setPriority(4);
    }

    public void start() {
        this.processingThread.start();
    }

    public void postSnapshot(TickSnapshot snapshot) {
        synchronized (this.snapshotLock) {
            if (this.pendingSnapshot != null) {
                for (TickSnapshot.GenerationReadyData genReady : this.pendingSnapshot.generationReady()) {
                    this.droppedGenerationReady.add(genReady);
                }
            }
            this.pendingSnapshot = snapshot;
            this.snapshotLock.notifyAll();
        }
    }

    public SendAction pollSendAction() {
        return this.sendActions.poll();
    }

    public GenerationTicketRequest pollGenerationTicketRequest() {
        return this.generationTicketRequests.poll();
    }

    public void invalidateTimestamps(String dimension, long[] positions) {
        this.timestampInvalidations.add(new TimestampInvalidation(dimension, positions));
    }

    protected boolean compressAndEnqueueLoaded(PlayerState state, LoadedColumnData column, int requestId, long columnTimestamp, long submissionOrder, String dimension) {
        if (column.serializedSections() == null || column.serializedSections().length == 0) {
            return false;
        }
        int estimatedBytes = column.serializedSections().length + 25;
        long packed = PositionUtil.packPosition(column.cx(), column.cz());
        this.timestampCache.put(dimension, packed, columnTimestamp, this.cycleNow);
        buildAndEnqueueColumnPayload(state, column.cx(), column.cz(), dimension, requestId, columnTimestamp, submissionOrder, column.serializedSections(), estimatedBytes);
        return true;
    }

    private void processingLoop() {
        TickSnapshot snapshot;
        while (true) {
            synchronized (this.snapshotLock) {
                while (this.pendingSnapshot == null) {
                    try {
                        this.snapshotLock.wait(50L);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                snapshot = this.pendingSnapshot;
                this.pendingSnapshot = null;
            }
            if (snapshot.shutdown()) {
                return;
            }
            try {
                processCycle(snapshot);
                this.consecutiveErrors = 0;
            } catch (Exception e2) {
                VSSLogger.error("Error in processing cycle", e2);
                int i = this.consecutiveErrors + 1;
                this.consecutiveErrors = i;
                if (i >= 10) {
                    VSSLogger.error("Processing thread hit " + this.consecutiveErrors + " consecutive errors, backing off");
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e3) {
                        return;
                    }
                } else {
                    continue;
                }
            }
        }
    }

    private void processCycle(TickSnapshot snapshot) {
        this.cycleNow = VSSConstants.epochSeconds();
        this.ctx.diagnostics().resetTickCounters();
        releaseDroppedGenerationSlots();
        preparePlayers(snapshot);
        drainDiskResultsForAllPlayers(snapshot);
        drainGenerationResultsForAllPlayers(snapshot);
        processGenerationReady(snapshot);
        routeIncomingRequests(snapshot);
        int i = this.evictionCounter + 1;
        this.evictionCounter = i;
        if (i >= EVICTION_INTERVAL_CYCLES) {
            this.evictionCounter = 0;
            int evicted = this.timestampCache.evictIfOversized();
            if (evicted > 0 && VSSLogger.isDebugEnabled()) {
                VSSLogger.debug("Evicted " + evicted + " oversized timestamp cache entries (" + this.timestampCache.size() + " remaining)");
            }
        }
        if (this.dataDir != null) {
            int i2 = this.saveCounter + 1;
            this.saveCounter = i2;
            if (i2 >= SAVE_INTERVAL_CYCLES) {
                this.saveCounter = 0;
                ColumnTimestampCache cacheSnapshot = this.timestampCache.snapshotForSave();
                SAVE_EXECUTOR.execute(() -> {
                    cacheSnapshot.save(this.dataDir);
                });
            }
        }
    }

    private void releaseDroppedGenerationSlots() {
        while (true) {
            TickSnapshot.GenerationReadyData genReady = this.droppedGenerationReady.poll();
            if (genReady != null) {
                PlayerState state = this.players.get(genReady.playerUuid());
                if (state != null) {
                    state.removePendingByPosition(genReady.columnData().cx(), genReady.columnData().cz());
                    state.getRateLimiters().generation().release();
                }
            } else {
                return;
            }
        }
    }

    private void preparePlayers(TickSnapshot snapshot) {
        while (true) {
            TimestampInvalidation inv = this.timestampInvalidations.poll();
            if (inv == null) {
                break;
            } else {
                this.timestampCache.invalidate(inv.dimension(), inv.positions());
            }
        }
        for (UUID removedUuid : snapshot.removedPlayers()) {
            cleanupDedupGroups(this.dedupTracker.removePlayer(removedUuid));
        }
        for (Map.Entry<UUID, TickSnapshot.PlayerTickData> entry : snapshot.players().entrySet()) {
            PlayerState state = this.players.get(entry.getKey());
            if (state != null) {
                if (entry.getValue().dimensionChanged()) {
                    state.clearProcessingState();
                    cleanupDedupGroups(this.dedupTracker.removePlayer(entry.getKey()));
                }
                state.drainDirtyClearRequests();
                while (true) {
                    Integer cancelId = state.pollCancel();
                    if (cancelId != null) {
                        PendingRequest pending = state.removePendingByRequestId(cancelId.intValue());
                        if (pending != null) {
                            state.getRateLimiters().forRequest(pending.type(), this.diskReadingAvailable).release();
                        }
                    }
                }
            }
        }
    }

    private void cleanupDedupGroups(List<DedupTracker.RemovedGroup> removedGroups) {
        for (DedupTracker.RemovedGroup rg : removedGroups) {
            int cx = PositionUtil.unpackX(rg.packed());
            int cz = PositionUtil.unpackZ(rg.packed());
            for (DedupTracker.Attachment attachment : rg.group().attached()) {
                PlayerState attachedState = this.players.get(attachment.playerUuid());
                if (attachedState != null) {
                    attachedState.removePendingByPosition(cx, cz);
                    attachedState.getRateLimiters().syncOnLoad().release();
                }
            }
        }
    }

    private void drainDiskResultsForAllPlayers(TickSnapshot snapshot) {
        for (Map.Entry<UUID, TickSnapshot.PlayerTickData> entry : snapshot.players().entrySet()) {
            PlayerState state = this.players.get(entry.getKey());
            if (state != null) {
                UUID playerUuid = entry.getKey();
                while (true) {
                    ReadResultAccess readResultAccessPollDiskResult = pollDiskResult(state);
                    if (readResultAccessPollDiskResult != null) {
                        int cx = readResultAccessPollDiskResult.chunkX();
                        int cz = readResultAccessPollDiskResult.chunkZ();
                        PendingRequest pending = state.removePendingByPosition(cx, cz);
                        int requestId = pending != null ? pending.requestId() : readResultAccessPollDiskResult.requestId();
                        state.getRateLimiters().syncOnLoad().release();
                        long packed = PositionUtil.packPosition(cx, cz);
                        if (readResultAccessPollDiskResult.saturated()) {
                            this.ctx.sendActions().add(new SendAction.RateLimited(playerUuid, requestId));
                            if (VSSLogger.isDebugEnabled()) {
                                VSSLogger.debug("Rate-limited " + String.valueOf(playerUuid) + " (disk saturated): chunk [" + cx + ", " + cz + "]");
                            }
                        } else if (readResultAccessPollDiskResult.notFound()) {
                            handleDiskNotFound(playerUuid, state, requestId, cx, cz, pending);
                        } else {
                            state.markDiskReadDone(cx, cz);
                            if (readResultAccessPollDiskResult.sectionBytes() != null) {
                                enqueueResultPayloads(state, readResultAccessPollDiskResult);
                            } else {
                                this.ctx.sendActions().add(new SendAction.ColumnUpToDate(playerUuid, requestId));
                            }
                            this.timestampCache.put(entry.getValue().dimension(), packed, readResultAccessPollDiskResult.columnTimestamp(), this.cycleNow);
                        }
                        this.ctx.diagnostics().incrementDiskDrained();
                        DedupTracker.Group group = this.dedupTracker.removeGroup(packed);
                        if (group != null) {
                            dispatchDedupGroup(group, readResultAccessPollDiskResult, cx, cz);
                        }
                    }
                }
            }
        }
    }

    private void dispatchDedupGroup(DedupTracker.Group group, ReadResult result, int cx, int cz) {
        byte[] sectionBytes = result.sectionBytes();
        for (DedupTracker.Attachment attachment : group.attached()) {
            PlayerState attachedState = this.players.get(attachment.playerUuid());
            if (attachedState != null) {
                PendingRequest attachedPending = attachedState.removePendingByPosition(cx, cz);
                attachedState.getRateLimiters().syncOnLoad().release();
                int attachedRequestId = attachedPending != null ? attachedPending.requestId() : attachment.requestId();
                if (result.saturated()) {
                    this.ctx.sendActions().add(new SendAction.RateLimited(attachment.playerUuid(), attachedRequestId));
                } else if (result.notFound()) {
                    handleDiskNotFound(attachment.playerUuid(), attachedState, attachedRequestId, cx, cz, attachedPending);
                } else if (sectionBytes != null) {
                    attachedState.markDiskReadDone(cx, cz);
                    buildAndEnqueueColumnPayload(attachedState, cx, cz, group.dimension(), attachedRequestId, result.columnTimestamp(), attachment.submissionOrder(), sectionBytes, result.estimatedBytes());
                } else {
                    attachedState.markDiskReadDone(cx, cz);
                    this.ctx.sendActions().add(new SendAction.ColumnUpToDate(attachment.playerUuid(), attachedRequestId));
                }
                this.ctx.diagnostics().incrementDiskDrained();
            }
        }
    }

    private void handleDiskNotFound(UUID playerUuid, PlayerState state, int requestId, int cx, int cz, PendingRequest pending) {
        if (pending != null && pending.type() == RequestType.GENERATION && this.generationAvailable) {
            if (state.getRateLimiters().generation().tryAcquire()) {
                this.ctx.generationTicketRequests().add(new GenerationTicketRequest(playerUuid, requestId, cx, cz, this.ctx.sequence().next()));
                state.addPendingRequest(new PendingRequest(requestId, cx, cz, RequestType.GENERATION));
                return;
            } else {
                this.ctx.sendActions().add(new SendAction.ColumnNotGenerated(playerUuid, requestId));
                return;
            }
        }
        this.ctx.sendActions().add(new SendAction.ColumnNotGenerated(playerUuid, requestId));
    }

    private void drainGenerationResultsForAllPlayers(TickSnapshot snapshot) {
        for (Map.Entry<UUID, TickSnapshot.PlayerTickData> entry : snapshot.players().entrySet()) {
            PlayerState state = this.players.get(entry.getKey());
            if (state != null) {
                UUID playerUuid = entry.getKey();
                while (true) {
                    ReadResultAccess readResultAccessPollGenerationResult = pollGenerationResult(state);
                    if (readResultAccessPollGenerationResult != null) {
                        int cx = readResultAccessPollGenerationResult.chunkX();
                        int cz = readResultAccessPollGenerationResult.chunkZ();
                        PendingRequest pending = state.removePendingByPosition(cx, cz);
                        int requestId = pending != null ? pending.requestId() : readResultAccessPollGenerationResult.requestId();
                        state.getRateLimiters().generation().release();
                        if (readResultAccessPollGenerationResult.notFound()) {
                            this.ctx.sendActions().add(new SendAction.ColumnNotGenerated(playerUuid, requestId));
                        } else {
                            state.markDiskReadDone(cx, cz);
                            enqueueResultPayloads(state, readResultAccessPollGenerationResult);
                            long packed = PositionUtil.packPosition(cx, cz);
                            this.timestampCache.put(entry.getValue().dimension(), packed, readResultAccessPollGenerationResult.columnTimestamp(), this.cycleNow);
                        }
                        this.ctx.diagnostics().incrementGenDrained();
                    }
                }
            }
        }
    }

    private void processGenerationReady(TickSnapshot snapshot) {
        for (TickSnapshot.GenerationReadyData genReady : snapshot.generationReady()) {
            PlayerState state = this.players.get(genReady.playerUuid());
            if (state != null) {
                int cx = genReady.columnData().cx();
                int cz = genReady.columnData().cz();
                state.removePendingByPosition(cx, cz);
                state.markDiskReadDone(cx, cz);
                state.getRateLimiters().generation().release();
                TickSnapshot.PlayerTickData playerData = snapshot.players().get(genReady.playerUuid());
                if (playerData != null) {
                    String dimension = playerData.dimension();
                    boolean sent = compressAndEnqueueLoaded(state, genReady.columnData(), genReady.requestId(), genReady.columnTimestamp(), genReady.submissionOrder(), dimension);
                    if (!sent) {
                        this.sendActions.add(new SendAction.ColumnUpToDate(genReady.playerUuid(), genReady.requestId()));
                    }
                    this.ctx.diagnostics().incrementGenDrained();
                }
            }
        }
    }

    private void routeIncomingRequests(TickSnapshot tickSnapshot) {
        this.requestRouter.routeAll(tickSnapshot, this.players, this::submitDiskRead, this::compressAndEnqueueLoaded, this.cycleNow);
    }

    public ProcessingDiagnostics getDiagnostics() {
        return this.ctx.diagnostics();
    }

    public void shutdown() {
        postSnapshot(TickSnapshot.shutdownSentinel());
        try {
            this.processingThread.interrupt();
            this.processingThread.join(5000L);
            if (this.processingThread.isAlive()) {
                VSSLogger.warn("Processing thread did not terminate within 5000ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (this.dataDir != null) {
            this.timestampCache.save(this.dataDir);
        }
    }
}

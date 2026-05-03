package dev.xantha.vss.networking.client;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.networking.client.InFlightTracker;
import dev.xantha.vss.networking.client.SpiralScanner;
import dev.xantha.vss.networking.payloads.BatchChunkRequestC2SPayload;
import dev.xantha.vss.networking.payloads.CancelRequestC2SPayload;
import dev.xantha.vss.networking.payloads.SessionConfigS2CPayload;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/LodRequestManager.class */
public class LodRequestManager {
    private static final int BACKPRESSURE_NUMERATOR = 3;
    private static final int BACKPRESSURE_DENOMINATOR = 4;
    private static final int MIN_SEND_PER_TICK = 16;
    private static final long TIMEOUT_NANOS = 10000000000L;
    private SessionConfigS2CPayload sessionConfig;
    private String serverAddress;
    private int lastChunkX;
    private int lastChunkZ;
    private ResourceKey<Level> lastDimension;
    private final Long2LongOpenHashMap columnTimestamps = new Long2LongOpenHashMap();
    private final InFlightTracker tracker;
    private final RequestQueue queue;
    private boolean cacheLoaded;
    private volatile CompletableFuture<Long2LongOpenHashMap> pendingCacheLoad;
    private final LongOpenHashSet dirtyColumns;
    private final LongOpenHashSet rateLimitRetryPositions;
    private final LongOpenHashSet validatedThisSession;
    private final RequestMetrics metrics;
    private int maxGenConcurrency;
    private int maxSyncConcurrency;
    private boolean skipNextScan;
    private int maxSendPerTick;
    private int[] sendRequestIdBuffer;
    private long[] sendPositionBuffer;
    private long[] sendTimestampBuffer;
    private final SpiralScanner scanner;

    public LodRequestManager() {
        this.columnTimestamps.defaultReturnValue(-1L);
        this.tracker = new InFlightTracker();
        this.queue = new RequestQueue();
        this.cacheLoaded = false;
        this.pendingCacheLoad = null;
        this.dirtyColumns = new LongOpenHashSet();
        this.rateLimitRetryPositions = new LongOpenHashSet();
        this.validatedThisSession = new LongOpenHashSet();
        this.metrics = new RequestMetrics();
        this.maxSendPerTick = MIN_SEND_PER_TICK;
        this.sendRequestIdBuffer = new int[MIN_SEND_PER_TICK];
        this.sendPositionBuffer = new long[MIN_SEND_PER_TICK];
        this.sendTimestampBuffer = new long[MIN_SEND_PER_TICK];
        this.scanner = new SpiralScanner();
    }

    private void putTimestamp(long packed, long timestamp) {
        long old = this.columnTimestamps.put(packed, timestamp);
        this.metrics.adjustCounters(old, timestamp);
    }

    private void clearTimestamps() {
        this.columnTimestamps.clear();
        this.metrics.reset();
    }

    private void loadTimestamps(Long2LongOpenHashMap loaded) {
        this.columnTimestamps.putAll(loaded);
        this.metrics.bulkRecount(this.columnTimestamps);
    }

    public void onSessionConfig(SessionConfigS2CPayload config, String serverAddress) {
        this.sessionConfig = config;
        this.serverAddress = serverAddress;
        resetRequestState();
        this.lastDimension = null;
        this.cacheLoaded = false;
        this.scanner.reset();
        this.maxGenConcurrency = config.generationConcurrencyLimitPerPlayer();
        this.maxSyncConcurrency = config.syncOnLoadConcurrencyLimitPerPlayer();
        this.skipNextScan = false;
    }

    private static int countMissingVanillaChunks(ClientLevel level, int playerCx, int playerCz, int radius) {
        ClientChunkCache chunkSource = level.getChunkSource();
        int missing = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (!chunkSource.hasChunk(playerCx + dx, playerCz + dz)) {
                    missing++;
                }
            }
        }
        return missing;
    }

    public void tick() {
        Minecraft mc;
        LocalPlayer player;
        ClientLevel level;
        int sent;
        if (this.sessionConfig == null || !this.sessionConfig.enabled() || (player = (mc = Minecraft.getInstance()).player) == null || player.isDeadOrDying() || (level = mc.level) == null) {
            return;
        }
        int playerCx = player.getBlockX() >> BACKPRESSURE_DENOMINATOR;
        int playerCz = player.getBlockZ() >> BACKPRESSURE_DENOMINATOR;
        ResourceKey<Level> currentDim = level.dimension();
        if (this.lastDimension != null && !currentDim.equals(this.lastDimension)) {
            onDimensionChange(currentDim);
        } else if (!this.cacheLoaded) {
            this.cacheLoaded = true;
            startAsyncCacheLoad(currentDim);
        }
        this.lastDimension = currentDim;
        if (playerCx != this.lastChunkX || playerCz != this.lastChunkZ) {
            int pruneDistance = this.scanner.getPruneDistance(this.sessionConfig);
            this.scanner.pruneOutOfRangeTimestamps(this.columnTimestamps, this.metrics, playerCx, playerCz, pruneDistance);
            this.scanner.pruneOutOfRangePositions(this.dirtyColumns, playerCx, playerCz, pruneDistance);
            this.scanner.pruneOutOfRangePositions(this.rateLimitRetryPositions, playerCx, playerCz, pruneDistance);
            this.scanner.pruneOutOfRangePositions(this.validatedThisSession, playerCx, playerCz, pruneDistance);
            pruneAndCancelOutOfRangePending(playerCx, playerCz, pruneDistance);
            this.lastChunkX = playerCx;
            this.lastChunkZ = playerCz;
            this.scanner.resetScanCounter();
        }
        this.metrics.updateRollingRates();
        int columnQueueSize = VSSClientNetworking.getQueuedColumnCount();
        if (columnQueueSize >= (8000 * BACKPRESSURE_NUMERATOR) / BACKPRESSURE_DENOMINATOR) {
            return;
        }
        if (this.pendingCacheLoad != null) {
            if (!this.pendingCacheLoad.isDone()) {
                return;
            }
            try {
                Long2LongOpenHashMap loaded = this.pendingCacheLoad.getNow(null);
                if (loaded != null && this.lastDimension != null) {
                    loadTimestamps(loaded);
                }
            } catch (Exception e) {
            }
            this.pendingCacheLoad = null;
        }
        if (this.scanner.advanceScanTick()) {
            if (this.skipNextScan) {
                this.skipNextScan = false;
            } else {
                int viewDistance = mc.options.getEffectiveRenderDistance();
                int missingVanilla = countMissingVanillaChunks(level, playerCx, playerCz, viewDistance);
                this.scanner.updateMissingVanillaChunks(missingVanilla);
                int budget = SpiralScanner.baseBudget(this.sessionConfig);
                int haltThreshold = (8000 * BACKPRESSURE_NUMERATOR) / BACKPRESSURE_DENOMINATOR;
                if (columnQueueSize > 0) {
                    budget = Math.max(1, Math.round(budget * Math.max(0.0f, 1.0f - (columnQueueSize / haltThreshold))));
                }
                if (missingVanilla > 0) {
                    int exclusionArea = ((2 * viewDistance) + 1) * ((2 * viewDistance) + 1);
                    float missingFraction = missingVanilla / exclusionArea;
                    float vanillaScale = Math.max(0.0f, 1.0f - (missingFraction * missingFraction));
                    budget = vanillaScale <= 0.0f ? 0 : Math.max(1, Math.round(budget * vanillaScale));
                }
                if (budget > 0) {
                    SpiralScanner spiralScanner = this.scanner;
                    Long2LongOpenHashMap long2LongOpenHashMap = this.columnTimestamps;
                    LongOpenHashSet longOpenHashSet = this.dirtyColumns;
                    LongOpenHashSet longOpenHashSet2 = this.rateLimitRetryPositions;
                    LongOpenHashSet longOpenHashSet3 = this.validatedThisSession;
                    InFlightTracker inFlightTracker = this.tracker;
                    Objects.requireNonNull(inFlightTracker);
                    SpiralScanner.ScanResult scanResult = spiralScanner.scan(playerCx, playerCz, viewDistance, long2LongOpenHashMap, longOpenHashSet, longOpenHashSet2, longOpenHashSet3, inFlightTracker::isInFlight, this.sessionConfig, budget);
                    if (scanResult.count() > 0) {
                        this.queue.populate(scanResult);
                        updateSendPerTick(scanResult.count());
                    }
                }
            }
            this.tracker.timeoutSweep(TIMEOUT_NANOS);
        }
        if (!this.queue.hasNext() || (sent = drainQueue(this.maxSendPerTick)) <= 0) {
            return;
        }
        sendRequests(this.sendPositionBuffer, this.sendTimestampBuffer, sent);
    }

    private void updateSendPerTick(int lastScanQueued) {
        int perTick = Math.min(1024, Math.max(MIN_SEND_PER_TICK, ((lastScanQueued + 20) - 1) / 20));
        if (perTick != this.maxSendPerTick) {
            this.maxSendPerTick = perTick;
            if (perTick > this.sendPositionBuffer.length) {
                this.sendRequestIdBuffer = new int[perTick];
                this.sendPositionBuffer = new long[perTick];
                this.sendTimestampBuffer = new long[perTick];
            }
        }
    }

    private int drainQueue(int maxToSend) {
        long now = System.nanoTime();
        long[] positionBuffer = this.sendPositionBuffer;
        long[] timestampBuffer = this.sendTimestampBuffer;
        int count = 0;
        while (count < maxToSend && this.queue.hasNext()) {
            long pos = this.queue.peekPosition();
            long ts = this.queue.peekTimestamp();
            if (this.tracker.isInFlight(pos)) {
                this.queue.skip();
            } else {
                long stored = this.columnTimestamps.get(pos);
                if (stored <= 0 || this.dirtyColumns.contains(pos) || this.rateLimitRetryPositions.contains(pos) || !this.validatedThisSession.contains(pos)) {
                    boolean isGen = ts == 0;
                    if (isGen && this.tracker.generationCount() >= this.maxGenConcurrency) {
                        this.queue.skip();
                    } else if (isGen || this.tracker.size() - this.tracker.generationCount() < this.maxSyncConcurrency) {
                        this.queue.skip();
                        positionBuffer[count] = pos;
                        timestampBuffer[count] = ts;
                        this.tracker.markPending(pos, now, isGen);
                        this.rateLimitRetryPositions.remove(pos);
                        this.dirtyColumns.remove(pos);
                        count++;
                    } else {
                        this.queue.skip();
                    }
                } else {
                    this.queue.skip();
                }
            }
        }
        return count;
    }

    private void sendRequests(long[] positionBuffer, long[] timestampBuffer, int count) {
        int[] requestIds = this.sendRequestIdBuffer;
        for (int i = 0; i < count; i++) {
            requestIds[i] = this.tracker.send(positionBuffer[i]);
        }
        try {
            ClientPlayNetworking.send(new BatchChunkRequestC2SPayload(requestIds, positionBuffer, timestampBuffer, count));
        } catch (Exception e) {
            VSSLogger.error("Failed to send batch chunk request", e);
            for (int i2 = 0; i2 < count; i2++) {
                this.tracker.removeByRequestId(requestIds[i2]);
            }
        }
        this.metrics.recordSendCycle(count);
    }

    public void onColumnReceived(int requestId, long columnTimestamp) {
        InFlightTracker.RemovedRequest completion = this.tracker.removeByRequestId(requestId);
        if (completion != null) {
            this.dirtyColumns.remove(completion.position());
            putTimestamp(completion.position(), columnTimestamp);
            this.validatedThisSession.add(completion.position());
        }
        this.metrics.recordColumnReceived();
    }

    public void onDirtyColumns(long[] dirtyPositions) {
        boolean added = false;
        for (long packed : dirtyPositions) {
            long stored = this.columnTimestamps.get(packed);
            if (stored > 0) {
                this.dirtyColumns.add(packed);
                added = true;
            }
        }
        if (added) {
            this.scanner.resetScanCounter();
        }
    }

    public void onColumnNotGenerated(int requestId) {
        InFlightTracker.RemovedRequest removal = this.tracker.removeByRequestId(requestId);
        if (removal != null) {
            putTimestamp(removal.position(), 0L);
        }
        this.metrics.recordNotGenerated();
    }

    public void onColumnUpToDate(int requestId) {
        InFlightTracker.RemovedRequest completion = this.tracker.removeByRequestId(requestId);
        if (completion != null) {
            this.validatedThisSession.add(completion.position());
            if (this.columnTimestamps.get(completion.position()) == -1) {
                putTimestamp(completion.position(), VSSConstants.epochSeconds());
            }
        }
        this.metrics.recordUpToDate();
    }

    public void onRateLimited(int requestId) {
        InFlightTracker.RemovedRequest removal = this.tracker.removeByRequestId(requestId);
        if (removal != null) {
            this.rateLimitRetryPositions.add(removal.position());
        }
        this.metrics.recordRateLimited();
        this.skipNextScan = true;
    }

    private void onDimensionChange(ResourceKey<Level> newDimension) {
        saveCache();
        cancelAllPending();
        resetRequestState();
        this.queue.clear();
        this.scanner.resetScanCounter();
        this.cacheLoaded = true;
        startAsyncCacheLoad(newDimension);
    }

    private void resetRequestState() {
        clearTimestamps();
        this.dirtyColumns.clear();
        this.rateLimitRetryPositions.clear();
        this.validatedThisSession.clear();
        this.tracker.clear();
        this.skipNextScan = false;
    }

    private void startAsyncCacheLoad(ResourceKey<Level> dimension) {
        this.pendingCacheLoad = ColumnCacheStore.loadAsync(this.serverAddress, dimension);
    }

    private void sendCancelPacket(int reqId) {
        try {
            ClientPlayNetworking.send(new CancelRequestC2SPayload(reqId));
        } catch (Exception e) {
        }
    }

    private void pruneAndCancelOutOfRangePending(int playerCx, int playerCz, int pruneDistance) {
        this.tracker.pruneOutOfRange(playerCx, playerCz, pruneDistance, this::sendCancelPacket);
    }

    private void cancelAllPending() {
        this.tracker.forEachRequestId(this::sendCancelPacket);
    }

    public void disconnect() {
        this.tracker.clear();
    }

    public void saveCache() {
        if (this.serverAddress != null && this.lastDimension != null && !this.columnTimestamps.isEmpty()) {
            ColumnCacheStore.saveAsync(this.serverAddress, this.lastDimension, this.columnTimestamps);
        }
    }

    public void flushCache() {
        if (this.serverAddress != null) {
            ColumnCacheStore.clearForServer(this.serverAddress);
        }
        cancelAllPending();
        resetRequestState();
        this.queue.clear();
        this.scanner.reset();
    }

    public int getReceivedColumnCount() {
        return this.metrics.getReceivedCount();
    }

    public int getEmptyColumnCount() {
        return this.metrics.getEmptyCount();
    }

    public int getEffectiveLodDistanceChunks() {
        if (this.sessionConfig != null) {
            return this.scanner.getEffectiveLodDistance(this.sessionConfig);
        }
        return 0;
    }

    public long getTotalSendCycles() {
        return this.metrics.getTotalSendCycles();
    }

    public long getTotalPositionsRequested() {
        return this.metrics.getTotalPositionsRequested();
    }

    public int getDirtyColumnCount() {
        return this.dirtyColumns.size();
    }

    public int getConfirmedRing() {
        return this.scanner.getConfirmedRing();
    }

    public int getScanRing() {
        return this.scanner.getScanRing();
    }

    public int getMissingVanillaChunks() {
        return this.scanner.getMissingVanillaChunks();
    }

    public long getTotalColumnsReceived() {
        return this.metrics.getTotalColumnsReceived();
    }

    public long getTotalUpToDate() {
        return this.metrics.getTotalUpToDate();
    }

    public long getTotalNotGenerated() {
        return this.metrics.getTotalNotGenerated();
    }

    public long getTotalRateLimited() {
        return this.metrics.getTotalRateLimited();
    }

    public double getReceiveRate() {
        return this.metrics.getReceiveRate();
    }

    public double getRequestRate() {
        return this.metrics.getRequestRate();
    }

    public int getPendingCount() {
        return this.tracker.size();
    }

    public int getQueueRemaining() {
        return this.queue.remaining();
    }

    public int getLastBudget() {
        return this.scanner.getLastBudget();
    }

    public int getLastSyncQueued() {
        return this.scanner.getLastSyncQueued();
    }

    public int getLastGenQueued() {
        return this.scanner.getLastGenQueued();
    }
}

package dev.xantha.vss.networking.server;

import dev.xantha.vss.common.PositionUtil;
import dev.xantha.vss.common.SharedBandwidthLimiter;
import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.IncomingRequest;
import dev.xantha.vss.common.processing.LoadedColumnData;
import dev.xantha.vss.common.processing.OffThreadProcessor;
import dev.xantha.vss.common.processing.RateLimiterSet;
import dev.xantha.vss.common.processing.SendAction;
import dev.xantha.vss.common.processing.SendActionBatcher;
import dev.xantha.vss.common.processing.TickDiagnostics;
import dev.xantha.vss.common.processing.TickSnapshot;
import dev.xantha.vss.common.tracking.DirtyColumnTracker;
import dev.xantha.vss.config.VSSServerConfig;
import dev.xantha.vss.networking.payloads.BandwidthUpdateC2SPayload;
import dev.xantha.vss.networking.payloads.BatchChunkRequestC2SPayload;
import dev.xantha.vss.networking.payloads.BatchResponseS2CPayload;
import dev.xantha.vss.networking.payloads.CancelRequestC2SPayload;
import dev.xantha.vss.networking.server.PlayerRequestState;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/RequestProcessingService.class */
public class RequestProcessingService {
    private final MinecraftServer server;
    private final ChunkDiskReader diskReader;
    private final ChunkGenerationService generationService;
    private final SharedBandwidthLimiter bandwidthLimiter;
    private final FabricOffThreadProcessor offThreadProcessor;
    private final DirtyColumnTracker dirtyTracker;
    private final DirtyColumnBroadcaster dirtyBroadcaster;
    private static final int DIAG_LOG_INTERVAL_TICKS = 100;
    private static final int MAX_PROBES_PER_TICK_PER_PLAYER = 512;
    private final Map<UUID, PlayerRequestState> players = new ConcurrentHashMap();
    private final long startTimeNanos = System.nanoTime();
    private final Map<ServerLevel, String> dimensionStringCache = new HashMap();
    private int diagLogCounter = 0;
    private final TickDiagnostics diag = new TickDiagnostics();
    private final Map<UUID, TickSnapshot.PlayerTickData> reusablePlayerTickData = new HashMap();
    private final Map<UUID, Long2ObjectMap<LoadedColumnData>> reusableLoadedChunkProbes = new HashMap();
    private final SendActionBatcher sendActionBatcher = new SendActionBatcher();

    public RequestProcessingService(MinecraftServer server) {
        this.server = server;
        VSSServerConfig config = VSSServerConfig.CONFIG;
        this.dirtyTracker = new DirtyColumnTracker();
        this.diskReader = new ChunkDiskReader(config.diskReaderThreads);
        if (config.enableChunkGeneration) {
            this.generationService = new ChunkGenerationService(config);
        } else {
            this.generationService = null;
        }
        this.bandwidthLimiter = new SharedBandwidthLimiter(config.bytesPerSecondLimitGlobal);
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data");
        this.offThreadProcessor = new FabricOffThreadProcessor(this.players, this.diskReader, this.generationService, dataDir, config.perDimensionTimestampCacheSizeMB);
        this.offThreadProcessor.start();
        this.dirtyBroadcaster = new DirtyColumnBroadcaster(server, this.players, this.offThreadProcessor, this.dirtyTracker);
    }

    public PlayerRequestState registerPlayer(ServerPlayer player, int capabilities) {
        VSSServerConfig config = VSSServerConfig.CONFIG;
        PlayerRequestState state = this.players.computeIfAbsent(player.getUUID(), uuid -> {
            return new PlayerRequestState(player, config.syncOnLoadRateLimitPerPlayer, config.syncOnLoadConcurrencyLimitPerPlayer, config.generationRateLimitPerPlayer, config.generationConcurrencyLimitPerPlayer);
        });
        this.diskReader.registerPlayer(player.getUUID());
        if (this.generationService != null) {
            this.generationService.registerPlayer(player.getUUID());
        }
        state.setCapabilities(capabilities);
        state.markHandshakeComplete();
        return state;
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
        cleanupPlayerServices(uuid);
    }

    private void cleanupPlayerServices(UUID uuid) {
        this.diskReader.removePlayerResults(uuid);
        if (this.generationService != null) {
            this.generationService.removePlayer(uuid);
        }
    }

    public void handleBatchRequest(ServerPlayer player, BatchChunkRequestC2SPayload payload) {
        PlayerRequestState state = this.players.get(player.getUUID());
        if (state == null || !state.hasCompletedHandshake()) {
            return;
        }
        int playerCx = player.getBlockX() >> 4;
        int playerCz = player.getBlockZ() >> 4;
        int maxDist = VSSServerConfig.CONFIG.lodDistanceChunks + 32;
        for (int i = 0; i < payload.count(); i++) {
            long packedPosition = payload.packedPositions()[i];
            int cx = PositionUtil.unpackX(packedPosition);
            int cz = PositionUtil.unpackZ(packedPosition);
            if (PositionUtil.chebyshevDistance(cx, cz, playerCx, playerCz) <= maxDist) {
                state.addRequest(payload.requestIds()[i], packedPosition, payload.clientTimestamps()[i]);
            }
        }
    }

    public void handleCancel(ServerPlayer player, CancelRequestC2SPayload payload) {
        PlayerRequestState state = this.players.get(player.getUUID());
        if (state == null || !state.hasCompletedHandshake()) {
            return;
        }
        state.addCancel(payload.requestId());
    }

    public void handleBandwidthUpdate(ServerPlayer player, BandwidthUpdateC2SPayload payload) {
        PlayerRequestState state = this.players.get(player.getUUID());
        if (state == null || !state.hasCompletedHandshake()) {
            return;
        }
        state.setDesiredBandwidth(payload.desiredRate());
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public void tick() throws MatchException {
        if (VSSServerConfig.CONFIG.enabled) {
            this.diag.reset(this.offThreadProcessor.getDiagnostics());
            VSSServerConfig config = VSSServerConfig.CONFIG;
            List<TickSnapshot.GenerationReadyData> generationReady = tickGenerationService();
            LifecycleResult lifecycle = processPlayerLifecycle(config, generationReady);
            if (lifecycle.toRemove != null) {
                for (UUID uuid : lifecycle.toRemove) {
                    removePlayer(uuid);
                }
            }
            postSnapshot(lifecycle, generationReady, config);
            drainSendActions();
            drainGenerationTicketRequests();
            flushSendQueues(lifecycle.activeCount, config);
            tickDirtyBroadcast(config);
            tickDiagnosticsLog(config);
        }
    }

    private List<TickSnapshot.GenerationReadyData> tickGenerationService() {
        return this.generationService == null ? List.of() : this.generationService.tick();
    }

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult.class */
    private static final class LifecycleResult extends Record {
        private final Map<UUID, TickSnapshot.PlayerTickData> playerTickData;
        private final Map<UUID, Long2ObjectMap<LoadedColumnData>> loadedChunkProbes;
        private final int activeCount;
        private final List<UUID> toRemove;

        private LifecycleResult(Map<UUID, TickSnapshot.PlayerTickData> playerTickData, Map<UUID, Long2ObjectMap<LoadedColumnData>> loadedChunkProbes, int activeCount, List<UUID> toRemove) {
            this.playerTickData = playerTickData;
            this.loadedChunkProbes = loadedChunkProbes;
            this.activeCount = activeCount;
            this.toRemove = toRemove;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, LifecycleResult.class), LifecycleResult.class, "playerTickData;loadedChunkProbes;activeCount;toRemove", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->playerTickData:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->loadedChunkProbes:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->activeCount:I", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->toRemove:Ljava/util/List;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, LifecycleResult.class), LifecycleResult.class, "playerTickData;loadedChunkProbes;activeCount;toRemove", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->playerTickData:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->loadedChunkProbes:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->activeCount:I", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->toRemove:Ljava/util/List;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, LifecycleResult.class, Object.class), LifecycleResult.class, "playerTickData;loadedChunkProbes;activeCount;toRemove", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->playerTickData:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->loadedChunkProbes:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->activeCount:I", "FIELD:Ldev/xantha/vss/networking/server/RequestProcessingService$LifecycleResult;->toRemove:Ljava/util/List;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public Map<UUID, TickSnapshot.PlayerTickData> playerTickData() {
            return this.playerTickData;
        }

        public Map<UUID, Long2ObjectMap<LoadedColumnData>> loadedChunkProbes() {
            return this.loadedChunkProbes;
        }

        public int activeCount() {
            return this.activeCount;
        }

        public List<UUID> toRemove() {
            return this.toRemove;
        }
    }

    private LifecycleResult processPlayerLifecycle(VSSServerConfig config, List<TickSnapshot.GenerationReadyData> generationReady) {
        this.reusablePlayerTickData.clear();
        this.reusableLoadedChunkProbes.clear();
        Map<UUID, TickSnapshot.PlayerTickData> playerTickData = this.reusablePlayerTickData;
        Map<UUID, Long2ObjectMap<LoadedColumnData>> loadedChunkProbes = this.reusableLoadedChunkProbes;
        Map<UUID, LongOpenHashSet> genReadyPositions = null;
        if (!generationReady.isEmpty()) {
            genReadyPositions = new HashMap<>();
            for (TickSnapshot.GenerationReadyData genData : generationReady) {
                genReadyPositions.computeIfAbsent(genData.playerUuid(), k -> {
                    return new LongOpenHashSet();
                }).add(PositionUtil.packPosition(genData.columnData().cx(), genData.columnData().cz()));
            }
        }
        int activeCount = 0;
        List<UUID> toRemove = null;
        for (PlayerRequestState state : this.players.values()) {
            if (state.hasCompletedHandshake()) {
                activeCount++;
                this.diag.updateQueuePeak(state.getSendQueueSize());
                boolean removed = false;
                boolean dimensionChanged = false;
                if (state.getPlayer().isRemoved()) {
                    ServerPlayer current = this.server.getPlayerList().getPlayer(state.getPlayer().getUUID());
                    if (current == null) {
                        if (toRemove == null) {
                            toRemove = new ArrayList<>();
                        }
                        toRemove.add(state.getPlayer().getUUID());
                        removed = true;
                    } else {
                        state.updatePlayer(current);
                    }
                }
                if (!removed) {
                    if (state.checkDimensionChange()) {
                        state.onDimensionChange();
                        cleanupPlayerServices(state.getPlayer().getUUID());
                        this.diskReader.registerPlayer(state.getPlayer().getUUID());
                        if (this.generationService != null) {
                            this.generationService.registerPlayer(state.getPlayer().getUUID());
                        }
                        dimensionChanged = true;
                    }
                    ServerPlayer player = state.getPlayer();
                    ServerLevel level = player.level();
                    String dimension = this.dimensionStringCache.computeIfAbsent(level, l -> {
                        return l.dimension().identifier().toString();
                    });
                    this.offThreadProcessor.updateDimensionContext(dimension, level);
                    playerTickData.put(player.getUUID(), new TickSnapshot.PlayerTickData(dimension, dimensionChanged));
                    if (!dimensionChanged) {
                        LongOpenHashSet skipPositions = genReadyPositions != null ? genReadyPositions.get(player.getUUID()) : null;
                        Long2ObjectMap<LoadedColumnData> probes = probeLoadedChunks(state, level, skipPositions);
                        if (!probes.isEmpty()) {
                            loadedChunkProbes.put(player.getUUID(), probes);
                        }
                    }
                }
            }
        }
        return new LifecycleResult(playerTickData, loadedChunkProbes, activeCount, toRemove);
    }

    private void postSnapshot(LifecycleResult lifecycle, List<TickSnapshot.GenerationReadyData> generationReady, VSSServerConfig config) {
        List<UUID> removed = lifecycle.toRemove != null ? lifecycle.toRemove : List.of();
        TickSnapshot snapshot = new TickSnapshot(lifecycle.playerTickData, lifecycle.loadedChunkProbes, generationReady, removed, config.sendQueueLimitPerPlayer, false);
        this.offThreadProcessor.postSnapshot(snapshot);
    }

    private void flushSendQueues(int activeCount, VSSServerConfig config) {
        long perPlayerAllocation = this.bandwidthLimiter.getPerPlayerAllocation(activeCount);
        long perPlayerCap = Math.min(perPlayerAllocation, config.bytesPerSecondLimitPerPlayer);
        for (PlayerRequestState state : this.players.values()) {
            if (state.hasCompletedHandshake()) {
                long effective = Math.min(perPlayerCap, Math.max(1L, state.getDesiredBandwidth()));
                flushSendQueue(state, effective);
            }
        }
    }

    private void tickDirtyBroadcast(VSSServerConfig config) {
        this.dirtyBroadcaster.tick(config);
    }

    private void tickDiagnosticsLog(VSSServerConfig config) {
        int i = this.diagLogCounter + 1;
        this.diagLogCounter = i;
        if (i >= DIAG_LOG_INTERVAL_TICKS) {
            this.diagLogCounter = 0;
            if (VSSLogger.isDebugEnabled()) {
                long uptimeSec = getUptimeSeconds();
                long bwRate = uptimeSec > 0 ? this.bandwidthLimiter.getTotalBytesSent() / uptimeSec : 0L;
                VSSLogger.debug(this.diag.formatSummary(bwRate, config.bytesPerSecondLimitGlobal));
                for (PlayerRequestState state : this.players.values()) {
                    if (state.hasCompletedHandshake()) {
                        RateLimiterSet rl = state.getRateLimiters();
                        VSSLogger.debug(String.format("  %s: sq=%d, psync=%d, pgen=%d, syncCC=%d/%d, genCC=%d/%d, wq=%d", state.getPlayer().getName().getString(), Integer.valueOf(state.getSendQueueSize()), Integer.valueOf(state.getPendingSyncCount()), Integer.valueOf(state.getPendingGenerationCount()), Integer.valueOf(rl.syncOnLoad().getCurrentConcurrency()), Integer.valueOf(rl.syncOnLoad().getMaxConcurrency()), Integer.valueOf(rl.generation().getCurrentConcurrency()), Integer.valueOf(rl.generation().getMaxConcurrency()), Integer.valueOf(state.getWaitingQueueSize())));
                    }
                }
            }
        }
    }

    private Long2ObjectMap<LoadedColumnData> probeLoadedChunks(PlayerRequestState state, ServerLevel level, LongOpenHashSet skipPositions) {
        Long2ObjectOpenHashMap<LoadedColumnData> probes = new Long2ObjectOpenHashMap<>();
        int probed = 0;
        for (IncomingRequest req : state.getIncomingRequests()) {
            if (probed >= 512) {
                break;
            }
            long packed = PositionUtil.packPosition(req.cx(), req.cz());
            if (!probes.containsKey(packed) && (skipPositions == null || !skipPositions.contains(packed))) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(req.cx(), req.cz());
                if (chunk != null) {
                    probes.put(packed, SectionSerializer.serializeColumn(level, chunk, req.cx(), req.cz()));
                }
                probed++;
            }
        }
        return probes;
    }

    private void drainGenerationTicketRequests() {
        if (this.generationService == null) {
            return;
        }
        while (true) {
            OffThreadProcessor.GenerationTicketRequest req = this.offThreadProcessor.pollGenerationTicketRequest();
            if (req != null) {
                PlayerRequestState state = this.players.get(req.playerUuid());
                if (state != null && state.hasCompletedHandshake()) {
                    ServerPlayer player = state.getPlayer();
                    if (!player.isRemoved()) {
                        ServerLevel level = player.level();
                        boolean accepted = this.generationService.submitGeneration(req.playerUuid(), req.requestId(), level, req.cx(), req.cz(), req.submissionOrder());
                        if (!accepted) {
                            this.generationService.addResult(req.playerUuid(), ChunkDiskReader.emptyResult(req.playerUuid(), req.requestId(), req.cx(), req.cz(), req.submissionOrder()));
                        }
                    }
                }
            } else {
                return;
            }
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private void drainSendActions() throws MatchException {
        this.sendActionBatcher.clear();
        while (true) {
            SendAction action = this.offThreadProcessor.pollSendAction();
            if (action == null) {
                break;
            }
            PlayerRequestState state = this.players.get(action.playerUuid());
            if (state != null && state.hasCompletedHandshake()) {
                this.sendActionBatcher.add(action.playerUuid(), action.responseType(), action.requestId());
            }
        }
        if (this.sendActionBatcher.isEmpty()) {
            return;
        }
        this.sendActionBatcher.forEach((uuid, types, ids, count) -> {
            PlayerRequestState state2 = this.players.get(uuid);
            if (state2 == null || !state2.hasCompletedHandshake()) {
                return;
            }
            try {
                ServerPlayNetworking.send(state2.getPlayer(), new BatchResponseS2CPayload(types, ids, count));
            } catch (Exception e) {
                VSSLogger.error("Failed to send batch response to " + state2.getPlayer().getName().getString(), e);
            }
        });
    }

    private void flushSendQueue(PlayerRequestState state, long allocationBytes) {
        state.drainReadyPayloads();
        PriorityQueue<PlayerRequestState.QueuedPayload> queue = state.getSendQueue();
        while (!queue.isEmpty() && state.canSend(allocationBytes)) {
            PlayerRequestState.QueuedPayload queued = queue.peek();
            try {
                ServerPlayNetworking.send(state.getPlayer(), queued.payload());
                queue.poll();
                state.recordSend(queued.estimatedBytes());
                this.bandwidthLimiter.recordSend(queued.estimatedBytes());
                this.diag.recordSectionSent(queued.estimatedBytes());
            } catch (Exception e) {
                VSSLogger.error("Failed to send queued payload to " + state.getPlayer().getName().getString() + ", dropping remaining queue (" + queue.size() + " entries)", e);
                queue.clear();
                return;
            }
        }
    }

    public Map<UUID, PlayerRequestState> getPlayers() {
        return Collections.unmodifiableMap(this.players);
    }

    public ChunkDiskReader getDiskReader() {
        return this.diskReader;
    }

    public ChunkGenerationService getGenerationService() {
        return this.generationService;
    }

    public SharedBandwidthLimiter getBandwidthLimiter() {
        return this.bandwidthLimiter;
    }

    public long getUptimeSeconds() {
        return (System.nanoTime() - this.startTimeNanos) / VSSConstants.NANOS_PER_SECOND;
    }

    public OffThreadProcessor<?, ?> getOffThreadProcessor() {
        return this.offThreadProcessor;
    }

    public DirtyColumnTracker getDirtyTracker() {
        return this.dirtyTracker;
    }

    public String getTickDiagnostics() {
        return this.diag.format(VSSServerConfig.CONFIG.sendQueueLimitPerPlayer);
    }

    public long getWindowBandwidthRate() {
        return this.diag.getWindowBytesPerSecond();
    }

    public void shutdown() {
        try {
            this.offThreadProcessor.shutdown();
        } catch (Exception e) {
            VSSLogger.error("Error shutting down off-thread processor", e);
        }
        this.players.clear();
        try {
            this.diskReader.shutdown();
        } catch (Exception e2) {
            VSSLogger.error("Error shutting down disk reader", e2);
        }
        try {
            if (this.generationService != null) {
                this.generationService.shutdown();
            }
        } catch (Exception e3) {
            VSSLogger.error("Error shutting down generation service", e3);
        }
    }
}

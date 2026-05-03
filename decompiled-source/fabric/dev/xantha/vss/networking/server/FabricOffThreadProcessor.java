package dev.xantha.vss.networking.server;

import dev.xantha.vss.common.processing.OffThreadProcessor;
import dev.xantha.vss.networking.payloads.VoxelColumnS2CPayload;
import dev.xantha.vss.networking.server.ChunkDiskReader;
import dev.xantha.vss.networking.server.PlayerRequestState;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/FabricOffThreadProcessor.class */
public class FabricOffThreadProcessor extends OffThreadProcessor<PlayerRequestState, ChunkDiskReader.ReadResult> {
    private final ChunkDiskReader diskReader;
    private final ChunkGenerationService generationService;
    private final ConcurrentHashMap<String, ServerLevel> dimensionLevelMap;
    private final ConcurrentHashMap<String, ResourceKey<Level>> dimensionKeyCache;

    public FabricOffThreadProcessor(Map<UUID, PlayerRequestState> players, ChunkDiskReader diskReader, ChunkGenerationService generationService, Path dataDir, int perDimensionTimestampCacheSizeMB) {
        super(players, diskReader != null, generationService != null, dataDir, perDimensionTimestampCacheSizeMB);
        this.dimensionLevelMap = new ConcurrentHashMap<>();
        this.dimensionKeyCache = new ConcurrentHashMap<>();
        this.diskReader = diskReader;
        this.generationService = generationService;
    }

    public void updateDimensionContext(String dimension, ServerLevel level) {
        this.dimensionLevelMap.putIfAbsent(dimension, level);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public ChunkDiskReader.ReadResult pollDiskResult(PlayerRequestState state) {
        ConcurrentLinkedQueue<ChunkDiskReader.ReadResult> queue;
        if (this.diskReader == null || (queue = this.diskReader.getPlayerQueue(state.getPlayerUUID())) == null) {
            return null;
        }
        return queue.poll();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public ChunkDiskReader.ReadResult pollGenerationResult(PlayerRequestState state) {
        ConcurrentLinkedQueue<ChunkDiskReader.ReadResult> queue;
        if (this.generationService == null || (queue = this.generationService.getPlayerQueue(state.getPlayerUUID())) == null) {
            return null;
        }
        return queue.poll();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public void enqueueResultPayloads(PlayerRequestState state, ChunkDiskReader.ReadResult result) {
        if (result.sectionBytes() != null) {
            buildAndEnqueueColumnPayload(state, result.chunkX(), result.chunkZ(), result.dimension(), result.requestId(), result.columnTimestamp(), result.submissionOrder(), result.sectionBytes(), result.estimatedBytes());
        }
    }

    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    protected void submitDiskRead(UUID playerUuid, int requestId, String dimension, int cx, int cz, long submissionOrder) {
        ServerLevel level;
        if (this.diskReader == null || (level = this.dimensionLevelMap.get(dimension)) == null) {
            return;
        }
        this.diskReader.submitReadDirect(playerUuid, requestId, level, cx, cz, submissionOrder);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public void buildAndEnqueueColumnPayload(PlayerRequestState state, int cx, int cz, String dimension, int requestId, long columnTimestamp, long submissionOrder, byte[] sectionBytes, int estimatedBytes) {
        ResourceKey<Level> dimensionKey = this.dimensionKeyCache.computeIfAbsent(dimension, d -> {
            return ResourceKey.create(Registries.DIMENSION, Identifier.parse(d));
        });
        VoxelColumnS2CPayload payload = new VoxelColumnS2CPayload(requestId, cx, cz, dimensionKey, columnTimestamp, sectionBytes);
        state.addReadyPayload(new PlayerRequestState.QueuedPayload(payload, requestId, estimatedBytes, submissionOrder));
    }

    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public void shutdown() {
        super.shutdown();
        this.dimensionLevelMap.clear();
    }
}

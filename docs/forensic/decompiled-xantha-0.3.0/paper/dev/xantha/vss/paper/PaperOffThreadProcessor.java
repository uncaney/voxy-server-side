package dev.xantha.vss.paper;

import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.OffThreadProcessor;
import dev.xantha.vss.paper.PaperChunkDiskReader;
import dev.xantha.vss.paper.PaperPlayerRequestState;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.server.level.ServerLevel;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperOffThreadProcessor.class */
public class PaperOffThreadProcessor extends OffThreadProcessor<PaperPlayerRequestState, PaperChunkDiskReader.SimpleReadResult> {
    private final PaperChunkDiskReader diskReader;
    private final PaperChunkGenerationService generationService;
    private final ConcurrentHashMap<String, ServerLevel> dimensionLevelMap;

    public PaperOffThreadProcessor(Map<UUID, PaperPlayerRequestState> players, PaperChunkDiskReader diskReader, PaperChunkGenerationService generationService, Path dataDir, int perDimensionTimestampCacheSizeMB) {
        super(players, diskReader != null, generationService != null, dataDir, perDimensionTimestampCacheSizeMB);
        this.dimensionLevelMap = new ConcurrentHashMap<>();
        this.diskReader = diskReader;
        this.generationService = generationService;
    }

    public void updateDimensionContext(String dimension, ServerLevel level) {
        this.dimensionLevelMap.putIfAbsent(dimension, level);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public PaperChunkDiskReader.SimpleReadResult pollDiskResult(PaperPlayerRequestState state) {
        ConcurrentLinkedQueue<PaperChunkDiskReader.SimpleReadResult> queue;
        if (this.diskReader == null || (queue = this.diskReader.getPlayerQueue(state.getPlayerUUID())) == null) {
            return null;
        }
        return queue.poll();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public PaperChunkDiskReader.SimpleReadResult pollGenerationResult(PaperPlayerRequestState state) {
        ConcurrentLinkedQueue<PaperChunkDiskReader.SimpleReadResult> queue;
        if (this.generationService == null || (queue = this.generationService.getPlayerQueue(state.getPlayerUUID())) == null) {
            return null;
        }
        return queue.poll();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public void enqueueResultPayloads(PaperPlayerRequestState state, PaperChunkDiskReader.SimpleReadResult result) {
        if (result.sectionBytes() != null) {
            buildAndEnqueueColumnPayload(state, result.chunkX(), result.chunkZ(), result.dimension(), result.requestId(), result.columnTimestamp(), result.submissionOrder(), result.sectionBytes(), result.estimatedBytes());
        }
    }

    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    protected void submitDiskRead(UUID playerUuid, int requestId, String dimension, int cx, int cz, long submissionOrder) {
        if (this.diskReader == null) {
            return;
        }
        ServerLevel level = this.dimensionLevelMap.get(dimension);
        if (level == null) {
            VSSLogger.debug("No dimension context for " + dimension + ", skipping disk read for " + cx + "," + cz);
        } else {
            this.diskReader.submitReadDirect(playerUuid, requestId, level, cx, cz, submissionOrder);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public void buildAndEnqueueColumnPayload(PaperPlayerRequestState state, int cx, int cz, String dimension, int requestId, long columnTimestamp, long submissionOrder, byte[] sectionBytes, int estimatedBytes) {
        byte[] encoded = PaperPayloadHandler.encodeVoxelColumnPreEncoded(requestId, cx, cz, dimension, columnTimestamp, sectionBytes);
        state.addReadyPayload(new PaperPlayerRequestState.QueuedPayload(encoded, requestId, estimatedBytes, submissionOrder));
    }

    @Override // dev.xantha.vss.common.processing.OffThreadProcessor
    public void shutdown() {
        super.shutdown();
        this.dimensionLevelMap.clear();
    }
}

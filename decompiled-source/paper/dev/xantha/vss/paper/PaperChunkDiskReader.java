package dev.xantha.vss.paper;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.AbstractChunkDiskReader;
import dev.xantha.vss.common.processing.ReadResultAccess;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperChunkDiskReader.class */
public class PaperChunkDiskReader extends AbstractChunkDiskReader<SimpleReadResult> {

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult.class */
    public static final class SimpleReadResult extends Record implements ReadResultAccess {
        private final UUID playerUuid;
        private final int requestId;
        private final int chunkX;
        private final int chunkZ;
        private final byte[] sectionBytes;
        private final String dimension;
        private final int estimatedBytes;
        private final long columnTimestamp;
        private final boolean notFound;
        private final boolean saturated;
        private final long submissionOrder;

        public SimpleReadResult(UUID playerUuid, int requestId, int chunkX, int chunkZ, byte[] sectionBytes, String dimension, int estimatedBytes, long columnTimestamp, boolean notFound, boolean saturated, long submissionOrder) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.sectionBytes = sectionBytes;
            this.dimension = dimension;
            this.estimatedBytes = estimatedBytes;
            this.columnTimestamp = columnTimestamp;
            this.notFound = notFound;
            this.saturated = saturated;
            this.submissionOrder = submissionOrder;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, SimpleReadResult.class), SimpleReadResult.class, "playerUuid;requestId;chunkX;chunkZ;sectionBytes;dimension;estimatedBytes;columnTimestamp;notFound;saturated;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->chunkX:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->chunkZ:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->sectionBytes:[B", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->estimatedBytes:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->columnTimestamp:J", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->notFound:Z", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->saturated:Z", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, SimpleReadResult.class), SimpleReadResult.class, "playerUuid;requestId;chunkX;chunkZ;sectionBytes;dimension;estimatedBytes;columnTimestamp;notFound;saturated;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->chunkX:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->chunkZ:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->sectionBytes:[B", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->estimatedBytes:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->columnTimestamp:J", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->notFound:Z", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->saturated:Z", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, SimpleReadResult.class, Object.class), SimpleReadResult.class, "playerUuid;requestId;chunkX;chunkZ;sectionBytes;dimension;estimatedBytes;columnTimestamp;notFound;saturated;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->chunkX:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->chunkZ:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->sectionBytes:[B", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->estimatedBytes:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->columnTimestamp:J", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->notFound:Z", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->saturated:Z", "FIELD:Ldev/xantha/vss/paper/PaperChunkDiskReader$SimpleReadResult;->submissionOrder:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public UUID playerUuid() {
            return this.playerUuid;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public int requestId() {
            return this.requestId;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public int chunkX() {
            return this.chunkX;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public int chunkZ() {
            return this.chunkZ;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public byte[] sectionBytes() {
            return this.sectionBytes;
        }

        public String dimension() {
            return this.dimension;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public int estimatedBytes() {
            return this.estimatedBytes;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public long columnTimestamp() {
            return this.columnTimestamp;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public boolean notFound() {
            return this.notFound;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public boolean saturated() {
            return this.saturated;
        }

        @Override // dev.xantha.vss.common.processing.ReadResultAccess
        public long submissionOrder() {
            return this.submissionOrder;
        }
    }

    static SimpleReadResult emptyResult(UUID playerUuid, int requestId, int chunkX, int chunkZ, long submissionOrder) {
        return new SimpleReadResult(playerUuid, requestId, chunkX, chunkZ, null, null, 0, 0L, true, false, submissionOrder);
    }

    static SimpleReadResult saturatedResult(UUID playerUuid, int requestId, int chunkX, int chunkZ, long submissionOrder) {
        return new SimpleReadResult(playerUuid, requestId, chunkX, chunkZ, null, null, 0, 0L, false, true, submissionOrder);
    }

    public PaperChunkDiskReader(int threadCount) {
        super(threadCount);
    }

    public void submitReadDirect(UUID playerUuid, int requestId, ServerLevel level, int chunkX, int chunkZ, long submissionOrder) {
        if (isShutdown()) {
            return;
        }
        this.diag.recordSubmitted();
        ResourceKey<Level> dimension = level.dimension();
        RegistryAccess registryAccess = level.registryAccess();
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        try {
            this.executor.submit(() -> {
                if (isShutdown()) {
                    return;
                }
                try {
                    readChunkNbtAndSerialize(playerUuid, requestId, chunkMap, chunkX, chunkZ, dimension, registryAccess, submissionOrder);
                } catch (Exception e) {
                    VSSLogger.error("Failed to read chunk from disk at " + chunkX + ", " + chunkZ, e);
                    this.diag.recordError();
                    this.diag.recordCompleted(0L);
                    addResult(playerUuid, emptyResult(playerUuid, requestId, chunkX, chunkZ, submissionOrder));
                }
            });
        } catch (RejectedExecutionException e) {
            if (VSSLogger.isDebugEnabled()) {
                VSSLogger.debug("Disk reader executor saturated, returning rate-limited for " + chunkX + "," + chunkZ);
            }
            this.diag.recordSaturation();
            this.diag.recordCompleted(0L);
            addResult(playerUuid, saturatedResult(playerUuid, requestId, chunkX, chunkZ, submissionOrder));
        }
    }

    private void readChunkNbtAndSerialize(UUID playerUuid, int requestId, ChunkMap chunkMap, int chunkX, int chunkZ, ResourceKey<Level> dimension, RegistryAccess registryAccess, long submissionOrder) {
        if (isShutdown()) {
            return;
        }
        long startNs = System.nanoTime();
        try {
            byte[] serialized = PaperNbtSectionSerializer.readAndSerializeSections(chunkMap, registryAccess, chunkX, chunkZ);
            if (serialized == null) {
                this.diag.recordEmpty();
                this.diag.recordCompleted(System.nanoTime() - startNs);
                addResult(playerUuid, emptyResult(playerUuid, requestId, chunkX, chunkZ, submissionOrder));
            } else {
                if (serialized.length == 0) {
                    long columnTimestamp = VSSConstants.epochSeconds();
                    String dimensionStr = dimension.identifier().toString();
                    this.diag.recordEmpty();
                    this.diag.recordCompleted(System.nanoTime() - startNs);
                    addResult(playerUuid, new SimpleReadResult(playerUuid, requestId, chunkX, chunkZ, null, dimensionStr, 0, columnTimestamp, false, false, submissionOrder));
                    return;
                }
                long columnTimestamp2 = VSSConstants.epochSeconds();
                String dimensionStr2 = dimension.identifier().toString();
                int estimatedBytes = serialized.length + 25;
                this.diag.recordCompleted(System.nanoTime() - startNs);
                addResult(playerUuid, new SimpleReadResult(playerUuid, requestId, chunkX, chunkZ, serialized, dimensionStr2, estimatedBytes, columnTimestamp2, false, false, submissionOrder));
            }
        } catch (Exception e) {
            VSSLogger.error("Failed to read/serialize chunk at " + chunkX + ", " + chunkZ, e);
            this.diag.recordError();
            this.diag.recordCompleted(System.nanoTime() - startNs);
            addResult(playerUuid, emptyResult(playerUuid, requestId, chunkX, chunkZ, submissionOrder));
        }
    }
}

package dev.xantha.vss.networking.server;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.LoadedColumnData;
import dev.xantha.vss.common.processing.TickSnapshot;
import dev.xantha.vss.config.VSSServerConfig;
import dev.xantha.vss.networking.server.ChunkDiskReader;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/ChunkGenerationService.class */
public class ChunkGenerationService {
    private static final TicketType VSS_GEN_TICKET = new TicketType(0, 2);
    private final int maxConcurrent;
    private final int maxPerPlayerActive;
    private final int timeoutTicks;
    private final LinkedHashMap<PendingGenerationKey, PendingGeneration> active = new LinkedHashMap<>();
    private final Map<UUID, Integer> perPlayerActiveCount = new HashMap();
    private final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<ChunkDiskReader.ReadResult>> playerResults = new ConcurrentHashMap<>();
    private volatile long totalSubmitted = 0;
    private volatile long totalCompleted = 0;
    private volatile long totalTimeouts = 0;

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback.class */
    static final class GenerationCallback extends Record {
        private final UUID playerUuid;
        private final int requestId;
        private final long submissionOrder;

        GenerationCallback(UUID playerUuid, int requestId, long submissionOrder) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
            this.submissionOrder = submissionOrder;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, GenerationCallback.class), GenerationCallback.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->requestId:I", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, GenerationCallback.class), GenerationCallback.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->requestId:I", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, GenerationCallback.class, Object.class), GenerationCallback.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->requestId:I", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$GenerationCallback;->submissionOrder:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public UUID playerUuid() {
            return this.playerUuid;
        }

        public int requestId() {
            return this.requestId;
        }

        public long submissionOrder() {
            return this.submissionOrder;
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey.class */
    private static final class PendingGenerationKey extends Record {
        private final ResourceKey<Level> dimension;
        private final int cx;
        private final int cz;

        private PendingGenerationKey(ResourceKey<Level> dimension, int cx, int cz) {
            this.dimension = dimension;
            this.cx = cx;
            this.cz = cz;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, PendingGenerationKey.class), PendingGenerationKey.class, "dimension;cx;cz", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->dimension:Lnet/minecraft/resources/ResourceKey;", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->cx:I", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->cz:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, PendingGenerationKey.class), PendingGenerationKey.class, "dimension;cx;cz", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->dimension:Lnet/minecraft/resources/ResourceKey;", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->cx:I", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->cz:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, PendingGenerationKey.class, Object.class), PendingGenerationKey.class, "dimension;cx;cz", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->dimension:Lnet/minecraft/resources/ResourceKey;", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->cx:I", "FIELD:Ldev/xantha/vss/networking/server/ChunkGenerationService$PendingGenerationKey;->cz:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public ResourceKey<Level> dimension() {
            return this.dimension;
        }

        public int cx() {
            return this.cx;
        }

        public int cz() {
            return this.cz;
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/ChunkGenerationService$PendingGeneration.class */
    static class PendingGeneration {
        final ChunkPos pos;
        final ServerLevel level;
        final List<GenerationCallback> callbacks = new ArrayList();
        int ticksWaiting = 0;

        PendingGeneration(ChunkPos pos, ServerLevel level) {
            this.pos = pos;
            this.level = level;
        }
    }

    public ChunkGenerationService(VSSServerConfig config) {
        this.maxConcurrent = config.generationConcurrencyLimitGlobal;
        this.maxPerPlayerActive = config.generationConcurrencyLimitPerPlayer;
        this.timeoutTicks = config.generationTimeoutSeconds * 20;
    }

    public boolean submitGeneration(UUID playerUuid, int requestId, ServerLevel level, int cx, int cz, long submissionOrder) {
        PendingGenerationKey key = new PendingGenerationKey(level.dimension(), cx, cz);
        PendingGeneration existing = this.active.get(key);
        if (existing != null) {
            existing.callbacks.add(new GenerationCallback(playerUuid, requestId, submissionOrder));
            incrementCount(this.perPlayerActiveCount, playerUuid);
            return true;
        }
        int playerActive = this.perPlayerActiveCount.getOrDefault(playerUuid, 0).intValue();
        if (this.active.size() < this.maxConcurrent && playerActive < this.maxPerPlayerActive) {
            ChunkPos pos = new ChunkPos(cx, cz);
            level.getChunkSource().addTicketWithRadius(VSS_GEN_TICKET, pos, 0);
            PendingGeneration gen = new PendingGeneration(pos, level);
            gen.callbacks.add(new GenerationCallback(playerUuid, requestId, submissionOrder));
            this.active.put(key, gen);
            incrementCount(this.perPlayerActiveCount, playerUuid);
            this.totalSubmitted++;
            return true;
        }
        return false;
    }

    /* JADX WARN: Failed to analyze thrown exceptions
    java.util.ConcurrentModificationException
    	at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1096)
    	at java.base/java.util.ArrayList$Itr.next(ArrayList.java:1050)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.processInstructions(MethodThrowsVisitor.java:117)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.visit(MethodThrowsVisitor.java:68)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.checkInsn(MethodThrowsVisitor.java:178)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.processInstructions(MethodThrowsVisitor.java:131)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.visit(MethodThrowsVisitor.java:68)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.checkInsn(MethodThrowsVisitor.java:178)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.processInstructions(MethodThrowsVisitor.java:131)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.visit(MethodThrowsVisitor.java:68)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.checkInsn(MethodThrowsVisitor.java:178)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.processInstructions(MethodThrowsVisitor.java:131)
    	at jadx.core.dex.visitors.MethodThrowsVisitor.visit(MethodThrowsVisitor.java:68)
     */
    public List<TickSnapshot.GenerationReadyData> tick() {
        if (this.active.isEmpty()) {
            return List.of();
        }
        List<TickSnapshot.GenerationReadyData> ready = null;
        Iterator<Map.Entry<PendingGenerationKey, PendingGeneration>> iter = this.active.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<PendingGenerationKey, PendingGeneration> entry = iter.next();
            PendingGeneration gen = entry.getValue();
            gen.ticksWaiting++;
            if (gen.ticksWaiting > this.timeoutTicks) {
                VSSLogger.debug("Generation timeout for chunk " + gen.pos.x() + "," + gen.pos.z() + " after " + gen.ticksWaiting + " ticks (" + gen.callbacks.size() + " callbacks)");
                for (GenerationCallback cb : gen.callbacks) {
                    addResult(cb.playerUuid, ChunkDiskReader.emptyResult(cb.playerUuid, cb.requestId, gen.pos.x(), gen.pos.z(), cb.submissionOrder));
                    decrementCount(this.perPlayerActiveCount, cb.playerUuid);
                }
                gen.level.getChunkSource().removeTicketWithRadius(VSS_GEN_TICKET, gen.pos, 0);
                iter.remove();
                this.totalTimeouts++;
            } else {
                LevelChunk chunk = gen.level.getChunkSource().getChunkNow(gen.pos.x(), gen.pos.z());
                if (chunk != null) {
                    try {
                        long columnTimestamp = VSSConstants.epochSeconds();
                        LoadedColumnData columnData = SectionSerializer.serializeColumn(gen.level, chunk, gen.pos.x(), gen.pos.z());
                        for (GenerationCallback cb2 : gen.callbacks) {
                            if (ready == null) {
                                ready = new ArrayList<>();
                            }
                            ready.add(new TickSnapshot.GenerationReadyData(cb2.playerUuid, cb2.requestId, columnData, columnTimestamp, cb2.submissionOrder));
                            decrementCount(this.perPlayerActiveCount, cb2.playerUuid);
                        }
                        this.totalCompleted++;
                    } catch (Exception e) {
                        VSSLogger.error("Failed to extract primitives for generated chunk at " + gen.pos.x() + ", " + gen.pos.z(), e);
                        for (GenerationCallback cb3 : gen.callbacks) {
                            addResult(cb3.playerUuid, ChunkDiskReader.emptyResult(cb3.playerUuid, cb3.requestId, gen.pos.x(), gen.pos.z(), cb3.submissionOrder));
                            decrementCount(this.perPlayerActiveCount, cb3.playerUuid);
                        }
                    }
                    gen.level.getChunkSource().removeTicketWithRadius(VSS_GEN_TICKET, gen.pos, 0);
                    iter.remove();
                }
            }
        }
        return ready != null ? ready : List.of();
    }

    void registerPlayer(UUID playerUuid) {
        this.playerResults.computeIfAbsent(playerUuid, k -> {
            return new ConcurrentLinkedQueue();
        });
    }

    void addResult(UUID playerUuid, ChunkDiskReader.ReadResult result) {
        ConcurrentLinkedQueue<ChunkDiskReader.ReadResult> queue = this.playerResults.get(playerUuid);
        if (queue != null) {
            queue.add(result);
        }
    }

    public ConcurrentLinkedQueue<ChunkDiskReader.ReadResult> getPlayerQueue(UUID playerUuid) {
        return this.playerResults.get(playerUuid);
    }

    public void removePlayerResults(UUID playerUuid) {
        this.playerResults.remove(playerUuid);
    }

    public void removePlayer(UUID playerUuid) {
        removePlayerResults(playerUuid);
        this.perPlayerActiveCount.remove(playerUuid);
        Iterator<Map.Entry<PendingGenerationKey, PendingGeneration>> iter = this.active.entrySet().iterator();
        while (iter.hasNext()) {
            PendingGeneration gen = iter.next().getValue();
            gen.callbacks.removeIf(cb -> {
                return cb.playerUuid.equals(playerUuid);
            });
            if (gen.callbacks.isEmpty()) {
                gen.level.getChunkSource().removeTicketWithRadius(VSS_GEN_TICKET, gen.pos, 0);
                iter.remove();
            }
        }
    }

    public void shutdown() {
        for (PendingGeneration gen : this.active.values()) {
            gen.level.getChunkSource().removeTicketWithRadius(VSS_GEN_TICKET, gen.pos, 0);
        }
        this.active.clear();
        this.perPlayerActiveCount.clear();
        this.playerResults.clear();
    }

    public String getDiagnostics() {
        return String.format("submitted=%d, completed=%d, active=%d, timeouts=%d", Long.valueOf(this.totalSubmitted), Long.valueOf(this.totalCompleted), Integer.valueOf(this.active.size()), Long.valueOf(this.totalTimeouts));
    }

    public long getTotalSubmitted() {
        return this.totalSubmitted;
    }

    public long getTotalCompleted() {
        return this.totalCompleted;
    }

    public long getTotalTimeouts() {
        return this.totalTimeouts;
    }

    private static void incrementCount(Map<UUID, Integer> map, UUID uuid) {
        map.merge(uuid, 1, (v0, v1) -> {
            return Integer.sum(v0, v1);
        });
    }

    private static void decrementCount(Map<UUID, Integer> map, UUID uuid) {
        Integer count = map.get(uuid);
        if (count != null) {
            if (count.intValue() > 1) {
                map.put(uuid, Integer.valueOf(count.intValue() - 1));
            } else {
                map.remove(uuid);
            }
        }
    }
}

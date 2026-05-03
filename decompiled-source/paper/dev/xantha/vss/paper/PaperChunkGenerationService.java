package dev.xantha.vss.paper;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.LoadedColumnData;
import dev.xantha.vss.common.processing.TickSnapshot;
import dev.xantha.vss.paper.PaperChunkDiskReader;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperChunkGenerationService.class */
public class PaperChunkGenerationService {
    private final Plugin plugin;
    private final int maxConcurrent;
    private final int maxPerPlayerActive;
    private final int timeoutTicks;
    private final LinkedHashMap<PendingGenerationKey, ActiveGeneration> active = new LinkedHashMap<>();
    private final Map<UUID, Integer> perPlayerActiveCount = new HashMap();
    private final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<PaperChunkDiskReader.SimpleReadResult>> playerResults = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<TickSnapshot.GenerationReadyData> generationReadyQueue = new ConcurrentLinkedQueue<>();
    private volatile long totalSubmitted = 0;
    private volatile long totalCompleted = 0;
    private volatile long totalTimeouts = 0;

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback.class */
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
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, GenerationCallback.class), GenerationCallback.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, GenerationCallback.class), GenerationCallback.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, GenerationCallback.class, Object.class), GenerationCallback.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$GenerationCallback;->submissionOrder:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
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

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey.class */
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
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, PendingGenerationKey.class), PendingGenerationKey.class, "dimension;cx;cz", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->dimension:Lnet/minecraft/resources/ResourceKey;", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->cx:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->cz:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, PendingGenerationKey.class), PendingGenerationKey.class, "dimension;cx;cz", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->dimension:Lnet/minecraft/resources/ResourceKey;", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->cx:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->cz:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, PendingGenerationKey.class, Object.class), PendingGenerationKey.class, "dimension;cx;cz", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->dimension:Lnet/minecraft/resources/ResourceKey;", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->cx:I", "FIELD:Ldev/xantha/vss/paper/PaperChunkGenerationService$PendingGenerationKey;->cz:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
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

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperChunkGenerationService$ActiveGeneration.class */
    static class ActiveGeneration {
        final List<GenerationCallback> callbacks = new ArrayList();
        int ticksWaiting = 0;

        ActiveGeneration() {
        }
    }

    public PaperChunkGenerationService(PaperConfig config, Plugin plugin) {
        this.plugin = plugin;
        this.maxConcurrent = config.generationConcurrencyLimitGlobal;
        this.maxPerPlayerActive = config.generationConcurrencyLimitPerPlayer;
        this.timeoutTicks = config.generationTimeoutSeconds * 20;
    }

    public boolean submitGeneration(UUID playerUuid, int requestId, ServerLevel level, int cx, int cz, long submissionOrder) {
        PendingGenerationKey key = new PendingGenerationKey(level.dimension(), cx, cz);
        ActiveGeneration existingActive = this.active.get(key);
        if (existingActive != null) {
            existingActive.callbacks.add(new GenerationCallback(playerUuid, requestId, submissionOrder));
            incrementCount(this.perPlayerActiveCount, playerUuid);
            return true;
        }
        int playerActive = this.perPlayerActiveCount.getOrDefault(playerUuid, 0).intValue();
        if (this.active.size() < this.maxConcurrent && playerActive < this.maxPerPlayerActive) {
            ActiveGeneration gen = new ActiveGeneration();
            gen.callbacks.add(new GenerationCallback(playerUuid, requestId, submissionOrder));
            this.active.put(key, gen);
            incrementCount(this.perPlayerActiveCount, playerUuid);
            this.totalSubmitted++;
            launchAsyncLoad(key, level, cx, cz);
            return true;
        }
        return false;
    }

    private void launchAsyncLoad(PendingGenerationKey key, ServerLevel level, int cx, int cz) {
        level.getWorld().getChunkAtAsync(cx, cz, true, false).whenComplete((chunk, ex) -> {
            if (ex != null) {
                VSSLogger.error("Async chunk load failed at " + cx + "," + cz, ex);
            }
            Chunk readyChunk = ex == null ? chunk : null;
            try {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    onChunkReady(key, level, readyChunk, cx, cz);
                });
            } catch (Exception e) {
                VSSLogger.warn("Could not schedule generation callback (plugin shutting down) at " + cx + "," + cz);
            }
        });
    }

    private void onChunkReady(PendingGenerationKey key, ServerLevel level, Chunk chunk, int cx, int cz) {
        ActiveGeneration gen = this.active.remove(key);
        if (gen == null) {
            return;
        }
        if (chunk != null) {
            try {
                LevelChunk nmsChunk = level.getChunkSource().getChunkNow(cx, cz);
                if (nmsChunk != null) {
                    long columnTimestamp = VSSConstants.epochSeconds();
                    LoadedColumnData columnData = PaperSectionSerializer.serializeColumn(level, nmsChunk, cx, cz);
                    for (GenerationCallback cb : gen.callbacks) {
                        this.generationReadyQueue.add(new TickSnapshot.GenerationReadyData(cb.playerUuid, cb.requestId, columnData, columnTimestamp, cb.submissionOrder));
                        decrementCount(this.perPlayerActiveCount, cb.playerUuid);
                    }
                    this.totalCompleted++;
                } else {
                    VSSLogger.warn("Chunk at " + cx + "," + cz + " was null after async load completed");
                    emptyResultForCallbacks(gen.callbacks, cx, cz, this.perPlayerActiveCount);
                }
                return;
            } catch (Exception e) {
                VSSLogger.error("Failed to extract primitives for generated chunk at " + cx + ", " + cz, e);
                emptyResultForCallbacks(gen.callbacks, cx, cz, this.perPlayerActiveCount);
                return;
            }
        }
        emptyResultForCallbacks(gen.callbacks, cx, cz, this.perPlayerActiveCount);
    }

    private void emptyResultForCallbacks(List<GenerationCallback> callbacks, int cx, int cz, Map<UUID, Integer> countMap) {
        for (GenerationCallback cb : callbacks) {
            addResult(cb.playerUuid, PaperChunkDiskReader.emptyResult(cb.playerUuid, cb.requestId, cx, cz, cb.submissionOrder));
            decrementCount(countMap, cb.playerUuid);
        }
    }

    public List<TickSnapshot.GenerationReadyData> tick() {
        tickActiveTimeouts();
        List<TickSnapshot.GenerationReadyData> ready = null;
        while (true) {
            TickSnapshot.GenerationReadyData grd = this.generationReadyQueue.poll();
            if (grd == null) {
                break;
            }
            if (ready == null) {
                ready = new ArrayList<>();
            }
            ready.add(grd);
        }
        return ready != null ? ready : List.of();
    }

    private void tickActiveTimeouts() {
        if (this.active.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<PendingGenerationKey, ActiveGeneration>> iter = this.active.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<PendingGenerationKey, ActiveGeneration> entry = iter.next();
            ActiveGeneration gen = entry.getValue();
            gen.ticksWaiting++;
            if (gen.ticksWaiting > this.timeoutTicks) {
                emptyResultForCallbacks(gen.callbacks, entry.getKey().cx, entry.getKey().cz, this.perPlayerActiveCount);
                iter.remove();
                this.totalTimeouts++;
            }
        }
    }

    void registerPlayer(UUID playerUuid) {
        this.playerResults.computeIfAbsent(playerUuid, k -> {
            return new ConcurrentLinkedQueue();
        });
    }

    void addResult(UUID playerUuid, PaperChunkDiskReader.SimpleReadResult result) {
        ConcurrentLinkedQueue<PaperChunkDiskReader.SimpleReadResult> queue = this.playerResults.get(playerUuid);
        if (queue != null) {
            queue.add(result);
        }
    }

    public ConcurrentLinkedQueue<PaperChunkDiskReader.SimpleReadResult> getPlayerQueue(UUID playerUuid) {
        return this.playerResults.get(playerUuid);
    }

    public void removePlayerResults(UUID playerUuid) {
        this.playerResults.remove(playerUuid);
    }

    public void removePlayer(UUID playerUuid) {
        removePlayerResults(playerUuid);
        Iterator<Map.Entry<PendingGenerationKey, ActiveGeneration>> activeIter = this.active.entrySet().iterator();
        while (activeIter.hasNext()) {
            ActiveGeneration gen = activeIter.next().getValue();
            gen.callbacks.removeIf(cb -> {
                return cb.playerUuid.equals(playerUuid);
            });
            if (gen.callbacks.isEmpty()) {
                activeIter.remove();
            }
        }
        this.perPlayerActiveCount.remove(playerUuid);
    }

    public void shutdown() {
        this.active.clear();
        this.perPlayerActiveCount.clear();
        this.playerResults.clear();
    }

    public String getDiagnostics() {
        return String.format("submitted=%d, completed=%d, active=%d, timeouts=%d", Long.valueOf(this.totalSubmitted), Long.valueOf(this.totalCompleted), Integer.valueOf(this.active.size()), Long.valueOf(this.totalTimeouts));
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

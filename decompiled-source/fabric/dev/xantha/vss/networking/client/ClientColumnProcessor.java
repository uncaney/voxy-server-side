package dev.xantha.vss.networking.client;

import dev.xantha.vss.api.VSSApi;
import dev.xantha.vss.api.VoxelColumnData;
import dev.xantha.vss.config.VSSClientConfig;
import dev.xantha.vss.networking.payloads.VoxelColumnS2CPayload;
import io.netty.buffer.Unpooled;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/ClientColumnProcessor.class */
class ClientColumnProcessor {
    static final int MAX_QUEUED_COLUMNS = 8000;
    private static final long DROP_WARN_INTERVAL_MS = 5000;
    private static final int MAX_SECTIONS_PER_COLUMN = 64;
    private volatile boolean shuttingDown;
    private final ConcurrentLinkedQueue<VoxelColumnS2CPayload> columnQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger queueSize = new AtomicInteger();
    private final AtomicLong columnsDropped = new AtomicLong();
    private volatile long lastDropWarnMs = 0;
    private volatile ExecutorService executor = createExecutor();
    private final AtomicBoolean processing = new AtomicBoolean();
    private volatile int chunksValidated = 0;

    ClientColumnProcessor() {
    }

    private static ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "VSS-ColumnProcessor");
            t.setDaemon(true);
            return t;
        });
    }

    void offer(VoxelColumnS2CPayload payload) {
        if (this.shuttingDown) {
            return;
        }
        if (this.queueSize.get() < MAX_QUEUED_COLUMNS) {
            this.columnQueue.add(payload);
            this.queueSize.incrementAndGet();
            return;
        }
        this.columnsDropped.incrementAndGet();
        long now = System.currentTimeMillis();
        if (now - this.lastDropWarnMs > DROP_WARN_INTERVAL_MS) {
            this.lastDropWarnMs = now;
        }
    }

    void scheduleProcessing(boolean serverEnabled) {
        if (this.shuttingDown) {
            return;
        }
        if (!serverEnabled || !VSSClientConfig.CONFIG.receiveServerLods || !VSSApi.hasVoxelConsumers()) {
            this.columnQueue.clear();
            this.queueSize.set(0);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            this.columnQueue.clear();
            this.queueSize.set(0);
        } else {
            if (this.columnQueue.isEmpty()) {
                return;
            }
            if (VSSClientConfig.CONFIG.offThreadSectionProcessing) {
                if (this.processing.compareAndSet(false, true)) {
                    try {
                        this.executor.execute(() -> {
                            try {
                                drainColumnQueue(level);
                            } finally {
                                this.processing.set(false);
                            }
                        });
                        return;
                    } catch (Exception e) {
                        this.processing.set(false);
                        return;
                    }
                }
                return;
            }
            drainColumnQueue(level);
        }
    }

    private void drainColumnQueue(ClientLevel level) {
        VoxelColumnS2CPayload payload;
        byte[] decompressed;
        PalettedContainerFactory factory = PalettedContainerFactory.create(level.registryAccess());
        while (!Thread.currentThread().isInterrupted() && (payload = this.columnQueue.poll()) != null) {
            this.queueSize.decrementAndGet();
            if (level.dimension().equals(payload.dimension()) && (decompressed = payload.decompressedSections()) != null && decompressed.length != 0) {
                try {
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(decompressed));
                    try {
                        int sectionCount = Math.max(0, Math.min(buf.readVarInt(), 64));
                        VoxelColumnData.SectionData[] sectionDatas = new VoxelColumnData.SectionData[sectionCount];
                        for (int i = 0; i < sectionCount; i++) {
                            int sectionY = buf.readByte();
                            LevelChunkSection section = new LevelChunkSection(factory);
                            section.read(buf);
                            if (this.chunksValidated < 50) {
                                PalettedContainer<BlockState> states = section.getStates();
                                for (int y = 0; y < 16; y++) {
                                    for (int z = 0; z < 16; z++) {
                                        for (int x = 0; x < 16; x++) {
                                            states.get(x, y, z);
                                        }
                                    }
                                }
                            }
                            DataLayer blockLight = null;
                            if (buf.readBoolean()) {
                                byte[] lightBytes = new byte[2048];
                                buf.readBytes(lightBytes);
                                blockLight = new DataLayer(lightBytes);
                            }
                            DataLayer skyLight = null;
                            if (buf.readBoolean()) {
                                byte[] lightBytes2 = new byte[2048];
                                buf.readBytes(lightBytes2);
                                skyLight = new DataLayer(lightBytes2);
                            }
                            sectionDatas[i] = new VoxelColumnData.SectionData(sectionY, section, blockLight, skyLight);
                        }
                        if (this.chunksValidated < 50) {
                            this.chunksValidated++;
                        }
                        VoxelColumnData columnData = new VoxelColumnData(sectionDatas, payload.columnTimestamp());
                        VSSApi.dispatchColumn(level, payload.dimension(), payload.chunkX(), payload.chunkZ(), columnData);
                        buf.release();
                    } catch (Throwable th) {
                        buf.release();
                        throw th;
                    }
                } catch (Exception e) {
                    VSSClientNetworking.emergencyDisable("ViaVersion mismatch detected. Silently aborting LOD stream.");
                    this.columnQueue.clear();
                    this.queueSize.set(0);
                    return;
                }
            }
        }
    }

    void shutdown() {
        this.shuttingDown = true;
        ExecutorService old = this.executor;
        old.shutdownNow();
        this.columnQueue.clear();
        this.queueSize.set(0);
        this.chunksValidated = 0;
        try {
            old.awaitTermination(2L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.processing.set(false);
        this.executor = createExecutor();
        this.shuttingDown = false;
    }

    int getQueuedCount() {
        return this.queueSize.get();
    }

    long getColumnsDropped() {
        return this.columnsDropped.get();
    }

    void resetStats() {
        this.columnsDropped.set(0L);
        this.lastDropWarnMs = 0L;
        this.chunksValidated = 0;
    }
}

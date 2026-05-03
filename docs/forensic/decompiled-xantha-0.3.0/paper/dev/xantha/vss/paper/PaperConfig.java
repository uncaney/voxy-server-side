package dev.xantha.vss.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperConfig.class */
public class PaperConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "vss-server-config.json";
    public boolean enabled = true;
    public int lodDistanceChunks = 256;
    public int bytesPerSecondLimitPerPlayer = 20971520;
    public int diskReaderThreads = 5;
    public int sendQueueLimitPerPlayer = 4000;
    public int bytesPerSecondLimitGlobal = VSSConstants.MAX_BYTES_PER_SECOND_PER_PLAYER;
    public boolean enableChunkGeneration = true;
    public int generationConcurrencyLimitGlobal = 32;
    public int generationTimeoutSeconds = 60;
    public int syncOnLoadRateLimitPerPlayer = 800;
    public int syncOnLoadConcurrencyLimitPerPlayer = 200;
    public int generationRateLimitPerPlayer = 80;
    public int generationConcurrencyLimitPerPlayer = 16;
    public int perDimensionTimestampCacheSizeMB = 32;
    public int dirtyBroadcastIntervalSeconds = 10;
    public List<String> updateEvents = List.of("org.bukkit.event.block.BlockPlaceEvent", "org.bukkit.event.block.BlockBreakEvent", "org.bukkit.event.block.BlockExplodeEvent", "org.bukkit.event.block.BlockPistonExtendEvent", "org.bukkit.event.block.BlockPistonRetractEvent", "org.bukkit.event.world.StructureGrowEvent", "org.bukkit.event.world.ChunkPopulateEvent");

    public void validate() {
        this.lodDistanceChunks = Math.clamp(this.lodDistanceChunks, 1, VSSConstants.MAX_LOD_DISTANCE);
        this.bytesPerSecondLimitPerPlayer = Math.clamp(this.bytesPerSecondLimitPerPlayer, 1024, VSSConstants.MAX_BYTES_PER_SECOND_PER_PLAYER);
        this.diskReaderThreads = Math.clamp(this.diskReaderThreads, 1, 64);
        this.sendQueueLimitPerPlayer = Math.clamp(this.sendQueueLimitPerPlayer, 1, VSSConstants.MAX_SEND_QUEUE_SIZE);
        this.bytesPerSecondLimitGlobal = (int) Math.clamp(this.bytesPerSecondLimitGlobal, 1024L, VSSConstants.MAX_BYTES_PER_SECOND_GLOBAL_LIMIT);
        this.generationConcurrencyLimitGlobal = Math.clamp(this.generationConcurrencyLimitGlobal, 1, 256);
        this.generationTimeoutSeconds = Math.clamp(this.generationTimeoutSeconds, 1, VSSConstants.MAX_GENERATION_TIMEOUT);
        this.syncOnLoadRateLimitPerPlayer = Math.clamp(this.syncOnLoadRateLimitPerPlayer, 1, 1000);
        this.syncOnLoadConcurrencyLimitPerPlayer = Math.clamp(this.syncOnLoadConcurrencyLimitPerPlayer, 1, 1000);
        this.generationRateLimitPerPlayer = Math.clamp(this.generationRateLimitPerPlayer, 1, 1000);
        this.generationConcurrencyLimitPerPlayer = Math.clamp(this.generationConcurrencyLimitPerPlayer, 1, 1000);
        this.perDimensionTimestampCacheSizeMB = Math.clamp(this.perDimensionTimestampCacheSizeMB, 1, 256);
        this.dirtyBroadcastIntervalSeconds = Math.clamp(this.dirtyBroadcastIntervalSeconds, 1, VSSConstants.MAX_DIRTY_BROADCAST_INTERVAL);
        if (this.updateEvents == null) {
            this.updateEvents = List.of();
        }
    }

    public void save(Path dataFolder) {
        try {
            Files.createDirectories(dataFolder, new FileAttribute[0]);
            Files.writeString(dataFolder.resolve(FILE_NAME), GSON.toJson(this), new OpenOption[0]);
        } catch (Exception e) {
            VSSLogger.error("Failed to save config vss-server-config.json", e);
        }
    }

    public static PaperConfig load(Path dataFolder) {
        Path path = dataFolder.resolve(FILE_NAME);
        boolean fileExists = Files.isRegularFile(path, new LinkOption[0]);
        if (fileExists) {
            try {
                String json = Files.readString(path);
                PaperConfig config = (PaperConfig) GSON.fromJson(json, PaperConfig.class);
                if (config != null) {
                    config.validate();
                    config.save(dataFolder);
                    return config;
                }
                VSSLogger.warn("Config vss-server-config.json was empty or invalid, using defaults");
            } catch (Exception e) {
                VSSLogger.error("Failed to read config vss-server-config.json, using defaults", e);
            }
        }
        PaperConfig config2 = new PaperConfig();
        config2.validate();
        if (!fileExists) {
            config2.save(dataFolder);
        }
        return config2;
    }
}

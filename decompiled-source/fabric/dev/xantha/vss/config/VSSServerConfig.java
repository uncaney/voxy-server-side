package dev.xantha.vss.config;

import dev.xantha.vss.common.VSSConstants;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/config/VSSServerConfig.class */
public class VSSServerConfig extends JsonConfig {
    private static final String FILE_NAME = "vss-server-config.json";
    public static final VSSServerConfig CONFIG = (VSSServerConfig) load(VSSServerConfig.class, FILE_NAME);
    public boolean enabled = true;
    public int lodDistanceChunks = 256;
    public int bytesPerSecondLimitPerPlayer = 20971520;
    public int diskReaderThreads = 5;
    public int sendQueueLimitPerPlayer = 4000;
    public int bytesPerSecondLimitGlobal = VSSConstants.MAX_BYTES_PER_SECOND_PER_PLAYER;
    public boolean enableChunkGeneration = true;
    public int generationConcurrencyLimitGlobal = 32;
    public int generationTimeoutSeconds = 60;
    public int dirtyBroadcastIntervalSeconds = 10;
    public int syncOnLoadRateLimitPerPlayer = 800;
    public int syncOnLoadConcurrencyLimitPerPlayer = 200;
    public int generationRateLimitPerPlayer = 80;
    public int generationConcurrencyLimitPerPlayer = 16;
    public int perDimensionTimestampCacheSizeMB = 32;

    @Override // dev.xantha.vss.config.JsonConfig
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override // dev.xantha.vss.config.JsonConfig
    protected void validate() {
        this.lodDistanceChunks = Math.clamp(this.lodDistanceChunks, 1, VSSConstants.MAX_LOD_DISTANCE);
        this.bytesPerSecondLimitPerPlayer = Math.clamp(this.bytesPerSecondLimitPerPlayer, 1024, VSSConstants.MAX_BYTES_PER_SECOND_PER_PLAYER);
        this.diskReaderThreads = Math.clamp(this.diskReaderThreads, 1, 64);
        this.sendQueueLimitPerPlayer = Math.clamp(this.sendQueueLimitPerPlayer, 1, VSSConstants.MAX_SEND_QUEUE_SIZE);
        this.bytesPerSecondLimitGlobal = (int) Math.clamp(this.bytesPerSecondLimitGlobal, 1024L, VSSConstants.MAX_BYTES_PER_SECOND_GLOBAL_LIMIT);
        this.generationConcurrencyLimitGlobal = Math.clamp(this.generationConcurrencyLimitGlobal, 1, 256);
        this.generationTimeoutSeconds = Math.clamp(this.generationTimeoutSeconds, 1, VSSConstants.MAX_GENERATION_TIMEOUT);
        this.dirtyBroadcastIntervalSeconds = Math.clamp(this.dirtyBroadcastIntervalSeconds, 1, VSSConstants.MAX_DIRTY_BROADCAST_INTERVAL);
        this.syncOnLoadRateLimitPerPlayer = Math.clamp(this.syncOnLoadRateLimitPerPlayer, 1, 1000);
        this.syncOnLoadConcurrencyLimitPerPlayer = Math.clamp(this.syncOnLoadConcurrencyLimitPerPlayer, 1, 1000);
        this.generationRateLimitPerPlayer = Math.clamp(this.generationRateLimitPerPlayer, 1, 1000);
        this.generationConcurrencyLimitPerPlayer = Math.clamp(this.generationConcurrencyLimitPerPlayer, 1, 1000);
        this.perDimensionTimestampCacheSizeMB = Math.clamp(this.perDimensionTimestampCacheSizeMB, 1, 256);
    }
}

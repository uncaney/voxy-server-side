package dev.xantha.vss.config;

import dev.xantha.vss.common.VSSConstants;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/config/VSSClientConfig.class */
public class VSSClientConfig extends JsonConfig {
    private static final String FILE_NAME = "vss-client-config.json";
    public static VSSClientConfig CONFIG = (VSSClientConfig) load(VSSClientConfig.class, FILE_NAME);
    public boolean receiveServerLods = true;
    public int lodDistanceChunks = 0;
    public boolean offThreadSectionProcessing = true;

    @Override // dev.xantha.vss.config.JsonConfig
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override // dev.xantha.vss.config.JsonConfig
    protected void validate() {
        this.lodDistanceChunks = Math.clamp(this.lodDistanceChunks, 0, VSSConstants.MAX_LOD_DISTANCE);
    }
}

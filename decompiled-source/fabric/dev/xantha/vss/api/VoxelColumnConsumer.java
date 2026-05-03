package dev.xantha.vss.api;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/api/VoxelColumnConsumer.class */
@FunctionalInterface
public interface VoxelColumnConsumer {
    void onVoxelColumnReceived(ClientLevel clientLevel, ResourceKey<Level> resourceKey, int i, int i2, VoxelColumnData voxelColumnData);
}

package dev.xantha.vss.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/mixin/AccessorServerChunkCache.class */
@Mixin({ServerChunkCache.class})
public interface AccessorServerChunkCache {
    @Accessor
    ChunkMap getChunkMap();
}

package dev.xantha.vss.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/mixin/AccessorChunkMap.class */
@Mixin({ChunkMap.class})
public interface AccessorChunkMap {
    @Accessor
    ServerLevel getLevel();
}

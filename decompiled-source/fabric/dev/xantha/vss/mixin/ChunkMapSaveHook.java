package dev.xantha.vss.mixin;

import dev.xantha.vss.networking.server.RequestProcessingService;
import dev.xantha.vss.networking.server.VSSServerNetworking;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/mixin/ChunkMapSaveHook.class */
@Mixin({ChunkMap.class})
public class ChunkMapSaveHook {

    @Unique
    private String vss$cachedDimension;

    /* JADX WARN: Multi-variable type inference failed */
    @Inject(method = {"save"}, at = {@At("RETURN")})
    private void vss$onChunkSaved(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir) {
        RequestProcessingService service;
        if (((Boolean) cir.getReturnValue()).booleanValue() && (service = VSSServerNetworking.getRequestService()) != null) {
            if (this.vss$cachedDimension == null) {
                ServerLevel level = ((AccessorChunkMap) this).getLevel();
                this.vss$cachedDimension = level.dimension().identifier().toString();
            }
            service.getDirtyTracker().markDirty(this.vss$cachedDimension, chunk.getPos().x(), chunk.getPos().z());
        }
    }
}

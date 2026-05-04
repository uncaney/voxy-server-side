package dev.vox.lss.mixin;

import dev.vox.lss.networking.server.LSSServerNetworking;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public class ChunkMapSaveHook {
    // Defensive: chunks save on the main server thread today, but a future MC
    // change or a mod calling save() from a different thread would expose a
    // visibility race on this cached field. Volatile is the cheap belt.
    @Unique
    private volatile String lss$cachedDimension;

    @Inject(method = "save", at = @At("RETURN"))
    private void lss$onChunkSaved(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            var service = LSSServerNetworking.getRequestService();
            if (service != null) {
                if (this.lss$cachedDimension == null) {
                    ServerLevel level = ((AccessorChunkMap) (Object) this).getLevel();
                    this.lss$cachedDimension = level.dimension().identifier().toString();
                }
                service.getDirtyTracker().markDirty(this.lss$cachedDimension, chunk.getPos().x(), chunk.getPos().z());
            }
        }
    }
}

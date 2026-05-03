package dev.xantha.vss.mixin;

import dev.xantha.vss.networking.server.VSSServerNetworking;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/mixin/IntegratedServerLanHook.class */
@Mixin({IntegratedServer.class})
public class IntegratedServerLanHook {
    @Inject(method = {"publishServer"}, at = {@At("RETURN")})
    private void vss$onLanPublished(GameType gameType, boolean allowCheats, int port, CallbackInfoReturnable<Boolean> cir) {
        if (((Boolean) cir.getReturnValue()).booleanValue()) {
            VSSServerNetworking.startServiceForLan((IntegratedServer) this);
        }
    }
}

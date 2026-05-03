package dev.xantha.vss;

import dev.xantha.vss.benchmark.BenchmarkHook;
import dev.xantha.vss.networking.VSSNetworking;
import dev.xantha.vss.networking.server.VSSServerNetworking;
import net.fabricmc.api.ModInitializer;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/VSSMod.class */
public class VSSMod implements ModInitializer {
    public void onInitialize() {
        VSSNetworking.registerPayloads();
        VSSServerNetworking.init();
        BenchmarkHook.initServer();
    }
}

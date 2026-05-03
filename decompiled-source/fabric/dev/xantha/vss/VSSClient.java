package dev.xantha.vss;

import dev.xantha.vss.benchmark.BenchmarkHook;
import dev.xantha.vss.compat.ModCompat;
import dev.xantha.vss.networking.client.VSSClientCommands;
import dev.xantha.vss.networking.client.VSSClientNetworking;
import net.fabricmc.api.ClientModInitializer;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/VSSClient.class */
public class VSSClient implements ClientModInitializer {
    public void onInitializeClient() {
        VSSClientNetworking.init();
        VSSClientCommands.init();
        ModCompat.init();
        BenchmarkHook.initClient();
    }
}

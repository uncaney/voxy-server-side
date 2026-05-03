package dev.xantha.vss.compat;

import java.util.OptionalInt;
import net.fabricmc.loader.api.FabricLoader;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/compat/ModCompat.class */
public final class ModCompat {
    private static boolean voxyLoaded;

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("voxy")) {
            voxyLoaded = VoxyCompat.init();
        }
    }

    public static OptionalInt getVoxyViewDistanceChunks() {
        return !voxyLoaded ? OptionalInt.empty() : VoxyCompat.getViewDistanceChunks();
    }
}

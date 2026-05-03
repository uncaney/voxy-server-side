package dev.xantha.vss.benchmark;

import dev.xantha.vss.api.VSSApi;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.networking.client.VSSClientNetworking;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/benchmark/BenchmarkHook.class */
public final class BenchmarkHook {
    private static final boolean ENABLED = Boolean.getBoolean("vss.benchmark");
    private static final int DURATION_SECONDS = Integer.getInteger("vss.benchmark.duration", 60).intValue();
    private static volatile Map<String, Object> latestClientSnapshot;

    private BenchmarkHook() {
    }

    public static void initServer() {
        if (ENABLED) {
            VSSLogger.info("[Benchmark] Server hook active, duration=" + DURATION_SECONDS + "s");
            int targetTicks = DURATION_SECONDS * 20;
            int[] tickCount = {0};
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                VSSLogger.info("[Benchmark] Server started, counting " + targetTicks + " ticks");
                tickCount[0] = 0;
            });
            ServerTickEvents.END_SERVER_TICK.register(server2 -> {
                tickCount[0] = tickCount[0] + 1;
                if (tickCount[0] == targetTicks) {
                    VSSLogger.info("[Benchmark] Duration reached (" + DURATION_SECONDS + "s), exporting metrics");
                    Path outputFile = Path.of("benchmark-results", "server.json");
                    BenchmarkMetricsExporter.exportServer(outputFile, DURATION_SECONDS);
                    VSSLogger.info("[Benchmark] Halting server");
                    server2.halt(false);
                }
            });
        }
    }

    public static void initClient() {
        if (ENABLED) {
            VSSLogger.info("[Benchmark] Client hook active");
            VSSApi.registerColumnConsumer((level, dimension, chunkX, chunkZ, columnData) -> {
            });
            int[] clientTick = {0};
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                clientTick[0] = clientTick[0] + 1;
                if (clientTick[0] % 20 == 0 && VSSClientNetworking.isServerEnabled()) {
                    latestClientSnapshot = BenchmarkMetricsExporter.buildClientMetrics();
                }
            });
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client2) -> {
                VSSLogger.info("[Benchmark] Client disconnected, exporting metrics");
                Path outputFile = Path.of("benchmark-results", "client.json");
                Map<String, Object> snapshot = latestClientSnapshot;
                if (snapshot != null) {
                    BenchmarkMetricsExporter.writeClientSnapshot(outputFile, snapshot);
                } else {
                    BenchmarkMetricsExporter.exportClient(outputFile);
                }
                VSSLogger.info("[Benchmark] Exiting client");
                Runtime.getRuntime().halt(0);
            });
        }
    }
}

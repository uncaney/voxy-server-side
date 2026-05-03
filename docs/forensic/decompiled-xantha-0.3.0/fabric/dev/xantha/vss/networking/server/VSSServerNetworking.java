package dev.xantha.vss.networking.server;

import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.config.VSSServerConfig;
import dev.xantha.vss.networking.client.VSSClientNetworking;
import dev.xantha.vss.networking.payloads.BandwidthUpdateC2SPayload;
import dev.xantha.vss.networking.payloads.BatchChunkRequestC2SPayload;
import dev.xantha.vss.networking.payloads.CancelRequestC2SPayload;
import dev.xantha.vss.networking.payloads.HandshakeC2SPayload;
import dev.xantha.vss.networking.payloads.SessionConfigS2CPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/VSSServerNetworking.class */
public class VSSServerNetworking {
    private static volatile RequestProcessingService requestService;

    public static RequestProcessingService getRequestService() {
        return requestService;
    }

    public static synchronized void startServiceForLan(MinecraftServer server) {
        if (requestService != null) {
            return;
        }
        VSSLogger.info("Starting VSS LOD request processing service (LAN server)");
        requestService = new RequestProcessingService(server);
        VSSClientNetworking.triggerHostHandshake();
    }

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(HandshakeC2SPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            VSSLogger.info("VSS handshake received from " + player.getName().getString() + " (protocol v" + payload.protocolVersion() + ", capabilities=" + payload.capabilities() + ")");
            VSSServerConfig config = VSSServerConfig.CONFIG;
            RequestProcessingService service = requestService;
            boolean effectiveEnabled = config.enabled && service != null;
            ServerPlayNetworking.send(player, new SessionConfigS2CPayload(15, effectiveEnabled, config.lodDistanceChunks, 1, config.syncOnLoadRateLimitPerPlayer, config.syncOnLoadConcurrencyLimitPerPlayer, config.generationRateLimitPerPlayer, config.generationConcurrencyLimitPerPlayer, config.enableChunkGeneration, config.bytesPerSecondLimitPerPlayer));
            if (payload.protocolVersion() != 15) {
                VSSLogger.warn("Player " + player.getName().getString() + " has incompatible VSS protocol version " + payload.protocolVersion() + " (server: 15), skipping LOD distribution");
            } else if (effectiveEnabled) {
                service.registerPlayer(player, payload.capabilities());
                VSSLogger.info("Player " + player.getName().getString() + " registered for VSS LOD request processing" + (payload.capabilities() != 0 ? " (caps=" + payload.capabilities() + ")" : ""));
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(BatchChunkRequestC2SPayload.TYPE, (payload2, context2) -> {
            RequestProcessingService service = requestService;
            if (service != null) {
                service.handleBatchRequest(context2.player(), payload2);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(CancelRequestC2SPayload.TYPE, (payload3, context3) -> {
            RequestProcessingService service = requestService;
            if (service != null) {
                service.handleCancel(context3.player(), payload3);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(BandwidthUpdateC2SPayload.TYPE, (payload4, context4) -> {
            RequestProcessingService service = requestService;
            if (service != null) {
                service.handleBandwidthUpdate(context4.player(), payload4);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!server.isDedicatedServer() && !Boolean.getBoolean("vss.test.integratedServer")) {
                VSSLogger.info("VSS LOD request processing deferred until LAN");
            } else {
                VSSLogger.info("Starting VSS LOD request processing service");
                requestService = new RequestProcessingService(server);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server2 -> {
            RequestProcessingService service = requestService;
            if (service != null) {
                VSSLogger.info("Stopping VSS LOD request processing service");
                service.shutdown();
                requestService = null;
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server3 -> {
            RequestProcessingService service = requestService;
            if (service != null) {
                service.tick();
            }
        });
        VSSServerCommands.init();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server4) -> {
            RequestProcessingService service = requestService;
            if (service != null) {
                service.removePlayer(handler.getPlayer().getUUID());
            }
        });
    }
}

package dev.xantha.vss.networking.client;

import dev.xantha.vss.api.VSSApi;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.config.VSSClientConfig;
import dev.xantha.vss.networking.payloads.BatchResponseS2CPayload;
import dev.xantha.vss.networking.payloads.DirtyColumnsS2CPayload;
import dev.xantha.vss.networking.payloads.HandshakeC2SPayload;
import dev.xantha.vss.networking.payloads.SessionConfigS2CPayload;
import dev.xantha.vss.networking.payloads.VoxelColumnS2CPayload;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/VSSClientNetworking.class */
public class VSSClientNetworking {
    private static volatile LodRequestManager requestManager;
    private static volatile boolean serverEnabled = false;
    private static volatile int serverLodDistance = 0;
    private static final AtomicLong columnsReceived = new AtomicLong();
    private static final AtomicLong bytesReceived = new AtomicLong();
    private static volatile long connectionStartMs = 0;
    public static volatile boolean versionMismatchFlag = false;
    private static final ClientColumnProcessor columnProcessor = new ClientColumnProcessor();

    public static boolean isServerEnabled() {
        return serverEnabled;
    }

    public static void emergencyDisable(String reason) {
        if (serverEnabled) {
            VSSLogger.warn("EMERGENCY VSS DISABLE: " + reason);
            serverEnabled = false;
            versionMismatchFlag = true;
            LodRequestManager manager = requestManager;
            if (manager != null) {
                manager.disconnect();
                requestManager = null;
            }
            columnProcessor.shutdown();
            columnProcessor.resetStats();
        }
    }

    private static boolean isVersionMismatch() {
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        if (serverData != null) {
            int clientProtocol = SharedConstants.getProtocolVersion();
            if (serverData.protocol != 0 && serverData.protocol != clientProtocol) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static int getServerLodDistance() {
        return serverLodDistance;
    }

    public static long getColumnsReceived() {
        return columnsReceived.get();
    }

    public static long getBytesReceived() {
        return bytesReceived.get();
    }

    public static long getColumnsDropped() {
        return columnProcessor.getColumnsDropped();
    }

    public static long getConnectionStartMs() {
        return connectionStartMs;
    }

    public static LodRequestManager getRequestManager() {
        return requestManager;
    }

    public static int getQueuedColumnCount() {
        return columnProcessor.getQueuedCount();
    }

    public static void triggerHostHandshake() {
        Minecraft.getInstance().execute(() -> {
            if (VSSClientConfig.CONFIG.receiveServerLods && requestManager == null) {
                if (isVersionMismatch()) {
                    VSSLogger.warn("Minecraft version mismatch detected. Aborting LAN VSS handshake.");
                    return;
                }
                try {
                    int clientCaps = VSSApi.hasVoxelConsumers() ? 1 : 0;
                    ClientPlayNetworking.send(new HandshakeC2SPayload(15, clientCaps));
                } catch (Exception e) {
                    VSSLogger.debug("LAN host handshake send failed: " + e.getMessage());
                }
            }
        });
    }

    public static void init() {
        registerPacketHandlers();
        registerConnectionLifecycle();
        registerTickHandler();
    }

    private static void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SessionConfigS2CPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                String serverAddr;
                VSSLogger.info("Server session config received (protocol v" + payload.protocolVersion() + ", LOD distance: " + payload.lodDistanceChunks() + " chunks, enabled: " + payload.enabled() + ", syncRate: " + payload.syncOnLoadRateLimitPerPlayer() + ")");
                if (payload.protocolVersion() != 15) {
                    serverEnabled = false;
                    return;
                }
                if (isVersionMismatch()) {
                    VSSLogger.warn("Server list version mismatch detected. Disabling LODs.");
                    serverEnabled = false;
                    return;
                }
                serverEnabled = payload.enabled();
                serverLodDistance = payload.lodDistanceChunks();
                if (payload.enabled()) {
                    connectionStartMs = System.currentTimeMillis();
                    LodRequestManager manager = new LodRequestManager();
                    Minecraft mc = Minecraft.getInstance();
                    ServerData serverData = mc.getCurrentServer();
                    IntegratedServer spServer = mc.getSingleplayerServer();
                    if (serverData != null && serverData.ip != null) {
                        serverAddr = serverData.ip;
                    } else if (spServer != null) {
                        Path worldDir = spServer.getWorldPath(LevelResource.ROOT).getFileName();
                        serverAddr = "local:" + String.valueOf(worldDir != null ? worldDir : "world");
                    } else {
                        serverAddr = "unknown";
                    }
                    manager.onSessionConfig(payload, serverAddr);
                    requestManager = manager;
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BatchResponseS2CPayload.TYPE, (payload2, context2) -> {
            context2.client().execute(() -> {
                LodRequestManager manager;
                if (serverEnabled && (manager = requestManager) != null) {
                    for (int i = 0; i < payload2.count(); i++) {
                        int requestId = payload2.requestIds()[i];
                        byte type = payload2.responseTypes()[i];
                        switch (type) {
                            case 0:
                                manager.onRateLimited(requestId);
                                break;
                            case 1:
                                manager.onColumnUpToDate(requestId);
                                break;
                            case 2:
                                manager.onColumnNotGenerated(requestId);
                                break;
                            default:
                                VSSLogger.warn("Unknown batch response type: " + type);
                                break;
                        }
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DirtyColumnsS2CPayload.TYPE, (payload3, context3) -> {
            context3.client().execute(() -> {
                LodRequestManager manager;
                if (serverEnabled && (manager = requestManager) != null) {
                    manager.onDirtyColumns(payload3.dirtyPositions());
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(VoxelColumnS2CPayload.TYPE, (payload4, context4) -> {
            if (serverEnabled) {
                columnsReceived.incrementAndGet();
                bytesReceived.addAndGet(payload4.estimatedBytes());
                context4.client().execute(() -> {
                    if (serverEnabled) {
                        LodRequestManager manager = requestManager;
                        if (manager != null) {
                            manager.onColumnReceived(payload4.requestId(), payload4.columnTimestamp());
                        }
                        columnProcessor.offer(payload4);
                    }
                });
            }
        });
    }

    private static void registerConnectionLifecycle() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            serverEnabled = false;
            versionMismatchFlag = false;
            serverLodDistance = 0;
            requestManager = null;
            if (VSSClientConfig.CONFIG.receiveServerLods) {
                if ((!Minecraft.getInstance().hasSingleplayerServer() || Boolean.getBoolean("vss.test.integratedServer")) && !isVersionMismatch()) {
                    try {
                        int clientCaps = VSSApi.hasVoxelConsumers() ? 1 : 0;
                        ClientPlayNetworking.send(new HandshakeC2SPayload(15, clientCaps));
                    } catch (Exception e) {
                        VSSLogger.debug("Handshake send failed (server likely doesn't have VSS): " + e.getMessage());
                    }
                }
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler2, client2) -> {
            LodRequestManager manager = requestManager;
            if (manager != null) {
                manager.disconnect();
                manager.saveCache();
            }
            columnProcessor.shutdown();
            columnProcessor.resetStats();
            serverEnabled = false;
            versionMismatchFlag = false;
            serverLodDistance = 0;
            columnsReceived.set(0L);
            bytesReceived.set(0L);
            connectionStartMs = 0L;
            requestManager = null;
        });
    }

    private static void registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LodRequestManager manager = requestManager;
            if (manager != null && serverEnabled) {
                manager.tick();
            }
            columnProcessor.scheduleProcessing(serverEnabled);
        });
    }
}

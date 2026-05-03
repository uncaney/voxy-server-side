package dev.xantha.vss.paper;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.paper.PaperPayloadHandler;
import java.util.Objects;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/VSSPaperPlugin.class */
public class VSSPaperPlugin extends JavaPlugin implements PluginMessageListener, Listener {
    private PaperConfig vssConfig;
    private volatile PaperRequestProcessingService requestService;

    /* JADX WARN: Type inference failed for: r0v31, types: [dev.xantha.vss.paper.VSSPaperPlugin$1] */
    public void onEnable() {
        this.vssConfig = PaperConfig.load(getDataFolder().toPath());
        getServer().getMessenger().registerIncomingPluginChannel(this, VSSConstants.CHANNEL_HANDSHAKE, this);
        getServer().getMessenger().registerIncomingPluginChannel(this, VSSConstants.CHANNEL_CHUNK_REQUEST, this);
        getServer().getMessenger().registerIncomingPluginChannel(this, VSSConstants.CHANNEL_CANCEL_REQUEST, this);
        getServer().getMessenger().registerIncomingPluginChannel(this, VSSConstants.CHANNEL_BANDWIDTH_UPDATE, this);
        getServer().getPluginManager().registerEvents(this, this);
        DedicatedServer nmsServer = getServer().getServer();
        this.requestService = new PaperRequestProcessingService(nmsServer, this, this.vssConfig);
        VSSLogger.info("Starting VSS LOD request processing service");
        PaperWorldHandler worldHandler = new PaperWorldHandler(this, this.requestService.getDirtyTracker());
        worldHandler.registerUpdateListeners(this.vssConfig.updateEvents);
        PluginCommand cmd = getCommand("vsslod");
        if (cmd != null) {
            PaperCommands executor = new PaperCommands(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }
        new BukkitRunnable(this) { // from class: dev.xantha.vss.paper.VSSPaperPlugin.1
            final /* synthetic */ VSSPaperPlugin this$0;

            {
                Objects.requireNonNull(this);
                this.this$0 = this;
            }

            public void run() {
                PaperRequestProcessingService service = this.this$0.requestService;
                if (service != null) {
                    service.tick();
                }
            }
        }.runTaskTimer(this, 1L, 1L);
        VSSLogger.info("Voxy Server Side (Paper) enabled");
    }

    public void onDisable() {
        PaperRequestProcessingService service = this.requestService;
        if (service != null) {
            VSSLogger.info("Stopping VSS LOD request processing service");
            service.shutdown();
            this.requestService = null;
        }
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        VSSLogger.info("Voxy Server Side (Paper) disabled");
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        PaperRequestProcessingService service = this.requestService;
        if (service == null || message == null || message.length == 0) {
            return;
        }
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        try {
            switch (channel) {
                case "vss:handshake_c2s":
                    handleHandshake(player, nmsPlayer, message);
                    break;
                case "vss:batch_chunk_req":
                    handleBatchChunkRequest(nmsPlayer, message);
                    break;
                case "vss:cancel_request":
                    handleCancelRequest(nmsPlayer, message);
                    break;
                case "vss:bandwidth_update":
                    handleBandwidthUpdate(nmsPlayer, message);
                    break;
            }
        } catch (Exception e) {
            VSSLogger.error("Error handling plugin message on channel " + channel + " from " + player.getName(), e);
        }
    }

    private void handleHandshake(Player bukkitPlayer, ServerPlayer nmsPlayer, byte[] data) {
        PaperPayloadHandler.DecodedHandshake handshake = PaperPayloadHandler.decodeHandshake(data);
        if (handshake == null) {
            return;
        }
        VSSLogger.info("VSS handshake received from " + nmsPlayer.getName().getString() + " (protocol v" + handshake.protocolVersion() + ", capabilities=" + handshake.capabilities() + ")");
        boolean effectiveEnabled = this.vssConfig.enabled && this.requestService != null;
        PaperPayloadHandler.sendSessionConfig(bukkitPlayer, 15, effectiveEnabled, this.vssConfig.lodDistanceChunks, 1, this.vssConfig.syncOnLoadRateLimitPerPlayer, this.vssConfig.syncOnLoadConcurrencyLimitPerPlayer, this.vssConfig.generationRateLimitPerPlayer, this.vssConfig.generationConcurrencyLimitPerPlayer, this.vssConfig.enableChunkGeneration, this.vssConfig.bytesPerSecondLimitPerPlayer);
        if (handshake.protocolVersion() != 15) {
            VSSLogger.warn("Player " + nmsPlayer.getName().getString() + " has incompatible VSS protocol version " + handshake.protocolVersion() + " (server: 15), skipping LOD distribution");
        } else if (effectiveEnabled) {
            this.requestService.registerPlayer(nmsPlayer, handshake.capabilities());
            VSSLogger.info("Player " + nmsPlayer.getName().getString() + " registered for VSS LOD request processing" + (handshake.capabilities() != 0 ? " (caps=" + handshake.capabilities() + ")" : ""));
        }
    }

    private void handleBatchChunkRequest(ServerPlayer nmsPlayer, byte[] data) {
        PaperRequestProcessingService service;
        PaperPayloadHandler.DecodedBatchChunkRequest decoded = PaperPayloadHandler.decodeBatchChunkRequest(data);
        if (decoded != null && (service = this.requestService) != null) {
            service.handleBatchRequest(nmsPlayer, decoded);
        }
    }

    private void handleCancelRequest(ServerPlayer nmsPlayer, byte[] data) {
        PaperRequestProcessingService service;
        PaperPayloadHandler.DecodedCancelRequest decoded = PaperPayloadHandler.decodeCancelRequest(data);
        if (decoded != null && (service = this.requestService) != null) {
            service.handleCancel(nmsPlayer, decoded.requestId());
        }
    }

    private void handleBandwidthUpdate(ServerPlayer nmsPlayer, byte[] data) {
        PaperRequestProcessingService service;
        PaperPayloadHandler.DecodedBandwidthUpdate decoded = PaperPayloadHandler.decodeBandwidthUpdate(data);
        if (decoded != null && (service = this.requestService) != null) {
            service.handleBandwidthUpdate(nmsPlayer, decoded.desiredRate());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PaperRequestProcessingService service = this.requestService;
        if (service != null) {
            service.removePlayer(event.getPlayer().getUniqueId());
        }
    }

    public PaperRequestProcessingService getRequestService() {
        return this.requestService;
    }

    public PaperConfig getLssConfig() {
        return this.vssConfig;
    }
}

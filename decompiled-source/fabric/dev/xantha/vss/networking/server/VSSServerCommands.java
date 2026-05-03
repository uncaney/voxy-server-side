package dev.xantha.vss.networking.server;

import dev.xantha.vss.common.DiagnosticsFormatter;
import dev.xantha.vss.common.SharedBandwidthLimiter;
import dev.xantha.vss.common.processing.ProcessingDiagnostics;
import dev.xantha.vss.config.VSSServerConfig;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/VSSServerCommands.class */
class VSSServerCommands {
    VSSServerCommands() {
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("vsslod").requires(source -> {
                return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
            }).then(Commands.literal("stats").executes(ctx -> {
                return showStats((CommandSourceStack) ctx.getSource());
            })).then(Commands.literal("diag").executes(ctx2 -> {
                return showDiagnostics((CommandSourceStack) ctx2.getSource());
            })));
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int showStats(CommandSourceStack source) {
        RequestProcessingService service = VSSServerNetworking.getRequestService();
        if (service == null) {
            source.sendFailure(Component.literal("VSS LOD request processing is not active"));
            return 0;
        }
        Map<UUID, PlayerRequestState> players = service.getPlayers();
        if (players.isEmpty()) {
            source.sendSuccess(() -> {
                return Component.literal("No players connected with VSS");
            }, false);
            return 1;
        }
        source.sendSuccess(() -> {
            return Component.literal("=== VSS LOD Request Stats ===");
        }, false);
        for (Map.Entry<UUID, PlayerRequestState> entry : players.entrySet()) {
            PlayerRequestState state = entry.getValue();
            ServerPlayer player = state.getPlayer();
            Object[] objArr = new Object[8];
            objArr[0] = player.getName().getString();
            objArr[1] = state.hasCompletedHandshake() ? "yes" : "no";
            objArr[2] = Long.valueOf(state.getTotalSectionsSent());
            objArr[3] = DiagnosticsFormatter.formatBytes(state.getTotalBytesSent());
            objArr[4] = Integer.valueOf(state.getPendingSyncCount());
            objArr[5] = Integer.valueOf(state.getPendingGenerationCount());
            objArr[6] = Integer.valueOf(state.getSendQueueSize());
            objArr[7] = Long.valueOf(state.getTotalRequestsReceived());
            String line = String.format("%s: handshake=%s, sent=%d sections (%s), pending_sync=%d, pending_gen=%d, send_queue=%d, requests=%d", objArr);
            source.sendSuccess(() -> {
                return Component.literal(line);
            }, false);
        }
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int showDiagnostics(CommandSourceStack source) {
        RequestProcessingService service = VSSServerNetworking.getRequestService();
        if (service == null) {
            source.sendFailure(Component.literal("VSS LOD request processing is not active"));
            return 0;
        }
        VSSServerConfig config = VSSServerConfig.CONFIG;
        long uptimeSec = service.getUptimeSeconds();
        ProcessingDiagnostics diag = service.getOffThreadProcessor().getDiagnostics();
        ChunkDiskReader diskReader = service.getDiskReader();
        long diskCompleted = diskReader != null ? diskReader.getDiag().getSuccessfulReadCount() : 0L;
        ChunkGenerationService genService = service.getGenerationService();
        SharedBandwidthLimiter bwLimiter = service.getBandwidthLimiter();
        long totalSent = 0;
        long totalBytes = 0;
        ArrayList<DiagnosticsFormatter.PlayerDiag> players = new ArrayList<>();
        for (Map.Entry<UUID, PlayerRequestState> entry : service.getPlayers().entrySet()) {
            PlayerRequestState state = entry.getValue();
            totalSent += state.getTotalSectionsSent();
            totalBytes += state.getTotalBytesSent();
            players.add(new DiagnosticsFormatter.PlayerDiag(state.getPlayer().getName().getString(), state.getSendQueueSize(), config.sendQueueLimitPerPlayer, state.getPendingSyncCount(), state.getPendingGenerationCount(), state.getTotalSectionsSent(), state.getTotalBytesSent()));
        }
        DiagnosticsFormatter.DiagData data = new DiagnosticsFormatter.DiagData(config.enabled, config.lodDistanceChunks, config.bytesPerSecondLimitPerPlayer, config.bytesPerSecondLimitGlobal, uptimeSec, totalSent, totalBytes, diag.getTotalInMemory(), diag.getTotalUpToDate(), diag.getTotalGenDrained(), diskCompleted, service.getTickDiagnostics(), diskReader != null ? diskReader.getDiagnostics() : "N/A", genService != null ? genService.getDiagnostics() : null, genService != null, bwLimiter.getTotalBytesSent(), service.getWindowBandwidthRate(), players);
        for (String line : DiagnosticsFormatter.formatDiagnostics(data)) {
            source.sendSuccess(() -> {
                return Component.literal(line);
            }, false);
        }
        return 1;
    }
}

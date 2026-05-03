package dev.xantha.vss.paper;

import dev.xantha.vss.common.DiagnosticsFormatter;
import dev.xantha.vss.common.SharedBandwidthLimiter;
import dev.xantha.vss.common.processing.ProcessingDiagnostics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperCommands.class */
public class PaperCommands implements CommandExecutor, TabCompleter {
    private final VSSPaperPlugin plugin;

    public PaperCommands(VSSPaperPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /vsslod <stats|diag>");
            return true;
        }
        PaperRequestProcessingService service = this.plugin.getRequestService();
        if (service == null) {
            sender.sendMessage("VSS LOD request processing is not active");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "stats":
                showStats(sender, service);
                break;
            case "diag":
                showDiagnostics(sender, service);
                break;
            default:
                sender.sendMessage("Usage: /vsslod <stats|diag>");
                break;
        }
        return true;
    }

    private void showStats(CommandSender sender, PaperRequestProcessingService service) {
        Map<UUID, PaperPlayerRequestState> players = service.getPlayers();
        if (players.isEmpty()) {
            sender.sendMessage("No players connected with VSS");
            return;
        }
        sender.sendMessage("=== VSS LOD Request Stats ===");
        for (Map.Entry<UUID, PaperPlayerRequestState> entry : players.entrySet()) {
            PaperPlayerRequestState state = entry.getValue();
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
            sender.sendMessage(line);
        }
    }

    private void showDiagnostics(CommandSender sender, PaperRequestProcessingService service) {
        PaperConfig config = this.plugin.getLssConfig();
        long uptimeSec = service.getUptimeSeconds();
        ProcessingDiagnostics diag = service.getOffThreadProcessor().getDiagnostics();
        PaperChunkDiskReader diskReader = service.getDiskReader();
        long diskCompleted = diskReader != null ? diskReader.getDiag().getSuccessfulReadCount() : 0L;
        PaperChunkGenerationService genService = service.getGenerationService();
        SharedBandwidthLimiter bwLimiter = service.getBandwidthLimiter();
        long totalSent = 0;
        long totalBytes = 0;
        ArrayList<DiagnosticsFormatter.PlayerDiag> players = new ArrayList<>();
        for (Map.Entry<UUID, PaperPlayerRequestState> entry : service.getPlayers().entrySet()) {
            PaperPlayerRequestState state = entry.getValue();
            totalSent += state.getTotalSectionsSent();
            totalBytes += state.getTotalBytesSent();
            players.add(new DiagnosticsFormatter.PlayerDiag(state.getPlayer().getName().getString(), state.getSendQueueSize(), config.sendQueueLimitPerPlayer, state.getPendingSyncCount(), state.getPendingGenerationCount(), state.getTotalSectionsSent(), state.getTotalBytesSent()));
        }
        DiagnosticsFormatter.DiagData data = new DiagnosticsFormatter.DiagData(config.enabled, config.lodDistanceChunks, config.bytesPerSecondLimitPerPlayer, config.bytesPerSecondLimitGlobal, uptimeSec, totalSent, totalBytes, diag.getTotalInMemory(), diag.getTotalUpToDate(), diag.getTotalGenDrained(), diskCompleted, service.getTickDiagnostics(), diskReader != null ? diskReader.getDiagnostics() : "N/A", genService != null ? genService.getDiagnostics() : null, genService != null, bwLimiter.getTotalBytesSent(), service.getWindowBandwidthRate(), players);
        for (String line : DiagnosticsFormatter.formatDiagnostics(data)) {
            sender.sendMessage(line);
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("stats", "diag").stream().filter(s -> {
                return s.startsWith(args[0].toLowerCase());
            }).toList();
        }
        return Collections.emptyList();
    }
}

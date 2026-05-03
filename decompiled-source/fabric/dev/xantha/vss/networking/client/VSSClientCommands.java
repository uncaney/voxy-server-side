package dev.xantha.vss.networking.client;

import dev.xantha.vss.common.DiagnosticsFormatter;
import dev.xantha.vss.common.VSSConstants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/VSSClientCommands.class */
public class VSSClientCommands {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal(VSSConstants.MOD_ID).then(ClientCommands.literal("clearcache").executes(context -> {
                LodRequestManager manager = VSSClientNetworking.getRequestManager();
                if (manager != null) {
                    manager.flushCache();
                    ((FabricClientCommandSource) context.getSource()).sendFeedback(Component.literal("VSS column cache cleared for current server. Chunks will be re-requested."));
                    return 1;
                }
                ColumnCacheStore.clearAll();
                ((FabricClientCommandSource) context.getSource()).sendFeedback(Component.literal("VSS column cache cleared for all servers."));
                return 1;
            })).then(ClientCommands.literal("diag").executes(context2 -> {
                showDiagnostics((FabricClientCommandSource) context2.getSource());
                return 1;
            })));
        });
    }

    private static void showDiagnostics(FabricClientCommandSource source) {
        LodRequestManager manager = VSSClientNetworking.getRequestManager();
        if (manager == null || !VSSClientNetworking.isServerEnabled()) {
            source.sendFeedback(Component.literal("VSS is not active on this server").withStyle(ChatFormatting.RED));
            return;
        }
        source.sendFeedback(Component.literal("=== VSS Client Diagnostics ===").withStyle(ChatFormatting.GOLD));
        int serverDist = VSSClientNetworking.getServerLodDistance();
        int effectiveDist = manager.getEffectiveLodDistanceChunks();
        source.sendFeedback(Component.literal(String.format("Connection: server_lod_dist=%d, effective_dist=%d", Integer.valueOf(serverDist), Integer.valueOf(effectiveDist))).withStyle(ChatFormatting.GRAY));
        long received = VSSClientNetworking.getColumnsReceived();
        long bytes = VSSClientNetworking.getBytesReceived();
        long dropped = VSSClientNetworking.getColumnsDropped();
        long startMs = VSSClientNetworking.getConnectionStartMs();
        long uptimeSec = startMs > 0 ? (System.currentTimeMillis() - startMs) / 1000 : 0L;
        source.sendFeedback(Component.literal(String.format("Throughput: received=%d (%s), dropped=%d, recv_rate=%s/s, req_rate=%s/s, uptime=%s", Long.valueOf(received), DiagnosticsFormatter.formatBytes(bytes), Long.valueOf(dropped), DiagnosticsFormatter.formatRate(manager.getReceiveRate()), DiagnosticsFormatter.formatRate(manager.getRequestRate()), DiagnosticsFormatter.formatUptime(uptimeSec))).withStyle(ChatFormatting.GRAY));
        int queued = VSSClientNetworking.getQueuedColumnCount();
        source.sendFeedback(Component.literal(String.format("Queue: queued=%d/%d", Integer.valueOf(queued), 8000)).withStyle(ChatFormatting.GRAY));
        int receivedCols = manager.getReceivedColumnCount();
        int empty = manager.getEmptyColumnCount();
        int dirty = manager.getDirtyColumnCount();
        source.sendFeedback(Component.literal(String.format("Columns: received=%d, empty=%d, dirty=%d", Integer.valueOf(receivedCols), Integer.valueOf(empty), Integer.valueOf(dirty))).withStyle(ChatFormatting.GRAY));
        source.sendFeedback(Component.literal(String.format("Responses: columns=%d, up_to_date=%d, not_generated=%d, rate_limited=%d", Long.valueOf(manager.getTotalColumnsReceived()), Long.valueOf(manager.getTotalUpToDate()), Long.valueOf(manager.getTotalNotGenerated()), Long.valueOf(manager.getTotalRateLimited()))).withStyle(ChatFormatting.GRAY));
        source.sendFeedback(Component.literal(String.format("Requests: send_cycles=%d, total_requested=%d", Long.valueOf(manager.getTotalSendCycles()), Long.valueOf(manager.getTotalPositionsRequested()))).withStyle(ChatFormatting.GRAY));
        int confirmedRing = manager.getConfirmedRing();
        int scanRing = manager.getScanRing();
        int maxRing = manager.getEffectiveLodDistanceChunks();
        source.sendFeedback(Component.literal(String.format("Scan: confirmed=%d, scanning=%d/%d, missing_vanilla=%d", Integer.valueOf(confirmedRing), Integer.valueOf(scanRing), Integer.valueOf(maxRing), Integer.valueOf(manager.getMissingVanillaChunks()))).withStyle(ChatFormatting.GRAY));
        int budget = manager.getLastBudget();
        int syncQueued = manager.getLastSyncQueued();
        int genQueued = manager.getLastGenQueued();
        source.sendFeedback(Component.literal(String.format("Budget: used=%d/%d (sync=%d, gen=%d), queue=%d", Integer.valueOf(syncQueued + genQueued), Integer.valueOf(budget), Integer.valueOf(syncQueued), Integer.valueOf(genQueued), Integer.valueOf(manager.getQueueRemaining()))).withStyle(ChatFormatting.GRAY));
    }
}

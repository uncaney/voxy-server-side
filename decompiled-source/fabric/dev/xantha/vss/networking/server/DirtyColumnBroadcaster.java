package dev.xantha.vss.networking.server;

import dev.xantha.vss.common.PositionUtil;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.tracking.DirtyColumnTracker;
import dev.xantha.vss.config.VSSServerConfig;
import dev.xantha.vss.networking.payloads.DirtyColumnsS2CPayload;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/server/DirtyColumnBroadcaster.class */
class DirtyColumnBroadcaster {
    private final MinecraftServer server;
    private final Map<UUID, PlayerRequestState> players;
    private final FabricOffThreadProcessor offThreadProcessor;
    private final DirtyColumnTracker dirtyTracker;
    private int counter = 0;
    private long[] positionFilterBuffer = null;

    DirtyColumnBroadcaster(MinecraftServer server, Map<UUID, PlayerRequestState> players, FabricOffThreadProcessor offThreadProcessor, DirtyColumnTracker dirtyTracker) {
        this.server = server;
        this.players = players;
        this.offThreadProcessor = offThreadProcessor;
        this.dirtyTracker = dirtyTracker;
    }

    void tick(VSSServerConfig config) {
        int intervalTicks = config.dirtyBroadcastIntervalSeconds * 20;
        int i = this.counter + 1;
        this.counter = i;
        if (i < intervalTicks) {
            return;
        }
        this.counter = 0;
        Set<UUID> failedPlayers = null;
        for (ServerLevel level : this.server.getAllLevels()) {
            String dimensionStr = level.dimension().identifier().toString();
            long[] dirty = this.dirtyTracker.drainDirty(dimensionStr);
            if (dirty != null && dirty.length != 0) {
                this.offThreadProcessor.invalidateTimestamps(dimensionStr, dirty);
                int bufLen = Math.min(dirty.length, 10240);
                if (this.positionFilterBuffer == null || this.positionFilterBuffer.length < bufLen) {
                    this.positionFilterBuffer = new long[bufLen];
                }
                for (PlayerRequestState state : this.players.values()) {
                    if (state.hasCompletedHandshake()) {
                        ServerPlayer player = state.getPlayer();
                        if (failedPlayers == null || !failedPlayers.contains(player.getUUID())) {
                            if (state.getLastDimension().equals(level.dimension()) && !player.isRemoved()) {
                                int playerCx = player.getBlockX() >> 4;
                                int playerCz = player.getBlockZ() >> 4;
                                int lodDist = config.lodDistanceChunks;
                                int count = 0;
                                for (long packed : dirty) {
                                    if (!PositionUtil.isOutOfRange(packed, playerCx, playerCz, lodDist)) {
                                        int i2 = count;
                                        count++;
                                        this.positionFilterBuffer[i2] = packed;
                                        if (count >= 10240) {
                                            break;
                                        }
                                    }
                                }
                                if (count > 0) {
                                    long[] result = new long[count];
                                    System.arraycopy(this.positionFilterBuffer, 0, result, 0, count);
                                    state.clearDiskReadDoneForPositions(result);
                                    try {
                                        ServerPlayNetworking.send(player, new DirtyColumnsS2CPayload(result));
                                    } catch (Exception e) {
                                        VSSLogger.error("Failed to send dirty columns to " + player.getName().getString(), e);
                                        if (failedPlayers == null) {
                                            failedPlayers = new HashSet<>();
                                        }
                                        failedPlayers.add(player.getUUID());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package dev.xantha.vss.paper;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import io.netty.buffer.Unpooled;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperPayloadHandler.class */
public final class PaperPayloadHandler {
    private static final Identifier ID_SESSION_CONFIG = Identifier.parse(VSSConstants.CHANNEL_SESSION_CONFIG);
    private static final Identifier ID_DIRTY_COLUMNS = Identifier.parse(VSSConstants.CHANNEL_DIRTY_COLUMNS);
    static final Identifier ID_VOXEL_COLUMN = Identifier.parse(VSSConstants.CHANNEL_VOXEL_COLUMN);
    private static final Identifier ID_BATCH_RESPONSE = Identifier.parse(VSSConstants.CHANNEL_BATCH_RESPONSE);

    private PaperPayloadHandler() {
    }

    public static void sendSessionConfig(Player player, int protocolVersion, boolean enabled, int lodDistanceChunks, int serverCapabilities, int syncOnLoadRateLimitPerPlayer, int syncOnLoadConcurrencyLimitPerPlayer, int generationRateLimitPerPlayer, int generationConcurrencyLimitPerPlayer, boolean generationEnabled, long playerBandwidthLimit) {
        sendEncoded(player, ID_SESSION_CONFIG, buf -> {
            buf.writeVarInt(protocolVersion);
            buf.writeBoolean(enabled);
            buf.writeVarInt(lodDistanceChunks);
            buf.writeVarInt(serverCapabilities);
            buf.writeVarInt(syncOnLoadRateLimitPerPlayer);
            buf.writeVarInt(syncOnLoadConcurrencyLimitPerPlayer);
            buf.writeVarInt(generationRateLimitPerPlayer);
            buf.writeVarInt(generationConcurrencyLimitPerPlayer);
            buf.writeBoolean(generationEnabled);
            buf.writeVarLong(playerBandwidthLimit);
        });
    }

    public static void sendBatchResponse(Player player, byte[] responseTypes, int[] requestIds, int count) {
        sendEncoded(player, ID_BATCH_RESPONSE, buf -> {
            buf.writeVarInt(count);
            for (int i = 0; i < count; i++) {
                buf.writeByte(responseTypes[i]);
                buf.writeVarInt(requestIds[i]);
            }
        });
    }

    public static Identifier channelId(String channel) {
        switch (channel) {
            case "vss:session_config":
                return ID_SESSION_CONFIG;
            case "vss:dirty_columns":
                return ID_DIRTY_COLUMNS;
            case "vss:voxel_column":
                return ID_VOXEL_COLUMN;
            case "vss:batch_response":
                return ID_BATCH_RESPONSE;
            default:
                return Identifier.parse(channel);
        }
    }

    public static byte[] encodeVoxelColumnPreEncoded(int requestId, int chunkX, int chunkZ, String dimensionStr, long columnTimestamp, byte[] sectionBytes) {
        return encodeToBytes(buf -> {
            buf.writeVarInt(requestId);
            buf.writeInt(chunkX);
            buf.writeInt(chunkZ);
            int ordinal = VSSConstants.dimensionStringToOrdinal(dimensionStr);
            buf.writeVarInt(ordinal);
            if (ordinal == -1) {
                buf.writeUtf(dimensionStr);
            }
            buf.writeLong(columnTimestamp);
            buf.writeByteArray(sectionBytes);
        });
    }

    public static byte[] encodeDirtyColumns(long[] dirtyPositions) {
        int len = Math.min(dirtyPositions.length, VSSConstants.MAX_DIRTY_COLUMN_POSITIONS);
        return encodeToBytes(buf -> {
            buf.writeVarInt(len);
            for (int i = 0; i < len; i++) {
                buf.writeLong(dirtyPositions[i]);
            }
        });
    }

    public static void sendDirtyColumns(Player player, long[] dirtyPositions) {
        byte[] data = encodeDirtyColumns(dirtyPositions);
        sendRawNmsPayload(player, ID_DIRTY_COLUMNS, data);
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperPayloadHandler$DecodedHandshake.class */
    public static final class DecodedHandshake extends Record {
        private final int protocolVersion;
        private final int capabilities;

        public DecodedHandshake(int protocolVersion, int capabilities) {
            this.protocolVersion = protocolVersion;
            this.capabilities = capabilities;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, DecodedHandshake.class), DecodedHandshake.class, "protocolVersion;capabilities", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedHandshake;->protocolVersion:I", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedHandshake;->capabilities:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, DecodedHandshake.class), DecodedHandshake.class, "protocolVersion;capabilities", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedHandshake;->protocolVersion:I", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedHandshake;->capabilities:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, DecodedHandshake.class, Object.class), DecodedHandshake.class, "protocolVersion;capabilities", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedHandshake;->protocolVersion:I", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedHandshake;->capabilities:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int protocolVersion() {
            return this.protocolVersion;
        }

        public int capabilities() {
            return this.capabilities;
        }
    }

    public static DecodedHandshake decodeHandshake(byte[] data) {
        if (data == null || data.length == 0) {
            VSSLogger.warn("Received empty handshake payload");
            return null;
        }
        return (DecodedHandshake) withReadBuffer(data, buf -> {
            int version = buf.readVarInt();
            int caps = buf.isReadable() ? buf.readVarInt() : 0;
            return new DecodedHandshake(version, caps);
        });
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest.class */
    public static final class DecodedBatchChunkRequest extends Record {
        private final int[] requestIds;
        private final long[] packedPositions;
        private final long[] clientTimestamps;
        private final int count;

        public DecodedBatchChunkRequest(int[] requestIds, long[] packedPositions, long[] clientTimestamps, int count) {
            this.requestIds = requestIds;
            this.packedPositions = packedPositions;
            this.clientTimestamps = clientTimestamps;
            this.count = count;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, DecodedBatchChunkRequest.class), DecodedBatchChunkRequest.class, "requestIds;packedPositions;clientTimestamps;count", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->requestIds:[I", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->packedPositions:[J", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->clientTimestamps:[J", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, DecodedBatchChunkRequest.class), DecodedBatchChunkRequest.class, "requestIds;packedPositions;clientTimestamps;count", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->requestIds:[I", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->packedPositions:[J", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->clientTimestamps:[J", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, DecodedBatchChunkRequest.class, Object.class), DecodedBatchChunkRequest.class, "requestIds;packedPositions;clientTimestamps;count", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->requestIds:[I", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->packedPositions:[J", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->clientTimestamps:[J", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBatchChunkRequest;->count:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int[] requestIds() {
            return this.requestIds;
        }

        public long[] packedPositions() {
            return this.packedPositions;
        }

        public long[] clientTimestamps() {
            return this.clientTimestamps;
        }

        public int count() {
            return this.count;
        }
    }

    public static DecodedBatchChunkRequest decodeBatchChunkRequest(byte[] data) {
        if (data == null || data.length == 0) {
            VSSLogger.warn("Received empty batch chunk request payload");
            return null;
        }
        return (DecodedBatchChunkRequest) withReadBuffer(data, buf -> {
            int count = buf.readVarInt();
            if (count < 0 || count > 1024) {
                VSSLogger.warn("Batch chunk request count out of range: " + count);
                return null;
            }
            int[] requestIds = new int[count];
            long[] packedPositions = new long[count];
            long[] clientTimestamps = new long[count];
            for (int i = 0; i < count; i++) {
                requestIds[i] = buf.readVarInt();
                packedPositions[i] = buf.readLong();
                clientTimestamps[i] = buf.readLong();
            }
            return new DecodedBatchChunkRequest(requestIds, packedPositions, clientTimestamps, count);
        });
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperPayloadHandler$DecodedCancelRequest.class */
    public static final class DecodedCancelRequest extends Record {
        private final int requestId;

        public DecodedCancelRequest(int requestId) {
            this.requestId = requestId;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, DecodedCancelRequest.class), DecodedCancelRequest.class, "requestId", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedCancelRequest;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, DecodedCancelRequest.class), DecodedCancelRequest.class, "requestId", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedCancelRequest;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, DecodedCancelRequest.class, Object.class), DecodedCancelRequest.class, "requestId", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedCancelRequest;->requestId:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int requestId() {
            return this.requestId;
        }
    }

    public static DecodedCancelRequest decodeCancelRequest(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return (DecodedCancelRequest) withReadBuffer(data, buf -> {
            return new DecodedCancelRequest(buf.readVarInt());
        });
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperPayloadHandler$DecodedBandwidthUpdate.class */
    public static final class DecodedBandwidthUpdate extends Record {
        private final long desiredRate;

        public DecodedBandwidthUpdate(long desiredRate) {
            this.desiredRate = desiredRate;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, DecodedBandwidthUpdate.class), DecodedBandwidthUpdate.class, "desiredRate", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBandwidthUpdate;->desiredRate:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, DecodedBandwidthUpdate.class), DecodedBandwidthUpdate.class, "desiredRate", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBandwidthUpdate;->desiredRate:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, DecodedBandwidthUpdate.class, Object.class), DecodedBandwidthUpdate.class, "desiredRate", "FIELD:Ldev/xantha/vss/paper/PaperPayloadHandler$DecodedBandwidthUpdate;->desiredRate:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public long desiredRate() {
            return this.desiredRate;
        }
    }

    public static DecodedBandwidthUpdate decodeBandwidthUpdate(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return (DecodedBandwidthUpdate) withReadBuffer(data, buf -> {
            return new DecodedBandwidthUpdate(buf.readVarLong());
        });
    }

    private static <T> T withReadBuffer(byte[] data, Function<FriendlyByteBuf, T> fn) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        try {
            T tApply = fn.apply(buf);
            buf.release();
            return tApply;
        } catch (Throwable th) {
            buf.release();
            throw th;
        }
    }

    private static void sendEncoded(Player player, Identifier channelId, Consumer<FriendlyByteBuf> writer) {
        byte[] bytes = encodeToBytes(writer);
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        if (nmsPlayer.connection == null) {
            return;
        }
        nmsPlayer.connection.send(new ClientboundCustomPayloadPacket(new DiscardedPayload(channelId, bytes)));
    }

    private static byte[] encodeToBytes(Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            buf.release();
            return bytes;
        } catch (Throwable th) {
            buf.release();
            throw th;
        }
    }

    public static void sendRawNmsPayload(Player player, Identifier channelId, byte[] data) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        if (nmsPlayer.connection == null) {
            return;
        }
        nmsPlayer.connection.send(new ClientboundCustomPayloadPacket(new DiscardedPayload(channelId, data)));
    }
}

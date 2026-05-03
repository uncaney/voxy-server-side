package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload.class */
public final class BatchChunkRequestC2SPayload extends Record implements CustomPacketPayload {
    private final int[] requestIds;
    private final long[] packedPositions;
    private final long[] clientTimestamps;
    private final int count;
    public static final CustomPacketPayload.Type<BatchChunkRequestC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_CHUNK_REQUEST));
    public static final StreamCodec<FriendlyByteBuf, BatchChunkRequestC2SPayload> CODEC = StreamCodec.of((buf, payload) -> {
        buf.writeVarInt(payload.count);
        for (int i = 0; i < payload.count; i++) {
            buf.writeVarInt(payload.requestIds[i]);
            buf.writeLong(payload.packedPositions[i]);
            buf.writeLong(payload.clientTimestamps[i]);
        }
    }, buf2 -> {
        int count = buf2.readVarInt();
        if (count < 0 || count > 1024) {
            throw new IllegalArgumentException("Batch chunk request count out of range: " + count);
        }
        int[] requestIds = new int[count];
        long[] packedPositions = new long[count];
        long[] clientTimestamps = new long[count];
        for (int i = 0; i < count; i++) {
            requestIds[i] = buf2.readVarInt();
            packedPositions[i] = buf2.readLong();
            clientTimestamps[i] = buf2.readLong();
        }
        return new BatchChunkRequestC2SPayload(requestIds, packedPositions, clientTimestamps, count);
    });

    public BatchChunkRequestC2SPayload(int[] requestIds, long[] packedPositions, long[] clientTimestamps, int count) {
        this.requestIds = requestIds;
        this.packedPositions = packedPositions;
        this.clientTimestamps = clientTimestamps;
        this.count = count;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, BatchChunkRequestC2SPayload.class), BatchChunkRequestC2SPayload.class, "requestIds;packedPositions;clientTimestamps;count", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->requestIds:[I", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->packedPositions:[J", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->clientTimestamps:[J", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, BatchChunkRequestC2SPayload.class), BatchChunkRequestC2SPayload.class, "requestIds;packedPositions;clientTimestamps;count", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->requestIds:[I", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->packedPositions:[J", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->clientTimestamps:[J", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, BatchChunkRequestC2SPayload.class, Object.class), BatchChunkRequestC2SPayload.class, "requestIds;packedPositions;clientTimestamps;count", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->requestIds:[I", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->packedPositions:[J", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->clientTimestamps:[J", "FIELD:Ldev/xantha/vss/networking/payloads/BatchChunkRequestC2SPayload;->count:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
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

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

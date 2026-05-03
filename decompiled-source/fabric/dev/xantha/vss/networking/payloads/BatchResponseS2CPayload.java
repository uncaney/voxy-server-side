package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/BatchResponseS2CPayload.class */
public final class BatchResponseS2CPayload extends Record implements CustomPacketPayload {
    private final byte[] responseTypes;
    private final int[] requestIds;
    private final int count;
    public static final CustomPacketPayload.Type<BatchResponseS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_BATCH_RESPONSE));
    public static final StreamCodec<FriendlyByteBuf, BatchResponseS2CPayload> CODEC = StreamCodec.of((buf, payload) -> {
        buf.writeVarInt(payload.count);
        for (int i = 0; i < payload.count; i++) {
            buf.writeByte(payload.responseTypes[i]);
            buf.writeVarInt(payload.requestIds[i]);
        }
    }, buf2 -> {
        int count = buf2.readVarInt();
        if (count < 0 || count > 4096) {
            throw new IllegalArgumentException("Batch response count out of range: " + count);
        }
        byte[] responseTypes = new byte[count];
        int[] requestIds = new int[count];
        for (int i = 0; i < count; i++) {
            responseTypes[i] = buf2.readByte();
            requestIds[i] = buf2.readVarInt();
        }
        return new BatchResponseS2CPayload(responseTypes, requestIds, count);
    });

    public BatchResponseS2CPayload(byte[] responseTypes, int[] requestIds, int count) {
        this.responseTypes = responseTypes;
        this.requestIds = requestIds;
        this.count = count;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, BatchResponseS2CPayload.class), BatchResponseS2CPayload.class, "responseTypes;requestIds;count", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->responseTypes:[B", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->requestIds:[I", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, BatchResponseS2CPayload.class), BatchResponseS2CPayload.class, "responseTypes;requestIds;count", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->responseTypes:[B", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->requestIds:[I", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, BatchResponseS2CPayload.class, Object.class), BatchResponseS2CPayload.class, "responseTypes;requestIds;count", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->responseTypes:[B", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->requestIds:[I", "FIELD:Ldev/xantha/vss/networking/payloads/BatchResponseS2CPayload;->count:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public byte[] responseTypes() {
        return this.responseTypes;
    }

    public int[] requestIds() {
        return this.requestIds;
    }

    public int count() {
        return this.count;
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

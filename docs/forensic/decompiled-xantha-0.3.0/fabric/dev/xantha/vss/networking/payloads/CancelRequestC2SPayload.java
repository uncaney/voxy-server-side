package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/CancelRequestC2SPayload.class */
public final class CancelRequestC2SPayload extends Record implements CustomPacketPayload {
    private final int requestId;
    public static final CustomPacketPayload.Type<CancelRequestC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_CANCEL_REQUEST));
    public static final StreamCodec<FriendlyByteBuf, CancelRequestC2SPayload> CODEC = StreamCodec.of((buf, payload) -> {
        buf.writeVarInt(payload.requestId);
    }, buf2 -> {
        return new CancelRequestC2SPayload(buf2.readVarInt());
    });

    public CancelRequestC2SPayload(int requestId) {
        this.requestId = requestId;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, CancelRequestC2SPayload.class), CancelRequestC2SPayload.class, "requestId", "FIELD:Ldev/xantha/vss/networking/payloads/CancelRequestC2SPayload;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, CancelRequestC2SPayload.class), CancelRequestC2SPayload.class, "requestId", "FIELD:Ldev/xantha/vss/networking/payloads/CancelRequestC2SPayload;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, CancelRequestC2SPayload.class, Object.class), CancelRequestC2SPayload.class, "requestId", "FIELD:Ldev/xantha/vss/networking/payloads/CancelRequestC2SPayload;->requestId:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public int requestId() {
        return this.requestId;
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

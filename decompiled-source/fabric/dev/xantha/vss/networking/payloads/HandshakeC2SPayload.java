package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/HandshakeC2SPayload.class */
public final class HandshakeC2SPayload extends Record implements CustomPacketPayload {
    private final int protocolVersion;
    private final int capabilities;
    public static final CustomPacketPayload.Type<HandshakeC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_HANDSHAKE));
    public static final StreamCodec<FriendlyByteBuf, HandshakeC2SPayload> CODEC = StreamCodec.of((buf, payload) -> {
        buf.writeVarInt(payload.protocolVersion);
        buf.writeVarInt(payload.capabilities);
    }, buf2 -> {
        int version = buf2.readVarInt();
        int caps = buf2.readVarInt();
        return new HandshakeC2SPayload(version, caps);
    });

    public HandshakeC2SPayload(int protocolVersion, int capabilities) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, HandshakeC2SPayload.class), HandshakeC2SPayload.class, "protocolVersion;capabilities", "FIELD:Ldev/xantha/vss/networking/payloads/HandshakeC2SPayload;->protocolVersion:I", "FIELD:Ldev/xantha/vss/networking/payloads/HandshakeC2SPayload;->capabilities:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, HandshakeC2SPayload.class), HandshakeC2SPayload.class, "protocolVersion;capabilities", "FIELD:Ldev/xantha/vss/networking/payloads/HandshakeC2SPayload;->protocolVersion:I", "FIELD:Ldev/xantha/vss/networking/payloads/HandshakeC2SPayload;->capabilities:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, HandshakeC2SPayload.class, Object.class), HandshakeC2SPayload.class, "protocolVersion;capabilities", "FIELD:Ldev/xantha/vss/networking/payloads/HandshakeC2SPayload;->protocolVersion:I", "FIELD:Ldev/xantha/vss/networking/payloads/HandshakeC2SPayload;->capabilities:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public int protocolVersion() {
        return this.protocolVersion;
    }

    public int capabilities() {
        return this.capabilities;
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

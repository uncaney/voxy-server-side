package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/BandwidthUpdateC2SPayload.class */
public final class BandwidthUpdateC2SPayload extends Record implements CustomPacketPayload {
    private final long desiredRate;
    public static final CustomPacketPayload.Type<BandwidthUpdateC2SPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_BANDWIDTH_UPDATE));
    public static final StreamCodec<FriendlyByteBuf, BandwidthUpdateC2SPayload> CODEC = StreamCodec.of((buf, payload) -> {
        buf.writeVarLong(payload.desiredRate);
    }, buf2 -> {
        return new BandwidthUpdateC2SPayload(buf2.readVarLong());
    });

    public BandwidthUpdateC2SPayload(long desiredRate) {
        this.desiredRate = desiredRate;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, BandwidthUpdateC2SPayload.class), BandwidthUpdateC2SPayload.class, "desiredRate", "FIELD:Ldev/xantha/vss/networking/payloads/BandwidthUpdateC2SPayload;->desiredRate:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, BandwidthUpdateC2SPayload.class), BandwidthUpdateC2SPayload.class, "desiredRate", "FIELD:Ldev/xantha/vss/networking/payloads/BandwidthUpdateC2SPayload;->desiredRate:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, BandwidthUpdateC2SPayload.class, Object.class), BandwidthUpdateC2SPayload.class, "desiredRate", "FIELD:Ldev/xantha/vss/networking/payloads/BandwidthUpdateC2SPayload;->desiredRate:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public long desiredRate() {
        return this.desiredRate;
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

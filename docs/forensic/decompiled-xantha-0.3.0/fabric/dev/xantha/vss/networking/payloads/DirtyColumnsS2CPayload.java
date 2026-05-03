package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/DirtyColumnsS2CPayload.class */
public final class DirtyColumnsS2CPayload extends Record implements CustomPacketPayload {
    private final long[] dirtyPositions;
    public static final int MAX_POSITIONS = 10240;
    public static final CustomPacketPayload.Type<DirtyColumnsS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_DIRTY_COLUMNS));
    public static final StreamCodec<FriendlyByteBuf, DirtyColumnsS2CPayload> CODEC = StreamCodec.of((buf, payload) -> {
        buf.writeVarInt(payload.dirtyPositions.length);
        long[] arr$ = payload.dirtyPositions;
        for (long pos : arr$) {
            buf.writeLong(pos);
        }
    }, buf2 -> {
        int rawLen = Math.max(buf2.readVarInt(), 0);
        int len = Math.min(rawLen, 10240);
        long[] positions = new long[len];
        for (int i = 0; i < len; i++) {
            positions[i] = buf2.readLong();
        }
        int excess = rawLen - len;
        if (excess > 0) {
            int toSkip = (int) Math.min(((long) excess) * 8, buf2.readableBytes());
            buf2.skipBytes(toSkip);
        }
        return new DirtyColumnsS2CPayload(positions);
    });

    public DirtyColumnsS2CPayload(long[] dirtyPositions) {
        this.dirtyPositions = dirtyPositions;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, DirtyColumnsS2CPayload.class), DirtyColumnsS2CPayload.class, "dirtyPositions", "FIELD:Ldev/xantha/vss/networking/payloads/DirtyColumnsS2CPayload;->dirtyPositions:[J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, DirtyColumnsS2CPayload.class), DirtyColumnsS2CPayload.class, "dirtyPositions", "FIELD:Ldev/xantha/vss/networking/payloads/DirtyColumnsS2CPayload;->dirtyPositions:[J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, DirtyColumnsS2CPayload.class, Object.class), DirtyColumnsS2CPayload.class, "dirtyPositions", "FIELD:Ldev/xantha/vss/networking/payloads/DirtyColumnsS2CPayload;->dirtyPositions:[J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public long[] dirtyPositions() {
        return this.dirtyPositions;
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

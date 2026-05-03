package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/SessionConfigS2CPayload.class */
public final class SessionConfigS2CPayload extends Record implements CustomPacketPayload {
    private final int protocolVersion;
    private final boolean enabled;
    private final int lodDistanceChunks;
    private final int serverCapabilities;
    private final int syncOnLoadRateLimitPerPlayer;
    private final int syncOnLoadConcurrencyLimitPerPlayer;
    private final int generationRateLimitPerPlayer;
    private final int generationConcurrencyLimitPerPlayer;
    private final boolean generationEnabled;
    private final long playerBandwidthLimit;
    public static final CustomPacketPayload.Type<SessionConfigS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_SESSION_CONFIG));
    public static final StreamCodec<FriendlyByteBuf, SessionConfigS2CPayload> CODEC = StreamCodec.of((buf, payload) -> {
        buf.writeVarInt(payload.protocolVersion);
        buf.writeBoolean(payload.enabled);
        buf.writeVarInt(payload.lodDistanceChunks);
        buf.writeVarInt(payload.serverCapabilities);
        buf.writeVarInt(payload.syncOnLoadRateLimitPerPlayer);
        buf.writeVarInt(payload.syncOnLoadConcurrencyLimitPerPlayer);
        buf.writeVarInt(payload.generationRateLimitPerPlayer);
        buf.writeVarInt(payload.generationConcurrencyLimitPerPlayer);
        buf.writeBoolean(payload.generationEnabled);
        buf.writeVarLong(payload.playerBandwidthLimit);
    }, buf2 -> {
        int version = buf2.readVarInt();
        boolean enabled = buf2.readBoolean();
        int lodDist = buf2.readVarInt();
        int serverCaps = buf2.readVarInt();
        int syncRate = buf2.readVarInt();
        int syncConc = buf2.readVarInt();
        int genRate = buf2.readVarInt();
        int genConc = buf2.readVarInt();
        boolean genEnabled = buf2.readBoolean();
        long bwLimit = buf2.readVarLong();
        return new SessionConfigS2CPayload(version, enabled, lodDist, serverCaps, syncRate, syncConc, genRate, genConc, genEnabled, bwLimit);
    });

    public SessionConfigS2CPayload(int protocolVersion, boolean enabled, int lodDistanceChunks, int serverCapabilities, int syncOnLoadRateLimitPerPlayer, int syncOnLoadConcurrencyLimitPerPlayer, int generationRateLimitPerPlayer, int generationConcurrencyLimitPerPlayer, boolean generationEnabled, long playerBandwidthLimit) {
        this.protocolVersion = protocolVersion;
        this.enabled = enabled;
        this.lodDistanceChunks = lodDistanceChunks;
        this.serverCapabilities = serverCapabilities;
        this.syncOnLoadRateLimitPerPlayer = syncOnLoadRateLimitPerPlayer;
        this.syncOnLoadConcurrencyLimitPerPlayer = syncOnLoadConcurrencyLimitPerPlayer;
        this.generationRateLimitPerPlayer = generationRateLimitPerPlayer;
        this.generationConcurrencyLimitPerPlayer = generationConcurrencyLimitPerPlayer;
        this.generationEnabled = generationEnabled;
        this.playerBandwidthLimit = playerBandwidthLimit;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, SessionConfigS2CPayload.class), SessionConfigS2CPayload.class, "protocolVersion;enabled;lodDistanceChunks;serverCapabilities;syncOnLoadRateLimitPerPlayer;syncOnLoadConcurrencyLimitPerPlayer;generationRateLimitPerPlayer;generationConcurrencyLimitPerPlayer;generationEnabled;playerBandwidthLimit", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->protocolVersion:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->enabled:Z", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->lodDistanceChunks:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->serverCapabilities:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->syncOnLoadRateLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->syncOnLoadConcurrencyLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationRateLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationConcurrencyLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationEnabled:Z", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->playerBandwidthLimit:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, SessionConfigS2CPayload.class), SessionConfigS2CPayload.class, "protocolVersion;enabled;lodDistanceChunks;serverCapabilities;syncOnLoadRateLimitPerPlayer;syncOnLoadConcurrencyLimitPerPlayer;generationRateLimitPerPlayer;generationConcurrencyLimitPerPlayer;generationEnabled;playerBandwidthLimit", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->protocolVersion:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->enabled:Z", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->lodDistanceChunks:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->serverCapabilities:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->syncOnLoadRateLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->syncOnLoadConcurrencyLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationRateLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationConcurrencyLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationEnabled:Z", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->playerBandwidthLimit:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, SessionConfigS2CPayload.class, Object.class), SessionConfigS2CPayload.class, "protocolVersion;enabled;lodDistanceChunks;serverCapabilities;syncOnLoadRateLimitPerPlayer;syncOnLoadConcurrencyLimitPerPlayer;generationRateLimitPerPlayer;generationConcurrencyLimitPerPlayer;generationEnabled;playerBandwidthLimit", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->protocolVersion:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->enabled:Z", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->lodDistanceChunks:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->serverCapabilities:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->syncOnLoadRateLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->syncOnLoadConcurrencyLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationRateLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationConcurrencyLimitPerPlayer:I", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->generationEnabled:Z", "FIELD:Ldev/xantha/vss/networking/payloads/SessionConfigS2CPayload;->playerBandwidthLimit:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public int protocolVersion() {
        return this.protocolVersion;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public int lodDistanceChunks() {
        return this.lodDistanceChunks;
    }

    public int serverCapabilities() {
        return this.serverCapabilities;
    }

    public int syncOnLoadRateLimitPerPlayer() {
        return this.syncOnLoadRateLimitPerPlayer;
    }

    public int syncOnLoadConcurrencyLimitPerPlayer() {
        return this.syncOnLoadConcurrencyLimitPerPlayer;
    }

    public int generationRateLimitPerPlayer() {
        return this.generationRateLimitPerPlayer;
    }

    public int generationConcurrencyLimitPerPlayer() {
        return this.generationConcurrencyLimitPerPlayer;
    }

    public boolean generationEnabled() {
        return this.generationEnabled;
    }

    public long playerBandwidthLimit() {
        return this.playerBandwidthLimit;
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

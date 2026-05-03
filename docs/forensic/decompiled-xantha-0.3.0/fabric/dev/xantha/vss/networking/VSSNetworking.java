package dev.xantha.vss.networking;

import dev.xantha.vss.networking.payloads.BandwidthUpdateC2SPayload;
import dev.xantha.vss.networking.payloads.BatchChunkRequestC2SPayload;
import dev.xantha.vss.networking.payloads.BatchResponseS2CPayload;
import dev.xantha.vss.networking.payloads.CancelRequestC2SPayload;
import dev.xantha.vss.networking.payloads.DirtyColumnsS2CPayload;
import dev.xantha.vss.networking.payloads.HandshakeC2SPayload;
import dev.xantha.vss.networking.payloads.SessionConfigS2CPayload;
import dev.xantha.vss.networking.payloads.VoxelColumnS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/VSSNetworking.class */
public class VSSNetworking {
    public static void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(HandshakeC2SPayload.TYPE, HandshakeC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BatchChunkRequestC2SPayload.TYPE, BatchChunkRequestC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CancelRequestC2SPayload.TYPE, CancelRequestC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BandwidthUpdateC2SPayload.TYPE, BandwidthUpdateC2SPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SessionConfigS2CPayload.TYPE, SessionConfigS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BatchResponseS2CPayload.TYPE, BatchResponseS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DirtyColumnsS2CPayload.TYPE, DirtyColumnsS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(VoxelColumnS2CPayload.TYPE, VoxelColumnS2CPayload.CODEC);
    }
}

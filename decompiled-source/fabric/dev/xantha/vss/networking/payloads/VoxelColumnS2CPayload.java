package dev.xantha.vss.networking.payloads;

import dev.xantha.vss.common.VSSConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/payloads/VoxelColumnS2CPayload.class */
public final class VoxelColumnS2CPayload implements CustomPacketPayload {
    private static final int MAX_SECTIONS_SIZE = 2097152;
    private static final int MAX_DIMENSION_STRING_LENGTH = 256;
    public static final CustomPacketPayload.Type<VoxelColumnS2CPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse(VSSConstants.CHANNEL_VOXEL_COLUMN));
    public static final StreamCodec<FriendlyByteBuf, VoxelColumnS2CPayload> CODEC = StreamCodec.of(VoxelColumnS2CPayload::write, VoxelColumnS2CPayload::read);
    private final int requestId;
    private final int chunkX;
    private final int chunkZ;
    private final ResourceKey<Level> dimension;
    private final long columnTimestamp;
    private final byte[] sectionBytes;

    public VoxelColumnS2CPayload(int requestId, int chunkX, int chunkZ, ResourceKey<Level> dimension, long columnTimestamp, byte[] sectionBytes) {
        this.requestId = requestId;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.dimension = dimension;
        this.columnTimestamp = columnTimestamp;
        this.sectionBytes = sectionBytes;
    }

    public int requestId() {
        return this.requestId;
    }

    public int chunkX() {
        return this.chunkX;
    }

    public int chunkZ() {
        return this.chunkZ;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public long columnTimestamp() {
        return this.columnTimestamp;
    }

    public byte[] decompressedSections() {
        return this.sectionBytes;
    }

    public int estimatedBytes() {
        return this.sectionBytes.length + 25;
    }

    private static int dimensionToOrdinal(ResourceKey<Level> dim) {
        if (dim == Level.OVERWORLD) {
            return 0;
        }
        if (dim == Level.NETHER) {
            return 1;
        }
        return dim == Level.END ? 2 : -1;
    }

    private static void write(FriendlyByteBuf buf, VoxelColumnS2CPayload payload) {
        buf.writeVarInt(payload.requestId);
        buf.writeInt(payload.chunkX);
        buf.writeInt(payload.chunkZ);
        int ordinal = dimensionToOrdinal(payload.dimension);
        buf.writeVarInt(ordinal);
        if (ordinal == -1) {
            buf.writeUtf(payload.dimension.identifier().toString());
        }
        buf.writeLong(payload.columnTimestamp);
        buf.writeByteArray(payload.sectionBytes);
    }

    private static VoxelColumnS2CPayload read(FriendlyByteBuf buf) {
        ResourceKey<Level> resourceKeyCreate;
        int requestId = buf.readVarInt();
        int cx = buf.readInt();
        int cz = buf.readInt();
        int ordinal = buf.readVarInt();
        switch (ordinal) {
            case 0:
                resourceKeyCreate = Level.OVERWORLD;
                break;
            case 1:
                resourceKeyCreate = Level.NETHER;
                break;
            case 2:
                resourceKeyCreate = Level.END;
                break;
            default:
                resourceKeyCreate = ResourceKey.create(Registries.DIMENSION, Identifier.parse(buf.readUtf(256)));
                break;
        }
        ResourceKey<Level> dim = resourceKeyCreate;
        long columnTimestamp = buf.readLong();
        byte[] sectionBytes = buf.readByteArray(MAX_SECTIONS_SIZE);
        return new VoxelColumnS2CPayload(requestId, cx, cz, dim, columnTimestamp, sectionBytes);
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package dev.xantha.vss.paper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperNbtSectionSerializer.class */
final class PaperNbtSectionSerializer {
    private static final byte[] EMPTY = new byte[0];

    private PaperNbtSectionSerializer() {
    }

    static byte[] readAndSerializeSections(ChunkMap chunkMap, RegistryAccess registryAccess, int cx, int cz) throws Exception {
        CompoundTag chunkNbt;
        String statusStr;
        byte[] blockLightData;
        LevelChunkSection result;
        CompletableFuture<Optional<CompoundTag>> future = chunkMap.read(new ChunkPos(cx, cz));
        Optional<CompoundTag> optionalTag = future.get(10L, TimeUnit.SECONDS);
        if (optionalTag.isEmpty() || (statusStr = (chunkNbt = optionalTag.get()).getStringOr("Status", (String) null)) == null || ChunkStatus.byName(statusStr) != ChunkStatus.FULL) {
            return null;
        }
        PalettedContainerFactory factory = PalettedContainerFactory.create(registryAccess);
        Codec<PalettedContainer<BlockState>> blockStateCodec = factory.blockStatesContainerCodec();
        Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec = factory.biomeContainerCodec();
        Registry<Biome> biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);
        Holder.Reference<Biome> defaultBiome = biomeRegistry.getOrThrow(Biomes.PLAINS);
        IdMap<Holder<Biome>> biomeHolderMap = biomeRegistry.asHolderIdMap();
        Optional<ListTag> sectionsTag = chunkNbt.getList("sections");
        if (sectionsTag.isEmpty()) {
            return null;
        }
        ListTag<CompoundTag> sectionsList = sectionsTag.orElseThrow();
        ArrayList<C1ParsedSection> parsed = new ArrayList<>(sectionsList.size());
        for (CompoundTag sectionTag : sectionsList) {
            int sectionY = sectionTag.getIntOr("Y", Integer.MIN_VALUE);
            if (sectionY != Integer.MIN_VALUE && (result = parseSection(sectionTag, sectionY, blockStateCodec, biomeCodec, defaultBiome, biomeHolderMap, (blockLightData = (byte[]) sectionTag.getByteArray("BlockLight").orElse(EMPTY)))) != null) {
                byte[] skyLightData = (byte[]) sectionTag.getByteArray("SkyLight").orElse(EMPTY);
                parsed.add(new C1ParsedSection(sectionY, result, blockLightData, skyLightData));
            }
        }
        if (parsed.isEmpty()) {
            return new byte[0];
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(parsed.size() * 1024));
        try {
            buf.writeVarInt(parsed.size());
            for (C1ParsedSection p : parsed) {
                buf.writeByte(p.sectionY);
                p.section.write(buf);
                boolean hasBlockLight = p.blockLight.length == 2048;
                buf.writeBoolean(hasBlockLight);
                if (hasBlockLight) {
                    buf.writeBytes(p.blockLight);
                }
                boolean hasSkyLight = p.skyLight.length == 2048;
                buf.writeBoolean(hasSkyLight);
                if (hasSkyLight) {
                    buf.writeBytes(p.skyLight);
                }
            }
            byte[] result2 = new byte[buf.readableBytes()];
            buf.readBytes(result2);
            buf.release();
            return result2;
        } catch (Throwable th) {
            buf.release();
            throw th;
        }
    }

    /* JADX INFO: renamed from: dev.xantha.vss.paper.PaperNbtSectionSerializer$1ParsedSection, reason: invalid class name */
    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection.class */
    static final class C1ParsedSection extends Record {
        private final int sectionY;
        private final LevelChunkSection section;
        private final byte[] blockLight;
        private final byte[] skyLight;

        C1ParsedSection(int sectionY, LevelChunkSection section, byte[] blockLight, byte[] skyLight) {
            this.sectionY = sectionY;
            this.section = section;
            this.blockLight = blockLight;
            this.skyLight = skyLight;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, C1ParsedSection.class), C1ParsedSection.class, "sectionY;section;blockLight;skyLight", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->sectionY:I", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->section:Lnet/minecraft/world/level/chunk/LevelChunkSection;", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->blockLight:[B", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->skyLight:[B").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, C1ParsedSection.class), C1ParsedSection.class, "sectionY;section;blockLight;skyLight", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->sectionY:I", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->section:Lnet/minecraft/world/level/chunk/LevelChunkSection;", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->blockLight:[B", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->skyLight:[B").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, C1ParsedSection.class, Object.class), C1ParsedSection.class, "sectionY;section;blockLight;skyLight", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->sectionY:I", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->section:Lnet/minecraft/world/level/chunk/LevelChunkSection;", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->blockLight:[B", "FIELD:Ldev/xantha/vss/paper/PaperNbtSectionSerializer$1ParsedSection;->skyLight:[B").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int sectionY() {
            return this.sectionY;
        }

        public LevelChunkSection section() {
            return this.section;
        }

        public byte[] blockLight() {
            return this.blockLight;
        }

        public byte[] skyLight() {
            return this.skyLight;
        }
    }

    private static LevelChunkSection parseSection(CompoundTag sectionTag, int sectionY, Codec<PalettedContainer<BlockState>> blockStateCodec, Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec, Holder<Biome> defaultBiome, IdMap<Holder<Biome>> biomeHolderMap, byte[] blockLightData) {
        PalettedContainerRO<Holder<Biome>> biomes;
        LevelChunkSection section;
        Optional<CompoundTag> blockStatesOpt = sectionTag.getCompound("block_states");
        if (blockStatesOpt.isEmpty()) {
            return null;
        }
        DataResult<PalettedContainer<BlockState>> blockStatesResult = blockStateCodec.parse(NbtOps.INSTANCE, (Tag) blockStatesOpt.get());
        PalettedContainer<BlockState> blockStates = (PalettedContainer) blockStatesResult.result().orElse(null);
        if (blockStates == null) {
            return null;
        }
        Optional<CompoundTag> optBiomes = sectionTag.getCompound("biomes");
        if (optBiomes.isPresent()) {
            DataResult<PalettedContainerRO<Holder<Biome>>> biomesResult = biomeCodec.parse(NbtOps.INSTANCE, (Tag) optBiomes.get());
            biomes = (PalettedContainerRO) biomesResult.result().orElse(null);
        } else {
            biomes = null;
        }
        if (biomes instanceof PalettedContainer) {
            PalettedContainer<Holder<Biome>> biomeContainer = (PalettedContainer) biomes;
            section = new LevelChunkSection(blockStates, biomeContainer);
        } else {
            PalettedContainer<Holder<Biome>> defaultBiomeContainer = new PalettedContainer<>(defaultBiome, Strategy.createForBiomes(biomeHolderMap));
            section = new LevelChunkSection(blockStates, defaultBiomeContainer);
        }
        if (section.hasOnlyAir()) {
            if (blockLightData.length != 2048) {
                return null;
            }
            boolean hasLight = false;
            int length = blockLightData.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                byte b = blockLightData[i];
                if (b != 0) {
                    hasLight = true;
                    break;
                }
                i++;
            }
            if (!hasLight) {
                return null;
            }
        }
        return section;
    }
}

package dev.xantha.vss.paper;

import dev.xantha.vss.common.processing.LoadedColumnData;
import io.netty.buffer.Unpooled;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperSectionSerializer.class */
final class PaperSectionSerializer {
    private PaperSectionSerializer() {
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperSectionSerializer$SectionInfo.class */
    private static final class SectionInfo extends Record {
        private final int index;
        private final int sectionY;
        private final SectionPos sectionPos;
        private final DataLayer blLayer;
        private final boolean hasBlockLight;

        private SectionInfo(int index, int sectionY, SectionPos sectionPos, DataLayer blLayer, boolean hasBlockLight) {
            this.index = index;
            this.sectionY = sectionY;
            this.sectionPos = sectionPos;
            this.blLayer = blLayer;
            this.hasBlockLight = hasBlockLight;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, SectionInfo.class), SectionInfo.class, "index;sectionY;sectionPos;blLayer;hasBlockLight", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->index:I", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->sectionY:I", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->sectionPos:Lnet/minecraft/core/SectionPos;", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->blLayer:Lnet/minecraft/world/level/chunk/DataLayer;", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->hasBlockLight:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, SectionInfo.class), SectionInfo.class, "index;sectionY;sectionPos;blLayer;hasBlockLight", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->index:I", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->sectionY:I", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->sectionPos:Lnet/minecraft/core/SectionPos;", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->blLayer:Lnet/minecraft/world/level/chunk/DataLayer;", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->hasBlockLight:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, SectionInfo.class, Object.class), SectionInfo.class, "index;sectionY;sectionPos;blLayer;hasBlockLight", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->index:I", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->sectionY:I", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->sectionPos:Lnet/minecraft/core/SectionPos;", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->blLayer:Lnet/minecraft/world/level/chunk/DataLayer;", "FIELD:Ldev/xantha/vss/paper/PaperSectionSerializer$SectionInfo;->hasBlockLight:Z").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int index() {
            return this.index;
        }

        public int sectionY() {
            return this.sectionY;
        }

        public SectionPos sectionPos() {
            return this.sectionPos;
        }

        public DataLayer blLayer() {
            return this.blLayer;
        }

        public boolean hasBlockLight() {
            return this.hasBlockLight;
        }
    }

    static LoadedColumnData serializeColumn(ServerLevel level, LevelChunk chunk, int cx, int cz) {
        int minSectionY = level.getMinSectionY();
        LevelChunkSection[] sections = chunk.getSections();
        LevelLightEngine lightEngine = level.getLightEngine();
        LayerLightEventListener blockLightListener = lightEngine.getLayerListener(LightLayer.BLOCK);
        ArrayList<SectionInfo> includedSections = new ArrayList<>(sections.length);
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section != null) {
                int sectionY = minSectionY + i;
                SectionPos sectionPos = SectionPos.of(cx, sectionY, cz);
                DataLayer blLayer = blockLightListener.getDataLayerData(sectionPos);
                boolean hasBlockLight = blLayer != null && hasNonZeroData(blLayer);
                if (!section.hasOnlyAir() || hasBlockLight) {
                    includedSections.add(new SectionInfo(i, sectionY, sectionPos, blLayer, hasBlockLight));
                }
            }
        }
        if (includedSections.isEmpty()) {
            return new LoadedColumnData(cx, cz, null, 0);
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(sections.length * 1024));
        try {
            buf.writeVarInt(includedSections.size());
            LayerLightEventListener skyLightListener = lightEngine.getLayerListener(LightLayer.SKY);
            for (SectionInfo info : includedSections) {
                LevelChunkSection section2 = sections[info.index];
                buf.writeByte(info.sectionY);
                section2.write(buf);
                buf.writeBoolean(info.hasBlockLight);
                if (info.hasBlockLight) {
                    buf.writeBytes(info.blLayer.getData());
                }
                DataLayer slLayer = skyLightListener.getDataLayerData(info.sectionPos);
                boolean hasSkyLight = slLayer != null && hasNonZeroData(slLayer);
                buf.writeBoolean(hasSkyLight);
                if (hasSkyLight) {
                    buf.writeBytes(slLayer.getData());
                }
            }
            byte[] serialized = new byte[buf.readableBytes()];
            buf.readBytes(serialized);
            LoadedColumnData loadedColumnData = new LoadedColumnData(cx, cz, serialized, serialized.length);
            buf.release();
            return loadedColumnData;
        } catch (Throwable th) {
            buf.release();
            throw th;
        }
    }

    private static boolean hasNonZeroData(DataLayer layer) {
        for (byte b : layer.getData()) {
            if (b != 0) {
                return true;
            }
        }
        return false;
    }
}

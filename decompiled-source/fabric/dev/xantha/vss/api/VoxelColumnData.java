package dev.xantha.vss.api;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/api/VoxelColumnData.class */
public final class VoxelColumnData extends Record {
    private final SectionData[] sections;
    private final long columnTimestamp;

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/api/VoxelColumnData$SectionData.class */
    public static final class SectionData extends Record {
        private final int sectionY;
        private final LevelChunkSection section;
        private final DataLayer blockLight;
        private final DataLayer skyLight;

        public SectionData(int sectionY, LevelChunkSection section, DataLayer blockLight, DataLayer skyLight) {
            this.sectionY = sectionY;
            this.section = section;
            this.blockLight = blockLight;
            this.skyLight = skyLight;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, SectionData.class), SectionData.class, "sectionY;section;blockLight;skyLight", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->sectionY:I", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->section:Lnet/minecraft/world/level/chunk/LevelChunkSection;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->blockLight:Lnet/minecraft/world/level/chunk/DataLayer;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->skyLight:Lnet/minecraft/world/level/chunk/DataLayer;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, SectionData.class), SectionData.class, "sectionY;section;blockLight;skyLight", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->sectionY:I", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->section:Lnet/minecraft/world/level/chunk/LevelChunkSection;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->blockLight:Lnet/minecraft/world/level/chunk/DataLayer;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->skyLight:Lnet/minecraft/world/level/chunk/DataLayer;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, SectionData.class, Object.class), SectionData.class, "sectionY;section;blockLight;skyLight", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->sectionY:I", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->section:Lnet/minecraft/world/level/chunk/LevelChunkSection;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->blockLight:Lnet/minecraft/world/level/chunk/DataLayer;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData$SectionData;->skyLight:Lnet/minecraft/world/level/chunk/DataLayer;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int sectionY() {
            return this.sectionY;
        }

        public LevelChunkSection section() {
            return this.section;
        }

        public DataLayer blockLight() {
            return this.blockLight;
        }

        public DataLayer skyLight() {
            return this.skyLight;
        }
    }

    public VoxelColumnData(SectionData[] sections, long columnTimestamp) {
        this.sections = sections;
        this.columnTimestamp = columnTimestamp;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, VoxelColumnData.class), VoxelColumnData.class, "sections;columnTimestamp", "FIELD:Ldev/xantha/vss/api/VoxelColumnData;->sections:[Ldev/xantha/vss/api/VoxelColumnData$SectionData;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData;->columnTimestamp:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, VoxelColumnData.class), VoxelColumnData.class, "sections;columnTimestamp", "FIELD:Ldev/xantha/vss/api/VoxelColumnData;->sections:[Ldev/xantha/vss/api/VoxelColumnData$SectionData;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData;->columnTimestamp:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, VoxelColumnData.class, Object.class), VoxelColumnData.class, "sections;columnTimestamp", "FIELD:Ldev/xantha/vss/api/VoxelColumnData;->sections:[Ldev/xantha/vss/api/VoxelColumnData$SectionData;", "FIELD:Ldev/xantha/vss/api/VoxelColumnData;->columnTimestamp:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public SectionData[] sections() {
        return this.sections;
    }

    public long columnTimestamp() {
        return this.columnTimestamp;
    }
}

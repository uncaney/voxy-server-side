package dev.xantha.vss.compat;

import dev.xantha.vss.api.VSSApi;
import dev.xantha.vss.api.VoxelColumnData;
import dev.xantha.vss.common.VSSLogger;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.OptionalInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/compat/VoxyCompat.class */
class VoxyCompat {
    private static MethodHandle worldIdentifierOf;
    private static MethodHandle rawIngest;
    private static volatile MethodHandle getVoxyConfig;
    private static volatile MethodHandle getSectionRenderDist;

    VoxyCompat() {
    }

    static boolean init() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> worldIdClass = Class.forName("me.cortex.voxy.commonImpl.WorldIdentifier");
            worldIdentifierOf = lookup.findStatic(worldIdClass, "of", MethodType.methodType(worldIdClass, (Class<?>) Level.class)).asType(MethodType.methodType((Class<?>) Object.class, (Class<?>) Level.class));
            Class<?> ingestClass = Class.forName("me.cortex.voxy.common.world.service.VoxelIngestService");
            rawIngest = lookup.findStatic(ingestClass, "rawIngest", MethodType.methodType(Boolean.TYPE, worldIdClass, LevelChunkSection.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, DataLayer.class, DataLayer.class));
            VSSApi.registerColumnConsumer((level, dimension, chunkX, chunkZ, columnData) -> {
                try {
                    Object worldId = (Object) worldIdentifierOf.invoke(level);
                    if (worldId == null) {
                        return;
                    }
                    VoxelColumnData.SectionData[] arr$ = columnData.sections();
                    for (VoxelColumnData.SectionData s : arr$) {
                        (void) rawIngest.invoke(worldId, s.section(), chunkX, s.sectionY(), chunkZ, s.blockLight(), s.skyLight());
                    }
                } catch (Throwable e) {
                    if ((e instanceof Error) && !(e instanceof LinkageError) && !(e instanceof AssertionError)) {
                        throw ((Error) e);
                    }
                    VSSLogger.error("Voxy raw ingest failed", e);
                }
            });
            VSSLogger.info("Voxy detected — registered raw ingest bridge");
            return true;
        } catch (ClassNotFoundException e) {
            VSSLogger.warn("Voxy compat: class not found — " + e.getMessage());
            return false;
        } catch (NoSuchMethodException e2) {
            VSSLogger.warn("Voxy compat: method not found — " + e2.getMessage());
            return false;
        } catch (Throwable e3) {
            VSSLogger.error("Failed to initialize Voxy compat", e3);
            return false;
        }
    }

    private static void initConfigHandles() throws Throwable {
        if (getVoxyConfig != null) {
            return;
        }
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> voxyConfigClass = Class.forName("me.cortex.voxy.client.config.VoxyConfig");
        Field configField = voxyConfigClass.getField("CONFIG");
        getSectionRenderDist = lookup.findGetter(voxyConfigClass, "sectionRenderDistance", Float.TYPE).asType(MethodType.methodType((Class<?>) Float.TYPE, (Class<?>) Object.class));
        getVoxyConfig = lookup.unreflectGetter(configField).asType(MethodType.methodType(Object.class));
    }

    static OptionalInt getViewDistanceChunks() {
        try {
            initConfigHandles();
            Object config = (Object) getVoxyConfig.invokeExact();
            float sectionDist = (float) getSectionRenderDist.invokeExact(config);
            return OptionalInt.of(Math.round(sectionDist * 32.0f));
        } catch (Throwable th) {
            return OptionalInt.empty();
        }
    }
}

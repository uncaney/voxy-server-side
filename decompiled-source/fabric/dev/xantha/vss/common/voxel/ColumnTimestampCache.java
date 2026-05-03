package dev.xantha.vss.common.voxel;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/voxel/ColumnTimestampCache.class */
public class ColumnTimestampCache {
    private static final int FORMAT_VERSION = 1;
    private static final String FILE_NAME = "vss-timestamps.bin";
    private static final int BYTES_PER_ENTRY = 16;
    private final Map<String, DimensionCache> caches = new HashMap();
    private final int maxEntriesPerDimension;

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/voxel/ColumnTimestampCache$DimensionCache.class */
    private static final class DimensionCache extends Record {
        private final Long2LongOpenHashMap timestamps;
        private final Long2LongOpenHashMap insertionTimes;

        private DimensionCache(Long2LongOpenHashMap timestamps, Long2LongOpenHashMap insertionTimes) {
            this.timestamps = timestamps;
            this.insertionTimes = insertionTimes;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, DimensionCache.class), DimensionCache.class, "timestamps;insertionTimes", "FIELD:Ldev/xantha/vss/common/voxel/ColumnTimestampCache$DimensionCache;->timestamps:Lit/unimi/dsi/fastutil/longs/Long2LongOpenHashMap;", "FIELD:Ldev/xantha/vss/common/voxel/ColumnTimestampCache$DimensionCache;->insertionTimes:Lit/unimi/dsi/fastutil/longs/Long2LongOpenHashMap;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, DimensionCache.class), DimensionCache.class, "timestamps;insertionTimes", "FIELD:Ldev/xantha/vss/common/voxel/ColumnTimestampCache$DimensionCache;->timestamps:Lit/unimi/dsi/fastutil/longs/Long2LongOpenHashMap;", "FIELD:Ldev/xantha/vss/common/voxel/ColumnTimestampCache$DimensionCache;->insertionTimes:Lit/unimi/dsi/fastutil/longs/Long2LongOpenHashMap;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, DimensionCache.class, Object.class), DimensionCache.class, "timestamps;insertionTimes", "FIELD:Ldev/xantha/vss/common/voxel/ColumnTimestampCache$DimensionCache;->timestamps:Lit/unimi/dsi/fastutil/longs/Long2LongOpenHashMap;", "FIELD:Ldev/xantha/vss/common/voxel/ColumnTimestampCache$DimensionCache;->insertionTimes:Lit/unimi/dsi/fastutil/longs/Long2LongOpenHashMap;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public Long2LongOpenHashMap timestamps() {
            return this.timestamps;
        }

        public Long2LongOpenHashMap insertionTimes() {
            return this.insertionTimes;
        }

        DimensionCache() {
            this(new Long2LongOpenHashMap(), new Long2LongOpenHashMap());
        }
    }

    public ColumnTimestampCache(int maxEntriesPerDimension) {
        this.maxEntriesPerDimension = maxEntriesPerDimension;
    }

    public static int mbToEntries(int mb) {
        return mb * 65536;
    }

    public void put(String dimension, long packed, long timestamp, long now) {
        DimensionCache cache = this.caches.computeIfAbsent(dimension, k -> {
            return new DimensionCache();
        });
        cache.timestamps.put(packed, timestamp);
        cache.insertionTimes.put(packed, now);
    }

    public long get(String dimension, long packed) {
        DimensionCache cache = this.caches.get(dimension);
        if (cache == null) {
            return 0L;
        }
        return cache.timestamps.getOrDefault(packed, 0L);
    }

    public void invalidate(String dimension, long[] positions) {
        DimensionCache cache = this.caches.get(dimension);
        if (cache == null) {
            return;
        }
        for (long pos : positions) {
            cache.timestamps.remove(pos);
            cache.insertionTimes.remove(pos);
        }
    }

    public int evictIfOversized() {
        int evicted = 0;
        for (DimensionCache cache : this.caches.values()) {
            int excess = cache.timestamps.size() - this.maxEntriesPerDimension;
            if (excess > 0) {
                evicted += evictOldest(cache, excess);
            }
        }
        return evicted;
    }

    private int evictOldest(DimensionCache cache, int count) {
        long[] keys = new long[count];
        long[] times = new long[count];
        int found = 0;
        ObjectIterator it = cache.insertionTimes.long2LongEntrySet().iterator();
        while (it.hasNext()) {
            Long2LongMap.Entry e = (Long2LongMap.Entry) it.next();
            long t = e.getLongValue();
            if (found < count) {
                keys[found] = e.getLongKey();
                times[found] = t;
                found++;
            } else {
                int maxIdx = 0;
                for (int i = 1; i < count; i++) {
                    if (times[i] > times[maxIdx]) {
                        maxIdx = i;
                    }
                }
                if (t < times[maxIdx]) {
                    keys[maxIdx] = e.getLongKey();
                    times[maxIdx] = t;
                }
            }
        }
        for (int i2 = 0; i2 < found; i2++) {
            cache.timestamps.remove(keys[i2]);
            cache.insertionTimes.remove(keys[i2]);
        }
        return found;
    }

    public int size() {
        int total = 0;
        for (DimensionCache cache : this.caches.values()) {
            total += cache.timestamps.size();
        }
        return total;
    }

    public void save(Path dataDir) {
        if (this.caches.isEmpty()) {
            return;
        }
        Path file = dataDir.resolve(FILE_NAME);
        Path tmpFile = file.resolveSibling("vss-timestamps.bin.tmp");
        try {
            Files.createDirectories(dataDir, new FileAttribute[0]);
            DataOutputStream out = new DataOutputStream(Files.newOutputStream(tmpFile, new OpenOption[0]));
            try {
                out.writeInt(1);
                out.writeInt(this.caches.size());
                for (Map.Entry<String, DimensionCache> entry : this.caches.entrySet()) {
                    out.writeUTF(entry.getKey());
                    DimensionCache dimCache = entry.getValue();
                    out.writeInt(dimCache.timestamps.size());
                    ObjectIterator it = dimCache.timestamps.long2LongEntrySet().iterator();
                    while (it.hasNext()) {
                        Long2LongMap.Entry e = (Long2LongMap.Entry) it.next();
                        out.writeLong(e.getLongKey());
                        out.writeLong(e.getLongValue());
                    }
                }
                out.close();
                Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                if (VSSLogger.isDebugEnabled()) {
                    VSSLogger.debug("Saved " + size() + " timestamp cache entries to " + String.valueOf(file));
                }
            } finally {
            }
        } catch (IOException e2) {
            VSSLogger.warn("Failed to save timestamp cache to " + String.valueOf(file), e2);
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException e22) {
                VSSLogger.warn("Failed to clean up temporary timestamp cache file " + String.valueOf(tmpFile), e22);
            }
        }
    }

    public void load(Path dataDir) {
        Path file = dataDir.resolve(FILE_NAME);
        if (Files.exists(file, new LinkOption[0])) {
            long now = VSSConstants.epochSeconds();
            int totalLoaded = 0;
            try {
                DataInputStream in = new DataInputStream(Files.newInputStream(file, new OpenOption[0]));
                try {
                    int version = in.readInt();
                    if (version != 1) {
                        VSSLogger.warn("Timestamp cache " + String.valueOf(file) + " has unsupported version " + version + ", discarding");
                        in.close();
                        return;
                    }
                    int dimCount = in.readInt();
                    for (int d = 0; d < dimCount; d++) {
                        String dimension = in.readUTF();
                        int entryCount = in.readInt();
                        if (entryCount < 0 || entryCount > this.maxEntriesPerDimension) {
                            VSSLogger.warn("Timestamp cache dimension " + dimension + " has invalid count " + entryCount + ", skipping");
                            in.skipBytes(entryCount * BYTES_PER_ENTRY);
                        } else {
                            DimensionCache cache = this.caches.computeIfAbsent(dimension, k -> {
                                return new DimensionCache();
                            });
                            cache.timestamps.ensureCapacity(entryCount);
                            cache.insertionTimes.ensureCapacity(entryCount);
                            for (int i = 0; i < entryCount; i++) {
                                long packed = in.readLong();
                                long timestamp = in.readLong();
                                cache.timestamps.put(packed, timestamp);
                                cache.insertionTimes.put(packed, now);
                            }
                            totalLoaded += entryCount;
                        }
                    }
                    VSSLogger.info("Loaded " + totalLoaded + " timestamp cache entries from " + String.valueOf(file));
                    in.close();
                } finally {
                }
            } catch (IOException e) {
                VSSLogger.warn("Failed to load timestamp cache from " + String.valueOf(file), e);
            }
        }
    }

    public ColumnTimestampCache snapshotForSave() {
        ColumnTimestampCache snapshot = new ColumnTimestampCache(this.maxEntriesPerDimension);
        for (Map.Entry<String, DimensionCache> entry : this.caches.entrySet()) {
            DimensionCache dimCache = entry.getValue();
            DimensionCache copy = new DimensionCache(new Long2LongOpenHashMap(dimCache.timestamps), new Long2LongOpenHashMap());
            snapshot.caches.put(entry.getKey(), copy);
        }
        return snapshot;
    }
}

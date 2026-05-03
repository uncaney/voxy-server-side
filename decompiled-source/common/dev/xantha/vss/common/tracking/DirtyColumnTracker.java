package dev.xantha.vss.common.tracking;

import dev.xantha.vss.common.PositionUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/tracking/DirtyColumnTracker.class */
public class DirtyColumnTracker {
    private final Map<String, LongOpenHashSet> dirtyColumns = new HashMap();

    public synchronized void markDirty(String dimension, int cx, int cz) {
        long packed = PositionUtil.packPosition(cx, cz);
        this.dirtyColumns.computeIfAbsent(dimension, k -> {
            return new LongOpenHashSet();
        }).add(packed);
    }

    public synchronized long[] drainDirty(String dimension) {
        LongOpenHashSet set = this.dirtyColumns.get(dimension);
        if (set == null || set.isEmpty()) {
            return null;
        }
        long[] result = set.toLongArray();
        set.clear();
        return result;
    }
}

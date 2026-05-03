package dev.xantha.vss.networking.client;

import dev.xantha.vss.common.PositionUtil;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.function.IntConsumer;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/InFlightTracker.class */
class InFlightTracker {
    private final Long2LongOpenHashMap pendingRequests = new Long2LongOpenHashMap();
    private int nextRequestId;
    private final Int2LongOpenHashMap requestIdToPosition;
    private final Long2IntOpenHashMap positionToRequestId;
    private final LongOpenHashSet generationPositions;

    InFlightTracker() {
        this.pendingRequests.defaultReturnValue(0L);
        this.nextRequestId = 0;
        this.requestIdToPosition = new Int2LongOpenHashMap();
        this.requestIdToPosition.defaultReturnValue(Long.MIN_VALUE);
        this.positionToRequestId = new Long2IntOpenHashMap();
        this.positionToRequestId.defaultReturnValue(-1);
        this.generationPositions = new LongOpenHashSet();
    }

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/InFlightTracker$RemovedRequest.class */
    static final class RemovedRequest extends Record {
        private final long position;

        RemovedRequest(long position) {
            this.position = position;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, RemovedRequest.class), RemovedRequest.class, "position", "FIELD:Ldev/xantha/vss/networking/client/InFlightTracker$RemovedRequest;->position:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, RemovedRequest.class), RemovedRequest.class, "position", "FIELD:Ldev/xantha/vss/networking/client/InFlightTracker$RemovedRequest;->position:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, RemovedRequest.class, Object.class), RemovedRequest.class, "position", "FIELD:Ldev/xantha/vss/networking/client/InFlightTracker$RemovedRequest;->position:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public long position() {
            return this.position;
        }
    }

    int send(long position) {
        int requestId = this.nextRequestId;
        this.nextRequestId = requestId + 1;
        this.requestIdToPosition.put(requestId, position);
        this.positionToRequestId.put(position, requestId);
        return requestId;
    }

    void markPending(long position, long sendTimeNanos, boolean isGeneration) {
        this.pendingRequests.put(position, sendTimeNanos);
        if (isGeneration) {
            this.generationPositions.add(position);
        }
    }

    RemovedRequest removeByRequestId(int requestId) {
        long pos = removeAllByRequestId(requestId);
        if (pos != Long.MIN_VALUE) {
            return new RemovedRequest(pos);
        }
        return null;
    }

    private long removeAllByRequestId(int requestId) {
        long pos = this.requestIdToPosition.remove(requestId);
        if (pos == Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        this.positionToRequestId.remove(pos);
        this.pendingRequests.remove(pos);
        this.generationPositions.remove(pos);
        return pos;
    }

    boolean isInFlight(long position) {
        return this.pendingRequests.containsKey(position);
    }

    int size() {
        return this.pendingRequests.size();
    }

    int generationCount() {
        return this.generationPositions.size();
    }

    private int removeFromSecondaryMaps(long pos) {
        int requestId = this.positionToRequestId.remove(pos);
        if (requestId != -1) {
            this.requestIdToPosition.remove(requestId);
        }
        return requestId;
    }

    void timeoutSweep(long thresholdNanos) {
        long now = System.nanoTime();
        ObjectIterator<Long2LongMap.Entry> iter = this.pendingRequests.long2LongEntrySet().iterator();
        while (iter.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry) iter.next();
            if (now - entry.getLongValue() > thresholdNanos) {
                long pos = entry.getLongKey();
                removeFromSecondaryMaps(pos);
                this.generationPositions.remove(pos);
                iter.remove();
            }
        }
    }

    void pruneOutOfRange(int playerCx, int playerCz, int pruneDistance, IntConsumer cancelCallback) {
        ObjectIterator<Long2LongMap.Entry> iter = this.pendingRequests.long2LongEntrySet().iterator();
        while (iter.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry) iter.next();
            long pos = entry.getLongKey();
            if (PositionUtil.isOutOfRange(pos, playerCx, playerCz, pruneDistance)) {
                int requestId = removeFromSecondaryMaps(pos);
                this.generationPositions.remove(pos);
                if (requestId != -1) {
                    cancelCallback.accept(requestId);
                }
                iter.remove();
            }
        }
    }

    void forEachRequestId(IntConsumer callback) {
        ObjectIterator it = this.positionToRequestId.long2IntEntrySet().iterator();
        while (it.hasNext()) {
            Long2IntMap.Entry entry = (Long2IntMap.Entry) it.next();
            callback.accept(entry.getIntValue());
        }
    }

    void clear() {
        this.nextRequestId = 0;
        this.pendingRequests.clear();
        this.requestIdToPosition.clear();
        this.positionToRequestId.clear();
        this.generationPositions.clear();
    }
}

package dev.xantha.vss.common.processing;

import dev.xantha.vss.common.PositionUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.lang.Comparable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/AbstractPlayerRequestState.class */
public abstract class AbstractPlayerRequestState<Q extends Comparable<Q>> implements PlayerStateAccess {
    private final UUID playerUuid;
    private final RateLimiterSet rateLimiters;
    private volatile boolean hasHandshake = false;
    private volatile int capabilities = 0;
    private final ConcurrentLinkedQueue<IncomingRequest> incomingRequests = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Integer> incomingCancels = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<long[]> pendingDirtyClear = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Q> readyPayloads = new ConcurrentLinkedQueue<>();
    private final Long2ObjectOpenHashMap<PendingRequest> pendingByPosition = new Long2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<PendingRequest> pendingByRequestId = new Int2ObjectOpenHashMap<>();
    private final PriorityQueue<Q> sendQueue = new PriorityQueue<>();
    private final LongOpenHashSet diskReadDone = new LongOpenHashSet();
    private final PlayerBandwidthTracker bandwidth = new PlayerBandwidthTracker();
    private final ArrayDeque<QueuedRequest> waitingQueue = new ArrayDeque<>();
    private final AtomicLong totalRequestsReceived = new AtomicLong();
    private volatile long desiredBandwidth = Long.MAX_VALUE;
    private volatile int sendQueueSizeSnapshot = 0;
    private volatile int pendingSyncCount = 0;
    private volatile int pendingGenerationCount = 0;

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest.class */
    public static final class QueuedRequest extends Record {
        private final IncomingRequest request;
        private final RequestType type;
        private final String dimension;

        public QueuedRequest(IncomingRequest request, RequestType type, String dimension) {
            this.request = request;
            this.type = type;
            this.dimension = dimension;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, QueuedRequest.class), QueuedRequest.class, "request;type;dimension", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->request:Ldev/xantha/vss/common/processing/IncomingRequest;", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->type:Ldev/xantha/vss/common/processing/RequestType;", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->dimension:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, QueuedRequest.class), QueuedRequest.class, "request;type;dimension", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->request:Ldev/xantha/vss/common/processing/IncomingRequest;", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->type:Ldev/xantha/vss/common/processing/RequestType;", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->dimension:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, QueuedRequest.class, Object.class), QueuedRequest.class, "request;type;dimension", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->request:Ldev/xantha/vss/common/processing/IncomingRequest;", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->type:Ldev/xantha/vss/common/processing/RequestType;", "FIELD:Ldev/xantha/vss/common/processing/AbstractPlayerRequestState$QueuedRequest;->dimension:Ljava/lang/String;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public IncomingRequest request() {
            return this.request;
        }

        public RequestType type() {
            return this.type;
        }

        public String dimension() {
            return this.dimension;
        }
    }

    protected AbstractPlayerRequestState(UUID playerUuid, int syncRate, int syncConcurrency, int genRate, int genConcurrency) {
        this.playerUuid = playerUuid;
        this.rateLimiters = new RateLimiterSet(syncRate, syncConcurrency, genRate, genConcurrency);
    }

    public void setCapabilities(int capabilities) {
        this.capabilities = capabilities;
    }

    public int getCapabilities() {
        return this.capabilities;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public boolean supportsVoxelColumns() {
        return (this.capabilities & 1) != 0;
    }

    public void markHandshakeComplete() {
        this.hasHandshake = true;
    }

    public boolean hasCompletedHandshake() {
        return this.hasHandshake;
    }

    protected void enqueueIncomingRequest(IncomingRequest request) {
        this.incomingRequests.add(request);
        this.totalRequestsReceived.incrementAndGet();
    }

    public void addCancel(int requestId) {
        this.incomingCancels.add(Integer.valueOf(requestId));
    }

    public void drainReadyPayloads() {
        while (true) {
            Q qp = this.readyPayloads.poll();
            if (qp != null) {
                this.sendQueue.add(qp);
            } else {
                this.sendQueueSizeSnapshot = this.sendQueue.size();
                return;
            }
        }
    }

    public boolean canSend(long allocationBytes) {
        return this.bandwidth.canSend(allocationBytes);
    }

    public void recordSend(int bytes) {
        this.bandwidth.recordSend(bytes);
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public void drainDirtyClearRequests() {
        while (true) {
            long[] dirtyPositions = this.pendingDirtyClear.poll();
            if (dirtyPositions == null) {
                return;
            }
            for (long pos : dirtyPositions) {
                getDiskReadDonePositions().remove(pos);
            }
        }
    }

    public void clearDiskReadDoneForPositions(long[] positions) {
        this.pendingDirtyClear.add(positions);
    }

    protected void onDimensionChangeBase() {
        this.incomingRequests.clear();
        this.incomingCancels.clear();
        this.readyPayloads.clear();
        this.sendQueue.clear();
        this.pendingDirtyClear.clear();
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public void clearProcessingState() {
        this.pendingByPosition.clear();
        this.pendingByRequestId.clear();
        this.diskReadDone.clear();
        this.waitingQueue.clear();
        this.pendingSyncCount = 0;
        this.pendingGenerationCount = 0;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public IncomingRequest pollIncomingRequest() {
        return this.incomingRequests.poll();
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public void addPendingRequest(PendingRequest pending) {
        long packed = PositionUtil.packPosition(pending.cx(), pending.cz());
        PendingRequest replaced = (PendingRequest) this.pendingByPosition.put(packed, pending);
        if (replaced != null) {
            this.pendingByRequestId.remove(replaced.requestId());
            decrementPendingCounter(replaced.type());
        }
        this.pendingByRequestId.put(pending.requestId(), pending);
        incrementPendingCounter(pending.type());
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public PendingRequest removePendingByPosition(int cx, int cz) {
        long packed = PositionUtil.packPosition(cx, cz);
        PendingRequest pending = (PendingRequest) this.pendingByPosition.remove(packed);
        if (pending != null) {
            this.pendingByRequestId.remove(pending.requestId());
            decrementPendingCounter(pending.type());
        }
        return pending;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public PendingRequest removePendingByRequestId(int requestId) {
        PendingRequest pending = (PendingRequest) this.pendingByRequestId.remove(requestId);
        if (pending != null) {
            long packed = PositionUtil.packPosition(pending.cx(), pending.cz());
            this.pendingByPosition.remove(packed);
            decrementPendingCounter(pending.type());
        }
        return pending;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public boolean hasPendingRequest(int cx, int cz) {
        return this.pendingByPosition.containsKey(PositionUtil.packPosition(cx, cz));
    }

    private void incrementPendingCounter(RequestType type) {
        if (type != RequestType.SYNC) {
            this.pendingGenerationCount++;
        } else {
            this.pendingSyncCount++;
        }
    }

    private void decrementPendingCounter(RequestType type) {
        if (type != RequestType.SYNC) {
            this.pendingGenerationCount--;
        } else {
            this.pendingSyncCount--;
        }
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public Integer pollCancel() {
        return this.incomingCancels.poll();
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public boolean hasDiskReadDone(int cx, int cz) {
        return this.diskReadDone.contains(PositionUtil.packPosition(cx, cz));
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public void markDiskReadDone(int cx, int cz) {
        this.diskReadDone.add(PositionUtil.packPosition(cx, cz));
    }

    public Iterable<IncomingRequest> getIncomingRequests() {
        return this.incomingRequests;
    }

    public void addReadyPayload(Q payload) {
        this.readyPayloads.add(payload);
    }

    protected LongOpenHashSet getDiskReadDonePositions() {
        return this.diskReadDone;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public RateLimiterSet getRateLimiters() {
        return this.rateLimiters;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public ArrayDeque<QueuedRequest> getWaitingQueue() {
        return this.waitingQueue;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public int getWaitingQueueSize() {
        return this.waitingQueue.size();
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public UUID getPlayerUUID() {
        return this.playerUuid;
    }

    public PriorityQueue<Q> getSendQueue() {
        return this.sendQueue;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public int getSendQueueSize() {
        return this.sendQueueSizeSnapshot;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public int getPendingSyncCount() {
        return this.pendingSyncCount;
    }

    @Override // dev.xantha.vss.common.processing.PlayerStateAccess
    public int getPendingGenerationCount() {
        return this.pendingGenerationCount;
    }

    public long getTotalSectionsSent() {
        return this.bandwidth.getTotalSectionsSent();
    }

    public long getTotalBytesSent() {
        return this.bandwidth.getTotalBytesSent();
    }

    public long getTotalRequestsReceived() {
        return this.totalRequestsReceived.get();
    }

    public void setDesiredBandwidth(long desiredRate) {
        this.desiredBandwidth = desiredRate;
    }

    public long getDesiredBandwidth() {
        return this.desiredBandwidth;
    }
}

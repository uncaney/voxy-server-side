package dev.xantha.vss.common.processing;

import dev.xantha.vss.common.processing.AbstractPlayerRequestState;
import java.util.ArrayDeque;
import java.util.UUID;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/processing/PlayerStateAccess.class */
public interface PlayerStateAccess {
    void drainDirtyClearRequests();

    void clearProcessingState();

    boolean hasDiskReadDone(int i, int i2);

    void markDiskReadDone(int i, int i2);

    int getSendQueueSize();

    int getPendingSyncCount();

    int getPendingGenerationCount();

    boolean supportsVoxelColumns();

    UUID getPlayerUUID();

    RateLimiterSet getRateLimiters();

    ArrayDeque<AbstractPlayerRequestState.QueuedRequest> getWaitingQueue();

    int getWaitingQueueSize();

    IncomingRequest pollIncomingRequest();

    void addPendingRequest(PendingRequest pendingRequest);

    PendingRequest removePendingByPosition(int i, int i2);

    PendingRequest removePendingByRequestId(int i);

    boolean hasPendingRequest(int i, int i2);

    Integer pollCancel();
}

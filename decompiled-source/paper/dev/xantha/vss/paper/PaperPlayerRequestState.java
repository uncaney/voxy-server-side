package dev.xantha.vss.paper;

import dev.xantha.vss.common.processing.AbstractPlayerRequestState;
import dev.xantha.vss.common.processing.IncomingRequest;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperPlayerRequestState.class */
public class PaperPlayerRequestState extends AbstractPlayerRequestState<QueuedPayload> {
    private volatile ServerPlayer player;
    private ResourceKey<Level> lastDimension;

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload.class */
    public static final class QueuedPayload extends Record implements Comparable<QueuedPayload> {
        private final byte[] data;
        private final int requestId;
        private final int estimatedBytes;
        private final long submissionOrder;

        public QueuedPayload(byte[] data, int requestId, int estimatedBytes, long submissionOrder) {
            this.data = data;
            this.requestId = requestId;
            this.estimatedBytes = estimatedBytes;
            this.submissionOrder = submissionOrder;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, QueuedPayload.class), QueuedPayload.class, "data;requestId;estimatedBytes;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->data:[B", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->estimatedBytes:I", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, QueuedPayload.class), QueuedPayload.class, "data;requestId;estimatedBytes;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->data:[B", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->estimatedBytes:I", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, QueuedPayload.class, Object.class), QueuedPayload.class, "data;requestId;estimatedBytes;submissionOrder", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->data:[B", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->requestId:I", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->estimatedBytes:I", "FIELD:Ldev/xantha/vss/paper/PaperPlayerRequestState$QueuedPayload;->submissionOrder:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public byte[] data() {
            return this.data;
        }

        public int requestId() {
            return this.requestId;
        }

        public int estimatedBytes() {
            return this.estimatedBytes;
        }

        public long submissionOrder() {
            return this.submissionOrder;
        }

        @Override // java.lang.Comparable
        public int compareTo(QueuedPayload other) {
            return Long.compare(this.submissionOrder, other.submissionOrder);
        }
    }

    public PaperPlayerRequestState(ServerPlayer player, int syncRate, int syncConcurrency, int genRate, int genConcurrency) {
        super(player.getUUID(), syncRate, syncConcurrency, genRate, genConcurrency);
        this.player = player;
        this.lastDimension = player.level().dimension();
    }

    public void addRequest(int requestId, int cx, int cz, long clientTimestamp) {
        enqueueIncomingRequest(new IncomingRequest(requestId, cx, cz, clientTimestamp));
    }

    public void onDimensionChange() {
        onDimensionChangeBase();
    }

    public void updatePlayer(ServerPlayer newPlayer) {
        this.player = newPlayer;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public ResourceKey<Level> getLastDimension() {
        return this.lastDimension;
    }

    public boolean checkDimensionChange() {
        ResourceKey<Level> currentDim = this.player.level().dimension();
        if (!currentDim.equals(this.lastDimension)) {
            this.lastDimension = currentDim;
            return true;
        }
        return false;
    }
}

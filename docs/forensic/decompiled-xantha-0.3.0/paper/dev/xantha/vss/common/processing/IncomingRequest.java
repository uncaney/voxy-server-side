package dev.xantha.vss.common.processing;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/IncomingRequest.class */
public final class IncomingRequest extends Record {
    private final int requestId;
    private final int cx;
    private final int cz;
    private final long clientTimestamp;

    public IncomingRequest(int requestId, int cx, int cz, long clientTimestamp) {
        this.requestId = requestId;
        this.cx = cx;
        this.cz = cz;
        this.clientTimestamp = clientTimestamp;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, IncomingRequest.class), IncomingRequest.class, "requestId;cx;cz;clientTimestamp", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->clientTimestamp:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, IncomingRequest.class), IncomingRequest.class, "requestId;cx;cz;clientTimestamp", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->clientTimestamp:J").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, IncomingRequest.class, Object.class), IncomingRequest.class, "requestId;cx;cz;clientTimestamp", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/IncomingRequest;->clientTimestamp:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public int requestId() {
        return this.requestId;
    }

    public int cx() {
        return this.cx;
    }

    public int cz() {
        return this.cz;
    }

    public long clientTimestamp() {
        return this.clientTimestamp;
    }
}

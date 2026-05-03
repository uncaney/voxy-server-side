package dev.xantha.vss.common.processing;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/PendingRequest.class */
public final class PendingRequest extends Record {
    private final int requestId;
    private final int cx;
    private final int cz;
    private final RequestType type;

    public PendingRequest(int requestId, int cx, int cz, RequestType type) {
        this.requestId = requestId;
        this.cx = cx;
        this.cz = cz;
        this.type = type;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, PendingRequest.class), PendingRequest.class, "requestId;cx;cz;type", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->type:Ldev/xantha/vss/common/processing/RequestType;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, PendingRequest.class), PendingRequest.class, "requestId;cx;cz;type", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->type:Ldev/xantha/vss/common/processing/RequestType;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, PendingRequest.class, Object.class), PendingRequest.class, "requestId;cx;cz;type", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/PendingRequest;->type:Ldev/xantha/vss/common/processing/RequestType;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
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

    public RequestType type() {
        return this.type;
    }
}

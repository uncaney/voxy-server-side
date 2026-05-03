package dev.xantha.vss.common.processing;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/LoadedColumnData.class */
public final class LoadedColumnData extends Record {
    private final int cx;
    private final int cz;
    private final byte[] serializedSections;
    private final int estimatedBytes;

    public LoadedColumnData(int cx, int cz, byte[] serializedSections, int estimatedBytes) {
        this.cx = cx;
        this.cz = cz;
        this.serializedSections = serializedSections;
        this.estimatedBytes = estimatedBytes;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, LoadedColumnData.class), LoadedColumnData.class, "cx;cz;serializedSections;estimatedBytes", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->serializedSections:[B", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->estimatedBytes:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, LoadedColumnData.class), LoadedColumnData.class, "cx;cz;serializedSections;estimatedBytes", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->serializedSections:[B", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->estimatedBytes:I").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, LoadedColumnData.class, Object.class), LoadedColumnData.class, "cx;cz;serializedSections;estimatedBytes", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->cx:I", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->cz:I", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->serializedSections:[B", "FIELD:Ldev/xantha/vss/common/processing/LoadedColumnData;->estimatedBytes:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public int cx() {
        return this.cx;
    }

    public int cz() {
        return this.cz;
    }

    public byte[] serializedSections() {
        return this.serializedSections;
    }

    public int estimatedBytes() {
        return this.estimatedBytes;
    }
}

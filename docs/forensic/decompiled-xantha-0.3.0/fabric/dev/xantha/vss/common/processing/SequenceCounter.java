package dev.xantha.vss.common.processing;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/processing/SequenceCounter.class */
public class SequenceCounter {
    private long value = 0;

    public long next() {
        long j = this.value;
        this.value = j + 1;
        return j;
    }
}

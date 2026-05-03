package dev.xantha.vss.common.processing;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/processing/ReadResultAccess.class */
public interface ReadResultAccess {
    int chunkX();

    int chunkZ();

    int requestId();

    long columnTimestamp();

    boolean notFound();

    long submissionOrder();

    default boolean saturated() {
        return false;
    }

    default byte[] sectionBytes() {
        return null;
    }

    default int estimatedBytes() {
        return 0;
    }
}

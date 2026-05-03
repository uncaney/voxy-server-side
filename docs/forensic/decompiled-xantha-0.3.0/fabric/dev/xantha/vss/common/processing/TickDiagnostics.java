package dev.xantha.vss.common.processing;

import dev.xantha.vss.common.DiagnosticsFormatter;
import dev.xantha.vss.common.VSSConstants;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/processing/TickDiagnostics.class */
public class TickDiagnostics {
    private int lastTickSectionsSent;
    private int lastTickDiskQueued;
    private int lastTickDiskDrained;
    private int lastTickGenDrained;
    private int lastTickInMemorySerialized;
    private int lastTickBytesFlushed;
    private int lastTickQueuePeak;
    private int lastTickSkippedDuplicate;
    private int lastTickUpToDate;
    private int curTickSectionsSent;
    private int curTickBytesFlushed;
    private int curTickQueuePeak;
    private static final int WINDOW_TICKS = 100;
    private final int[] byteRing = new int[WINDOW_TICKS];
    private final long[] nanosRing = new long[WINDOW_TICKS];
    private long windowByteSum;
    private int ringPos;
    private int ringCount;

    public void reset(ProcessingDiagnostics diag) {
        this.windowByteSum -= (long) this.byteRing[this.ringPos];
        this.byteRing[this.ringPos] = this.curTickBytesFlushed;
        this.nanosRing[this.ringPos] = System.nanoTime();
        this.windowByteSum += (long) this.curTickBytesFlushed;
        this.ringPos = (this.ringPos + 1) % WINDOW_TICKS;
        if (this.ringCount < WINDOW_TICKS) {
            this.ringCount++;
        }
        this.lastTickSectionsSent = this.curTickSectionsSent;
        this.lastTickDiskQueued = diag.getLastDiskQueued();
        this.lastTickDiskDrained = diag.getLastDiskDrained();
        this.lastTickGenDrained = diag.getLastGenDrained();
        this.lastTickInMemorySerialized = diag.getLastInMemory();
        this.lastTickBytesFlushed = this.curTickBytesFlushed;
        this.lastTickQueuePeak = this.curTickQueuePeak;
        this.lastTickSkippedDuplicate = diag.getLastSkippedDuplicate();
        this.lastTickUpToDate = diag.getLastUpToDate();
        this.curTickSectionsSent = 0;
        this.curTickBytesFlushed = 0;
        this.curTickQueuePeak = 0;
    }

    public long getWindowBytesPerSecond() {
        if (this.ringCount < 2) {
            return 0L;
        }
        int newestIdx = ((this.ringPos - 1) + WINDOW_TICKS) % WINDOW_TICKS;
        int oldestIdx = this.ringCount < WINDOW_TICKS ? 0 : this.ringPos;
        long elapsedNanos = this.nanosRing[newestIdx] - this.nanosRing[oldestIdx];
        if (elapsedNanos <= 0) {
            return 0L;
        }
        return (this.windowByteSum * VSSConstants.NANOS_PER_SECOND) / elapsedNanos;
    }

    public void recordSectionSent(int estimatedBytes) {
        this.curTickSectionsSent++;
        this.curTickBytesFlushed += estimatedBytes;
    }

    public void updateQueuePeak(int queueSize) {
        this.curTickQueuePeak = Math.max(this.curTickQueuePeak, queueSize);
    }

    public String format(int maxSendQueueSize) {
        return String.format("sent=%d, disk=%d/%d, utd=%d, gen=%d, in_mem=%d, skipped=%d, bytes=%s, qpeak=%d/%d", Integer.valueOf(this.lastTickSectionsSent), Integer.valueOf(this.lastTickDiskDrained), Integer.valueOf(this.lastTickDiskQueued), Integer.valueOf(this.lastTickUpToDate), Integer.valueOf(this.lastTickGenDrained), Integer.valueOf(this.lastTickInMemorySerialized), Integer.valueOf(this.lastTickSkippedDuplicate), DiagnosticsFormatter.formatBytes(this.lastTickBytesFlushed), Integer.valueOf(this.lastTickQueuePeak), Integer.valueOf(maxSendQueueSize));
    }

    public String formatSummary(long bwRate, long maxBytesPerSecondGlobal) {
        return String.format("sent=%d/tick, disk=%d/%d, utd=%d, bw=%s/%s", Integer.valueOf(this.lastTickSectionsSent), Integer.valueOf(this.lastTickDiskDrained), Integer.valueOf(this.lastTickDiskQueued), Integer.valueOf(this.lastTickUpToDate), DiagnosticsFormatter.formatBytes(bwRate), DiagnosticsFormatter.formatBytes(maxBytesPerSecondGlobal));
    }
}

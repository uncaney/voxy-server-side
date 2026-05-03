package dev.xantha.vss.common.processing;

import dev.xantha.vss.common.VSSConstants;

/* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/PlayerBandwidthTracker.class */
public class PlayerBandwidthTracker {
    private static final int BURST_DIVISOR = 4;
    private long availableTokens = 0;
    private long lastRefillNanos = System.nanoTime();
    private long totalSectionsSent = 0;
    private long totalBytesSent = 0;

    public boolean canSend(long allocationBytes) {
        if (allocationBytes <= 0) {
            return false;
        }
        long now = System.nanoTime();
        long elapsedNanos = now - this.lastRefillNanos;
        if (elapsedNanos >= VSSConstants.NANOS_PER_MS) {
            this.lastRefillNanos = now;
            long refill = (Math.min(elapsedNanos, VSSConstants.NANOS_PER_SECOND) * allocationBytes) / VSSConstants.NANOS_PER_SECOND;
            long burstCap = allocationBytes / 4;
            this.availableTokens = Math.min(this.availableTokens + refill, burstCap);
        }
        return this.availableTokens > 0;
    }

    public void recordSend(int bytes) {
        this.availableTokens = Math.max(0L, this.availableTokens - ((long) bytes));
        this.totalSectionsSent++;
        this.totalBytesSent += (long) bytes;
    }

    public long getTotalSectionsSent() {
        return this.totalSectionsSent;
    }

    public long getTotalBytesSent() {
        return this.totalBytesSent;
    }
}

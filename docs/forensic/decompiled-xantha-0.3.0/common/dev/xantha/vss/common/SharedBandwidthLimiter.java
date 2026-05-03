package dev.xantha.vss.common;

/* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/SharedBandwidthLimiter.class */
public class SharedBandwidthLimiter {
    private final long maxBytesPerSecond;
    private long availableTokens;
    private long lastRefillNanos = System.nanoTime();
    private long totalBytesSent;

    public SharedBandwidthLimiter(long maxBytesPerSecond) {
        this.maxBytesPerSecond = maxBytesPerSecond;
        this.availableTokens = maxBytesPerSecond;
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsedNanos = now - this.lastRefillNanos;
        if (elapsedNanos < VSSConstants.NANOS_PER_MS) {
            return;
        }
        this.lastRefillNanos = now;
        long refill = (Math.min(elapsedNanos, VSSConstants.NANOS_PER_SECOND) * this.maxBytesPerSecond) / VSSConstants.NANOS_PER_SECOND;
        this.availableTokens = Math.min(this.availableTokens + refill, this.maxBytesPerSecond);
    }

    public long getPerPlayerAllocation(int activePlayerCount) {
        refill();
        if (this.availableTokens <= 0 || activePlayerCount <= 0) {
            return 0L;
        }
        return this.availableTokens / ((long) activePlayerCount);
    }

    public void recordSend(int bytes) {
        this.availableTokens = Math.max(0L, this.availableTokens - ((long) bytes));
        this.totalBytesSent += (long) bytes;
    }

    public long getTotalBytesSent() {
        return this.totalBytesSent;
    }
}

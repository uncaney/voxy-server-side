package dev.xantha.vss.networking.client;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/RequestMetrics.class */
class RequestMetrics {
    private static final double EWMA_SMOOTHING_FACTOR = 0.3d;
    private int receivedCount = 0;
    private int emptyCount = 0;
    private long totalSendCycles = 0;
    private long totalPositionsRequested = 0;
    private long totalColumnsReceived = 0;
    private long totalUpToDate = 0;
    private long totalNotGenerated = 0;
    private long totalRateLimited = 0;
    private long lastRateUpdateMs = 0;
    private int columnsReceivedInWindow = 0;
    private int positionsRequestedInWindow = 0;
    private double receiveRate = 0.0d;
    private double requestRate = 0.0d;

    RequestMetrics() {
    }

    void adjustCounters(long oldTimestamp, long newTimestamp) {
        if (oldTimestamp >= 0) {
            if (oldTimestamp > 0) {
                this.receivedCount--;
            } else {
                this.emptyCount--;
            }
        }
        if (newTimestamp > 0) {
            this.receivedCount++;
        } else if (newTimestamp == 0) {
            this.emptyCount++;
        }
    }

    void recordSendCycle(int positionCount) {
        this.totalSendCycles++;
        this.totalPositionsRequested += (long) positionCount;
        this.positionsRequestedInWindow += positionCount;
    }

    void recordColumnReceived() {
        this.totalColumnsReceived++;
        this.columnsReceivedInWindow++;
    }

    void recordUpToDate() {
        this.totalUpToDate++;
    }

    void recordNotGenerated() {
        this.totalNotGenerated++;
    }

    void recordRateLimited() {
        this.totalRateLimited++;
    }

    void updateRollingRates() {
        long now = System.currentTimeMillis();
        long elapsed = now - this.lastRateUpdateMs;
        if (elapsed >= 1000) {
            double seconds = elapsed / 1000.0d;
            double instantReceiveRate = ((double) this.columnsReceivedInWindow) / seconds;
            double instantRequestRate = ((double) this.positionsRequestedInWindow) / seconds;
            this.receiveRate = (this.receiveRate * 0.7d) + (instantReceiveRate * EWMA_SMOOTHING_FACTOR);
            this.requestRate = (this.requestRate * 0.7d) + (instantRequestRate * EWMA_SMOOTHING_FACTOR);
            this.columnsReceivedInWindow = 0;
            this.positionsRequestedInWindow = 0;
            this.lastRateUpdateMs = now;
        }
    }

    void reset() {
        this.receivedCount = 0;
        this.emptyCount = 0;
        this.lastRateUpdateMs = 0L;
        this.columnsReceivedInWindow = 0;
        this.positionsRequestedInWindow = 0;
        this.receiveRate = 0.0d;
        this.requestRate = 0.0d;
    }

    void bulkRecount(Long2LongOpenHashMap timestamps) {
        this.receivedCount = 0;
        this.emptyCount = 0;
        LongIterator it = timestamps.values().iterator();
        while (it.hasNext()) {
            long ts = ((Long) it.next()).longValue();
            if (ts > 0) {
                this.receivedCount++;
            } else if (ts == 0) {
                this.emptyCount++;
            }
        }
    }

    void onTimestampRemoved(long timestamp) {
        if (timestamp > 0) {
            this.receivedCount--;
        } else if (timestamp == 0) {
            this.emptyCount--;
        }
    }

    int getReceivedCount() {
        return this.receivedCount;
    }

    int getEmptyCount() {
        return this.emptyCount;
    }

    long getTotalSendCycles() {
        return this.totalSendCycles;
    }

    long getTotalPositionsRequested() {
        return this.totalPositionsRequested;
    }

    long getTotalColumnsReceived() {
        return this.totalColumnsReceived;
    }

    long getTotalUpToDate() {
        return this.totalUpToDate;
    }

    long getTotalNotGenerated() {
        return this.totalNotGenerated;
    }

    long getTotalRateLimited() {
        return this.totalRateLimited;
    }

    double getReceiveRate() {
        return this.receiveRate;
    }

    double getRequestRate() {
        return this.requestRate;
    }
}

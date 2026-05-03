package dev.xantha.vss.common.processing;

/* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/RateLimiterSet.class */
public class RateLimiterSet {
    private final ConcurrencyLimiter syncOnLoadLimiter;
    private final ConcurrencyLimiter generationLimiter;
    private final int syncRateLimit;
    private final int genRateLimit;

    public RateLimiterSet(int syncRateLimit, int syncConcurrency, int genRateLimit, int genConcurrency) {
        this.syncOnLoadLimiter = new ConcurrencyLimiter(syncConcurrency);
        this.generationLimiter = new ConcurrencyLimiter(genConcurrency);
        this.syncRateLimit = syncRateLimit;
        this.genRateLimit = genRateLimit;
    }

    public ConcurrencyLimiter syncOnLoad() {
        return this.syncOnLoadLimiter;
    }

    public ConcurrencyLimiter generation() {
        return this.generationLimiter;
    }

    public ConcurrencyLimiter forRequest(RequestType type, boolean diskReadingAvailable) {
        if (type == RequestType.SYNC || diskReadingAvailable) {
            return this.syncOnLoadLimiter;
        }
        return this.generationLimiter;
    }

    public int syncRateLimit() {
        return this.syncRateLimit;
    }

    public int genRateLimit() {
        return this.genRateLimit;
    }
}

package dev.xantha.vss.networking.client;

import dev.xantha.vss.common.PositionUtil;
import dev.xantha.vss.compat.ModCompat;
import dev.xantha.vss.config.VSSClientConfig;
import dev.xantha.vss.networking.payloads.SessionConfigS2CPayload;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.OptionalInt;
import java.util.function.LongPredicate;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/SpiralScanner.class */
class SpiralScanner {
    private static final int BUDGET_MULTIPLIER = 4;
    private int lastBudget;
    private int lastSyncQueued;
    private int lastGenQueued;
    private int confirmedRing = 0;
    private int scanRing = 0;
    private int scanTickCounter = 19;
    private int missingVanillaChunks = Integer.MAX_VALUE;
    private int cachedVoxyDistance = -1;
    private int voxyDistanceStaleness = 0;
    private long[] posBuf = new long[0];
    private long[] tsBuf = new long[0];

    SpiralScanner() {
    }

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/SpiralScanner$ScanResult.class */
    static final class ScanResult extends Record {
        private final long[] positions;
        private final long[] timestamps;
        private final int count;

        ScanResult(long[] positions, long[] timestamps, int count) {
            this.positions = positions;
            this.timestamps = timestamps;
            this.count = count;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, ScanResult.class), ScanResult.class, "positions;timestamps;count", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->positions:[J", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->timestamps:[J", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, ScanResult.class), ScanResult.class, "positions;timestamps;count", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->positions:[J", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->timestamps:[J", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->count:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, ScanResult.class, Object.class), ScanResult.class, "positions;timestamps;count", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->positions:[J", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->timestamps:[J", "FIELD:Ldev/xantha/vss/networking/client/SpiralScanner$ScanResult;->count:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public long[] positions() {
            return this.positions;
        }

        public long[] timestamps() {
            return this.timestamps;
        }

        public int count() {
            return this.count;
        }
    }

    boolean advanceScanTick() {
        int i = this.scanTickCounter + 1;
        this.scanTickCounter = i;
        if (i < 20) {
            return false;
        }
        this.scanTickCounter = 0;
        return true;
    }

    static int baseBudget(SessionConfigS2CPayload config) {
        int concurrencyBased = config.syncOnLoadConcurrencyLimitPerPlayer() * BUDGET_MULTIPLIER;
        return Math.min(concurrencyBased, config.syncOnLoadRateLimitPerPlayer());
    }

    ScanResult scan(int playerCx, int playerCz, int viewDistance, Long2LongOpenHashMap columnTimestamps, LongOpenHashSet dirtyColumns, LongOpenHashSet rateLimitRetryPositions, LongOpenHashSet validatedThisSession, LongPredicate isInFlight, SessionConfigS2CPayload sessionConfig, int budget) {
        long ts;
        int lodDistance = getEffectiveLodDistance(sessionConfig);
        if (this.posBuf.length < budget) {
            this.posBuf = new long[budget];
            this.tsBuf = new long[budget];
        }
        long[] posBuf = this.posBuf;
        long[] tsBuf = this.tsBuf;
        int count = 0;
        int genCap = sessionConfig.generationConcurrencyLimitPerPlayer() * BUDGET_MULTIPLIER;
        int exclusionDistSq = viewDistance * viewDistance;
        int[] chunkCoords = new int[2];
        int localScanRing = -1;
        int syncQueued = 0;
        int genQueued = 0;
        if (!rateLimitRetryPositions.isEmpty()) {
            this.confirmedRing = 0;
        }
        int localConfirmedRing = this.confirmedRing;
        loop0: for (int r = localConfirmedRing; r <= lodDistance; r++) {
            if (2 * r * r <= exclusionDistSq) {
                localConfirmedRing = r + 1;
            } else if (r == 0) {
                localConfirmedRing = 1;
            } else {
                boolean ringFullySatisfied = true;
                int ringSize = 8 * r;
                for (int i = 0; i < ringSize; i++) {
                    if (count >= budget) {
                        break loop0;
                    }
                    ringIndexToCoord(r, i, playerCx, playerCz, chunkCoords);
                    int cx = chunkCoords[0];
                    int cz = chunkCoords[1];
                    int pdx = cx - playerCx;
                    int pdz = cz - playerCz;
                    if ((pdx * pdx) + (pdz * pdz) > exclusionDistSq) {
                        long packed = PositionUtil.packPosition(cx, cz);
                        if (!isInFlight.test(packed)) {
                            long stored = columnTimestamps.get(packed);
                            if (stored == -1) {
                                ts = -1;
                            } else if (stored == 0 && sessionConfig.generationEnabled()) {
                                if (genQueued >= genCap) {
                                    ringFullySatisfied = false;
                                } else {
                                    ts = 0;
                                }
                            } else if (dirtyColumns.contains(packed) || rateLimitRetryPositions.contains(packed)) {
                                ts = stored;
                            } else if (stored > 0 && !validatedThisSession.contains(packed)) {
                                ts = stored;
                            }
                            ringFullySatisfied = false;
                            posBuf[count] = packed;
                            tsBuf[count] = ts;
                            count++;
                            if (ts == 0) {
                                genQueued++;
                            } else {
                                syncQueued++;
                            }
                            if (localScanRing < r) {
                                localScanRing = r;
                            }
                        }
                    }
                }
                if (ringFullySatisfied) {
                    localConfirmedRing = r + 1;
                }
            }
        }
        this.confirmedRing = localConfirmedRing;
        this.scanRing = localScanRing >= 0 ? localScanRing : localConfirmedRing;
        this.lastBudget = budget;
        this.lastSyncQueued = syncQueued;
        this.lastGenQueued = genQueued;
        return new ScanResult(posBuf, tsBuf, count);
    }

    static void ringIndexToCoord(int r, int i, int centerX, int centerZ, int[] out) {
        int edge = i / (2 * r);
        int pos = i % (2 * r);
        switch (edge) {
            case 0:
                out[0] = (centerX - r) + pos;
                out[1] = centerZ - r;
                break;
            case 1:
                out[0] = centerX + r;
                out[1] = (centerZ - r) + pos;
                break;
            case 2:
                out[0] = (centerX + r) - pos;
                out[1] = centerZ + r;
                break;
            case 3:
                out[0] = centerX - r;
                out[1] = (centerZ + r) - pos;
                break;
        }
    }

    void updateMissingVanillaChunks(int count) {
        this.missingVanillaChunks = count;
    }

    void reset() {
        this.confirmedRing = 0;
        this.scanRing = 0;
        this.scanTickCounter = 19;
        this.missingVanillaChunks = Integer.MAX_VALUE;
        this.cachedVoxyDistance = -1;
        this.voxyDistanceStaleness = 0;
    }

    void resetScanCounter() {
        this.confirmedRing = 0;
        this.scanTickCounter = 0;
    }

    int getEffectiveLodDistance(SessionConfigS2CPayload sessionConfig) {
        int effective;
        int serverDistance = sessionConfig.lodDistanceChunks();
        int clientDistance = VSSClientConfig.CONFIG.lodDistanceChunks;
        if (clientDistance > 0) {
            effective = Math.min(clientDistance, serverDistance);
        } else {
            effective = serverDistance;
        }
        int voxyDist = getCachedVoxyDistance();
        if (voxyDist > 0 && voxyDist < effective) {
            effective = voxyDist;
        }
        return effective;
    }

    private int getCachedVoxyDistance() {
        int i = this.voxyDistanceStaleness + 1;
        this.voxyDistanceStaleness = i;
        if (i >= 20) {
            this.voxyDistanceStaleness = 0;
            OptionalInt voxyDistance = ModCompat.getVoxyViewDistanceChunks();
            this.cachedVoxyDistance = voxyDistance.isPresent() ? voxyDistance.getAsInt() : -1;
        }
        return this.cachedVoxyDistance;
    }

    int getPruneDistance(SessionConfigS2CPayload sessionConfig) {
        return getEffectiveLodDistance(sessionConfig) + 32;
    }

    void pruneOutOfRangeTimestamps(Long2LongOpenHashMap columnTimestamps, RequestMetrics metrics, int playerCx, int playerCz, int pruneDistance) {
        ObjectIterator<Long2LongMap.Entry> iter = columnTimestamps.long2LongEntrySet().iterator();
        while (iter.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry) iter.next();
            if (PositionUtil.isOutOfRange(entry.getLongKey(), playerCx, playerCz, pruneDistance)) {
                metrics.onTimestampRemoved(entry.getLongValue());
                iter.remove();
            }
        }
    }

    void pruneOutOfRangePositions(LongOpenHashSet set, int playerCx, int playerCz, int pruneDistance) {
        LongIterator iter = set.iterator();
        while (iter.hasNext()) {
            long packed = iter.nextLong();
            if (PositionUtil.isOutOfRange(packed, playerCx, playerCz, pruneDistance)) {
                iter.remove();
            }
        }
    }

    int getConfirmedRing() {
        return this.confirmedRing;
    }

    int getScanRing() {
        return this.scanRing;
    }

    int getMissingVanillaChunks() {
        return this.missingVanillaChunks;
    }

    int getLastBudget() {
        return this.lastBudget;
    }

    int getLastSyncQueued() {
        return this.lastSyncQueued;
    }

    int getLastGenQueued() {
        return this.lastGenQueued;
    }
}

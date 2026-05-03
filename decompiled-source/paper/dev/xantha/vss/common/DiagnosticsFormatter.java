package dev.xantha.vss.common;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/DiagnosticsFormatter.class */
public final class DiagnosticsFormatter {

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag.class */
    public static final class PlayerDiag extends Record {
        private final String name;
        private final int sendQueue;
        private final int maxSendQueue;
        private final int pendingSync;
        private final int pendingGen;
        private final long sent;
        private final long bytes;

        public PlayerDiag(String name, int sendQueue, int maxSendQueue, int pendingSync, int pendingGen, long sent, long bytes) {
            this.name = name;
            this.sendQueue = sendQueue;
            this.maxSendQueue = maxSendQueue;
            this.pendingSync = pendingSync;
            this.pendingGen = pendingGen;
            this.sent = sent;
            this.bytes = bytes;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, PlayerDiag.class), PlayerDiag.class, "name;sendQueue;maxSendQueue;pendingSync;pendingGen;sent;bytes", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->name:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->sendQueue:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->maxSendQueue:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->pendingSync:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->pendingGen:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->sent:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->bytes:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, PlayerDiag.class), PlayerDiag.class, "name;sendQueue;maxSendQueue;pendingSync;pendingGen;sent;bytes", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->name:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->sendQueue:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->maxSendQueue:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->pendingSync:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->pendingGen:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->sent:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->bytes:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, PlayerDiag.class, Object.class), PlayerDiag.class, "name;sendQueue;maxSendQueue;pendingSync;pendingGen;sent;bytes", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->name:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->sendQueue:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->maxSendQueue:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->pendingSync:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->pendingGen:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->sent:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$PlayerDiag;->bytes:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public String name() {
            return this.name;
        }

        public int sendQueue() {
            return this.sendQueue;
        }

        public int maxSendQueue() {
            return this.maxSendQueue;
        }

        public int pendingSync() {
            return this.pendingSync;
        }

        public int pendingGen() {
            return this.pendingGen;
        }

        public long sent() {
            return this.sent;
        }

        public long bytes() {
            return this.bytes;
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/DiagnosticsFormatter$DiagData.class */
    public static final class DiagData extends Record {
        private final boolean enabled;
        private final int lodDist;
        private final long bwPerPlayer;
        private final long bwGlobal;
        private final long uptimeSec;
        private final long totalSent;
        private final long totalBytes;
        private final long cumInMem;
        private final long cumUtd;
        private final long cumGen;
        private final long diskCompleted;
        private final String tickDiagnostics;
        private final String diskReaderDiagnostics;
        private final String generationDiagnostics;
        private final boolean generationEnabled;
        private final long bwTotal;
        private final long bwWindowRate;
        private final List<PlayerDiag> players;

        public DiagData(boolean enabled, int lodDist, long bwPerPlayer, long bwGlobal, long uptimeSec, long totalSent, long totalBytes, long cumInMem, long cumUtd, long cumGen, long diskCompleted, String tickDiagnostics, String diskReaderDiagnostics, String generationDiagnostics, boolean generationEnabled, long bwTotal, long bwWindowRate, List<PlayerDiag> players) {
            this.enabled = enabled;
            this.lodDist = lodDist;
            this.bwPerPlayer = bwPerPlayer;
            this.bwGlobal = bwGlobal;
            this.uptimeSec = uptimeSec;
            this.totalSent = totalSent;
            this.totalBytes = totalBytes;
            this.cumInMem = cumInMem;
            this.cumUtd = cumUtd;
            this.cumGen = cumGen;
            this.diskCompleted = diskCompleted;
            this.tickDiagnostics = tickDiagnostics;
            this.diskReaderDiagnostics = diskReaderDiagnostics;
            this.generationDiagnostics = generationDiagnostics;
            this.generationEnabled = generationEnabled;
            this.bwTotal = bwTotal;
            this.bwWindowRate = bwWindowRate;
            this.players = players;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, DiagData.class), DiagData.class, "enabled;lodDist;bwPerPlayer;bwGlobal;uptimeSec;totalSent;totalBytes;cumInMem;cumUtd;cumGen;diskCompleted;tickDiagnostics;diskReaderDiagnostics;generationDiagnostics;generationEnabled;bwTotal;bwWindowRate;players", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->enabled:Z", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->lodDist:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwPerPlayer:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwGlobal:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->uptimeSec:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->totalSent:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->totalBytes:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumInMem:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumUtd:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumGen:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->diskCompleted:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->tickDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->diskReaderDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->generationDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->generationEnabled:Z", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwTotal:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwWindowRate:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->players:Ljava/util/List;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, DiagData.class), DiagData.class, "enabled;lodDist;bwPerPlayer;bwGlobal;uptimeSec;totalSent;totalBytes;cumInMem;cumUtd;cumGen;diskCompleted;tickDiagnostics;diskReaderDiagnostics;generationDiagnostics;generationEnabled;bwTotal;bwWindowRate;players", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->enabled:Z", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->lodDist:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwPerPlayer:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwGlobal:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->uptimeSec:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->totalSent:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->totalBytes:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumInMem:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumUtd:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumGen:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->diskCompleted:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->tickDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->diskReaderDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->generationDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->generationEnabled:Z", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwTotal:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwWindowRate:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->players:Ljava/util/List;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, DiagData.class, Object.class), DiagData.class, "enabled;lodDist;bwPerPlayer;bwGlobal;uptimeSec;totalSent;totalBytes;cumInMem;cumUtd;cumGen;diskCompleted;tickDiagnostics;diskReaderDiagnostics;generationDiagnostics;generationEnabled;bwTotal;bwWindowRate;players", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->enabled:Z", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->lodDist:I", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwPerPlayer:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwGlobal:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->uptimeSec:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->totalSent:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->totalBytes:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumInMem:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumUtd:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->cumGen:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->diskCompleted:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->tickDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->diskReaderDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->generationDiagnostics:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->generationEnabled:Z", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwTotal:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->bwWindowRate:J", "FIELD:Ldev/xantha/vss/common/DiagnosticsFormatter$DiagData;->players:Ljava/util/List;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public boolean enabled() {
            return this.enabled;
        }

        public int lodDist() {
            return this.lodDist;
        }

        public long bwPerPlayer() {
            return this.bwPerPlayer;
        }

        public long bwGlobal() {
            return this.bwGlobal;
        }

        public long uptimeSec() {
            return this.uptimeSec;
        }

        public long totalSent() {
            return this.totalSent;
        }

        public long totalBytes() {
            return this.totalBytes;
        }

        public long cumInMem() {
            return this.cumInMem;
        }

        public long cumUtd() {
            return this.cumUtd;
        }

        public long cumGen() {
            return this.cumGen;
        }

        public long diskCompleted() {
            return this.diskCompleted;
        }

        public String tickDiagnostics() {
            return this.tickDiagnostics;
        }

        public String diskReaderDiagnostics() {
            return this.diskReaderDiagnostics;
        }

        public String generationDiagnostics() {
            return this.generationDiagnostics;
        }

        public boolean generationEnabled() {
            return this.generationEnabled;
        }

        public long bwTotal() {
            return this.bwTotal;
        }

        public long bwWindowRate() {
            return this.bwWindowRate;
        }

        public List<PlayerDiag> players() {
            return this.players;
        }
    }

    private DiagnosticsFormatter() {
    }

    public static List<String> formatDiagnostics(DiagData d) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("=== VSS LOD Diagnostics ===");
        lines.add(String.format("Config: enabled=%s, lodDist=%d, bw/player=%s/s, bw/global=%s/s", Boolean.valueOf(d.enabled), Integer.valueOf(d.lodDist), formatBytes(d.bwPerPlayer), formatBytes(d.bwGlobal)));
        double secRate = d.uptimeSec > 0 ? d.totalSent / d.uptimeSec : 0.0d;
        double byteRate = d.uptimeSec > 0 ? d.totalBytes / d.uptimeSec : 0.0d;
        lines.add(String.format("Throughput: sent=%d (%s), rate=%s sections/s (%s/s), uptime=%s", Long.valueOf(d.totalSent), formatBytes(d.totalBytes), formatRate(secRate), formatBytes((long) byteRate), formatUptime(d.uptimeSec)));
        lines.add(String.format("Sources (total): in_mem=%d, disk=%d, up_to_date=%d, gen=%d", Long.valueOf(d.cumInMem), Long.valueOf(Math.max(0L, d.diskCompleted)), Long.valueOf(d.cumUtd), Long.valueOf(d.cumGen)));
        lines.add("Sources (tick): " + d.tickDiagnostics);
        lines.add("DiskReader: " + d.diskReaderDiagnostics);
        if (d.generationEnabled) {
            lines.add("Generation: " + d.generationDiagnostics);
        } else {
            lines.add("Generation: disabled");
        }
        lines.add(String.format("Bandwidth: %s/s / %s/s global (%s total)", formatBytes(d.bwWindowRate), formatBytes(d.bwGlobal), formatBytes(d.bwTotal)));
        for (PlayerDiag p : d.players) {
            double pRate = d.uptimeSec > 0 ? p.sent / d.uptimeSec : 0.0d;
            lines.add(String.format("  %s: sq=%d/%d, psync=%d, pgen=%d, sent=%d (%s), rate=%s/s", p.name, Integer.valueOf(p.sendQueue), Integer.valueOf(p.maxSendQueue), Integer.valueOf(p.pendingSync), Integer.valueOf(p.pendingGen), Long.valueOf(p.sent), formatBytes(p.bytes), formatRate(pRate)));
        }
        return lines;
    }

    public static String formatRate(double rate) {
        return rate >= 1000.0d ? String.format("%.1fK", Double.valueOf(rate / 1000.0d)) : String.format("%.0f", Double.valueOf(rate));
    }

    public static String formatUptime(long seconds) {
        return seconds < 60 ? seconds + "s" : seconds < 3600 ? String.format("%dm %ds", Long.valueOf(seconds / 60), Long.valueOf(seconds % 60)) : String.format("%dh %dm", Long.valueOf(seconds / 3600), Long.valueOf((seconds % 3600) / 60));
    }

    public static String formatBytes(long bytes) {
        return bytes < 1024 ? bytes + " B" : bytes < 1048576 ? String.format("%.1f KB", Double.valueOf(bytes / 1024.0d)) : bytes < VSSConstants.MAX_BYTES_PER_SECOND_GLOBAL_LIMIT ? String.format("%.1f MB", Double.valueOf(bytes / 1048576.0d)) : String.format("%.2f GB", Double.valueOf(bytes / 1.073741824E9d));
    }
}

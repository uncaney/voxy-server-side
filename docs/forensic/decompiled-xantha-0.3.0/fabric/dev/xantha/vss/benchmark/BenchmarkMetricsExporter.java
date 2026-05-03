package dev.xantha.vss.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.processing.DiskReaderDiagnostics;
import dev.xantha.vss.common.processing.ProcessingDiagnostics;
import dev.xantha.vss.networking.client.LodRequestManager;
import dev.xantha.vss.networking.client.VSSClientNetworking;
import dev.xantha.vss.networking.server.ChunkDiskReader;
import dev.xantha.vss.networking.server.ChunkGenerationService;
import dev.xantha.vss.networking.server.PlayerRequestState;
import dev.xantha.vss.networking.server.RequestProcessingService;
import dev.xantha.vss.networking.server.VSSServerNetworking;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/benchmark/BenchmarkMetricsExporter.class */
public final class BenchmarkMetricsExporter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final double BYTES_PER_MB = 1048576.0d;

    private BenchmarkMetricsExporter() {
    }

    public static void exportServer(Path outputFile, long durationSeconds) {
        RequestProcessingService service = VSSServerNetworking.getRequestService();
        if (service == null) {
            VSSLogger.warn("[Benchmark] No RequestProcessingService available, skipping server export");
            return;
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("duration_seconds", Long.valueOf(durationSeconds));
        long totalSent = 0;
        long totalBytes = 0;
        for (PlayerRequestState state : service.getPlayers().values()) {
            totalSent += state.getTotalSectionsSent();
            totalBytes += state.getTotalBytesSent();
        }
        long uptime = service.getUptimeSeconds();
        LinkedHashMap<String, Object> throughput = new LinkedHashMap<>();
        throughput.put("total_sections_sent", Long.valueOf(totalSent));
        throughput.put("total_bytes_sent", Long.valueOf(totalBytes));
        throughput.put("sections_per_second", Double.valueOf(uptime > 0 ? totalSent / uptime : 0.0d));
        throughput.put("bytes_per_second", Double.valueOf(uptime > 0 ? totalBytes / uptime : 0.0d));
        result.put("throughput", throughput);
        ProcessingDiagnostics diag = service.getOffThreadProcessor().getDiagnostics();
        LinkedHashMap<String, Object> sources = new LinkedHashMap<>();
        sources.put("in_memory", Long.valueOf(diag.getTotalInMemory()));
        sources.put("up_to_date", Long.valueOf(diag.getTotalUpToDate()));
        sources.put("generation", Long.valueOf(diag.getTotalGenDrained()));
        ChunkDiskReader diskReader = service.getDiskReader();
        sources.put("disk_read", Long.valueOf(diskReader != null ? diskReader.getDiag().getSuccessfulReadCount() : 0L));
        result.put("sources", sources);
        LinkedHashMap<String, Object> diskReaderMap = new LinkedHashMap<>();
        if (diskReader != null) {
            DiskReaderDiagnostics dd = diskReader.getDiag();
            diskReaderMap.put("submitted", Long.valueOf(dd.getSubmittedCount()));
            diskReaderMap.put("completed", Long.valueOf(dd.getCompletedCount()));
            diskReaderMap.put("empty", Long.valueOf(dd.getEmptyCount()));
            diskReaderMap.put("errors", Long.valueOf(dd.getErrorCount()));
            long completed = dd.getCompletedCount();
            double avgMs = completed > 0 ? (dd.getTotalReadTimeNanos() / completed) / 1000000.0d : 0.0d;
            diskReaderMap.put("avg_read_time_ms", Double.valueOf(avgMs));
            diskReaderMap.put("saturation_events", Long.valueOf(dd.getSaturationCount()));
        }
        result.put("disk_reader", diskReaderMap);
        LinkedHashMap<String, Object> genMap = new LinkedHashMap<>();
        ChunkGenerationService genService = service.getGenerationService();
        if (genService != null) {
            genMap.put("submitted", Long.valueOf(genService.getTotalSubmitted()));
            genMap.put("completed", Long.valueOf(genService.getTotalCompleted()));
            genMap.put("timeouts", Long.valueOf(genService.getTotalTimeouts()));
        }
        result.put("generation", genMap);
        LinkedHashMap<String, Object> rateLimiting = new LinkedHashMap<>();
        rateLimiting.put("sync_rate_limited", Long.valueOf(diag.getTotalSyncRateLimited()));
        rateLimiting.put("gen_rate_limited", Long.valueOf(diag.getTotalGenRateLimited()));
        rateLimiting.put("queue_full", Long.valueOf(diag.getTotalQueueFull()));
        rateLimiting.put("queued", Long.valueOf(diag.getTotalQueued()));
        result.put("rate_limiting", rateLimiting);
        LinkedHashMap<String, Object> bandwidth = new LinkedHashMap<>();
        bandwidth.put("total_bytes_sent", Long.valueOf(service.getBandwidthLimiter().getTotalBytesSent()));
        result.put("bandwidth", bandwidth);
        LinkedHashMap<String, Object> jvm = new LinkedHashMap<>();
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        jvm.put("heap_used_mb", Double.valueOf(heap.getUsed() / BYTES_PER_MB));
        jvm.put("heap_max_mb", Double.valueOf(heap.getMax() / BYTES_PER_MB));
        long gcCount = 0;
        long gcTime = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long c = gc.getCollectionCount();
            long t = gc.getCollectionTime();
            if (c >= 0) {
                gcCount += c;
            }
            if (t >= 0) {
                gcTime += t;
            }
        }
        jvm.put("gc_count", Long.valueOf(gcCount));
        jvm.put("gc_time_ms", Long.valueOf(gcTime));
        result.put("jvm", jvm);
        writeJson(outputFile, result);
        VSSLogger.info("[Benchmark] Server metrics written to " + String.valueOf(outputFile));
    }

    public static Map<String, Object> buildClientMetrics() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("columns_received", Long.valueOf(VSSClientNetworking.getColumnsReceived()));
        result.put("bytes_received", Long.valueOf(VSSClientNetworking.getBytesReceived()));
        LodRequestManager manager = VSSClientNetworking.getRequestManager();
        if (manager != null) {
            result.put("total_up_to_date", Long.valueOf(manager.getTotalUpToDate()));
            result.put("total_not_generated", Long.valueOf(manager.getTotalNotGenerated()));
            result.put("total_rate_limited", Long.valueOf(manager.getTotalRateLimited()));
            result.put("send_cycles", Long.valueOf(manager.getTotalSendCycles()));
            result.put("positions_requested", Long.valueOf(manager.getTotalPositionsRequested()));
        }
        return result;
    }

    public static void exportClient(Path outputFile) {
        writeClientSnapshot(outputFile, buildClientMetrics());
    }

    public static void writeClientSnapshot(Path outputFile, Map<String, Object> snapshot) {
        writeJson(outputFile, snapshot);
        VSSLogger.info("[Benchmark] Client metrics written to " + String.valueOf(outputFile));
    }

    private static void writeJson(Path outputFile, Map<String, Object> data) {
        try {
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            Files.writeString(outputFile, GSON.toJson(data), new OpenOption[0]);
        } catch (IOException e) {
            VSSLogger.error("[Benchmark] Failed to write metrics to " + String.valueOf(outputFile), e);
        }
    }
}

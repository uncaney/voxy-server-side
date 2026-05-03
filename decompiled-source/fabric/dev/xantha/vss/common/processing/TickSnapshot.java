package dev.xantha.vss.common.processing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/processing/TickSnapshot.class */
public final class TickSnapshot extends Record {
    private final Map<UUID, PlayerTickData> players;
    private final Map<UUID, Long2ObjectMap<LoadedColumnData>> loadedChunkProbes;
    private final List<GenerationReadyData> generationReady;
    private final List<UUID> removedPlayers;
    private final int maxSendQueueSize;
    private final boolean shutdown;

    public TickSnapshot(Map<UUID, PlayerTickData> players, Map<UUID, Long2ObjectMap<LoadedColumnData>> loadedChunkProbes, List<GenerationReadyData> generationReady, List<UUID> removedPlayers, int maxSendQueueSize, boolean shutdown) {
        this.players = players;
        this.loadedChunkProbes = loadedChunkProbes;
        this.generationReady = generationReady;
        this.removedPlayers = removedPlayers;
        this.maxSendQueueSize = maxSendQueueSize;
        this.shutdown = shutdown;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, TickSnapshot.class), TickSnapshot.class, "players;loadedChunkProbes;generationReady;removedPlayers;maxSendQueueSize;shutdown", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->players:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->loadedChunkProbes:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->generationReady:Ljava/util/List;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->removedPlayers:Ljava/util/List;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->maxSendQueueSize:I", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->shutdown:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, TickSnapshot.class), TickSnapshot.class, "players;loadedChunkProbes;generationReady;removedPlayers;maxSendQueueSize;shutdown", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->players:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->loadedChunkProbes:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->generationReady:Ljava/util/List;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->removedPlayers:Ljava/util/List;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->maxSendQueueSize:I", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->shutdown:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, TickSnapshot.class, Object.class), TickSnapshot.class, "players;loadedChunkProbes;generationReady;removedPlayers;maxSendQueueSize;shutdown", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->players:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->loadedChunkProbes:Ljava/util/Map;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->generationReady:Ljava/util/List;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->removedPlayers:Ljava/util/List;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->maxSendQueueSize:I", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot;->shutdown:Z").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public Map<UUID, PlayerTickData> players() {
        return this.players;
    }

    public Map<UUID, Long2ObjectMap<LoadedColumnData>> loadedChunkProbes() {
        return this.loadedChunkProbes;
    }

    public List<GenerationReadyData> generationReady() {
        return this.generationReady;
    }

    public List<UUID> removedPlayers() {
        return this.removedPlayers;
    }

    public int maxSendQueueSize() {
        return this.maxSendQueueSize;
    }

    public boolean shutdown() {
        return this.shutdown;
    }

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/processing/TickSnapshot$PlayerTickData.class */
    public static final class PlayerTickData extends Record {
        private final String dimension;
        private final boolean dimensionChanged;

        public PlayerTickData(String dimension, boolean dimensionChanged) {
            this.dimension = dimension;
            this.dimensionChanged = dimensionChanged;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, PlayerTickData.class), PlayerTickData.class, "dimension;dimensionChanged", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$PlayerTickData;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$PlayerTickData;->dimensionChanged:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, PlayerTickData.class), PlayerTickData.class, "dimension;dimensionChanged", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$PlayerTickData;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$PlayerTickData;->dimensionChanged:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, PlayerTickData.class, Object.class), PlayerTickData.class, "dimension;dimensionChanged", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$PlayerTickData;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$PlayerTickData;->dimensionChanged:Z").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public String dimension() {
            return this.dimension;
        }

        public boolean dimensionChanged() {
            return this.dimensionChanged;
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:META-INF/jars/common-0.3.0.jar:dev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData.class */
    public static final class GenerationReadyData extends Record {
        private final UUID playerUuid;
        private final int requestId;
        private final LoadedColumnData columnData;
        private final long columnTimestamp;
        private final long submissionOrder;

        public GenerationReadyData(UUID playerUuid, int requestId, LoadedColumnData columnData, long columnTimestamp, long submissionOrder) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
            this.columnData = columnData;
            this.columnTimestamp = columnTimestamp;
            this.submissionOrder = submissionOrder;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, GenerationReadyData.class), GenerationReadyData.class, "playerUuid;requestId;columnData;columnTimestamp;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->columnData:Ldev/xantha/vss/common/processing/LoadedColumnData;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->columnTimestamp:J", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, GenerationReadyData.class), GenerationReadyData.class, "playerUuid;requestId;columnData;columnTimestamp;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->columnData:Ldev/xantha/vss/common/processing/LoadedColumnData;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->columnTimestamp:J", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, GenerationReadyData.class, Object.class), GenerationReadyData.class, "playerUuid;requestId;columnData;columnTimestamp;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->columnData:Ldev/xantha/vss/common/processing/LoadedColumnData;", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->columnTimestamp:J", "FIELD:Ldev/xantha/vss/common/processing/TickSnapshot$GenerationReadyData;->submissionOrder:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public UUID playerUuid() {
            return this.playerUuid;
        }

        public int requestId() {
            return this.requestId;
        }

        public LoadedColumnData columnData() {
            return this.columnData;
        }

        public long columnTimestamp() {
            return this.columnTimestamp;
        }

        public long submissionOrder() {
            return this.submissionOrder;
        }
    }

    public static TickSnapshot shutdownSentinel() {
        return new TickSnapshot(Map.of(), Map.of(), List.of(), List.of(), 0, true);
    }
}

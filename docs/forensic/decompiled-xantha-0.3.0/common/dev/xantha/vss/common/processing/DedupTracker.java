package dev.xantha.vss.common.processing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/DedupTracker.class */
class DedupTracker {
    private final Long2ObjectOpenHashMap<Group> pending = new Long2ObjectOpenHashMap<>();

    DedupTracker() {
    }

    /* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/DedupTracker$Attachment.class */
    static final class Attachment extends Record {
        private final UUID playerUuid;
        private final int requestId;
        private final long submissionOrder;

        Attachment(UUID playerUuid, int requestId, long submissionOrder) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
            this.submissionOrder = submissionOrder;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, Attachment.class), Attachment.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, Attachment.class), Attachment.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->submissionOrder:J").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, Attachment.class, Object.class), Attachment.class, "playerUuid;requestId;submissionOrder", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->requestId:I", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Attachment;->submissionOrder:J").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public UUID playerUuid() {
            return this.playerUuid;
        }

        public int requestId() {
            return this.requestId;
        }

        public long submissionOrder() {
            return this.submissionOrder;
        }
    }

    /* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/DedupTracker$Group.class */
    static final class Group extends Record {
        private final UUID primaryPlayer;
        private final String dimension;
        private final ArrayList<Attachment> attached;

        Group(UUID primaryPlayer, String dimension, ArrayList<Attachment> attached) {
            this.primaryPlayer = primaryPlayer;
            this.dimension = dimension;
            this.attached = attached;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, Group.class), Group.class, "primaryPlayer;dimension;attached", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->primaryPlayer:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->attached:Ljava/util/ArrayList;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, Group.class), Group.class, "primaryPlayer;dimension;attached", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->primaryPlayer:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->attached:Ljava/util/ArrayList;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, Group.class, Object.class), Group.class, "primaryPlayer;dimension;attached", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->primaryPlayer:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->dimension:Ljava/lang/String;", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$Group;->attached:Ljava/util/ArrayList;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public UUID primaryPlayer() {
            return this.primaryPlayer;
        }

        public String dimension() {
            return this.dimension;
        }

        public ArrayList<Attachment> attached() {
            return this.attached;
        }
    }

    /* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/DedupTracker$RemovedGroup.class */
    static final class RemovedGroup extends Record {
        private final long packed;
        private final Group group;

        RemovedGroup(long packed, Group group) {
            this.packed = packed;
            this.group = group;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, RemovedGroup.class), RemovedGroup.class, "packed;group", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$RemovedGroup;->packed:J", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$RemovedGroup;->group:Ldev/xantha/vss/common/processing/DedupTracker$Group;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, RemovedGroup.class), RemovedGroup.class, "packed;group", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$RemovedGroup;->packed:J", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$RemovedGroup;->group:Ldev/xantha/vss/common/processing/DedupTracker$Group;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, RemovedGroup.class, Object.class), RemovedGroup.class, "packed;group", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$RemovedGroup;->packed:J", "FIELD:Ldev/xantha/vss/common/processing/DedupTracker$RemovedGroup;->group:Ldev/xantha/vss/common/processing/DedupTracker$Group;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public long packed() {
            return this.packed;
        }

        public Group group() {
            return this.group;
        }
    }

    boolean tryAttachOrCreate(long packed, String dimension, UUID primaryPlayer, int requestId, long submissionOrder) {
        Group existing = (Group) this.pending.get(packed);
        if (existing != null) {
            existing.attached().add(new Attachment(primaryPlayer, requestId, submissionOrder));
            return true;
        }
        this.pending.put(packed, new Group(primaryPlayer, dimension, new ArrayList(2)));
        return false;
    }

    Group removeGroup(long packed) {
        return (Group) this.pending.remove(packed);
    }

    List<RemovedGroup> removePlayer(UUID playerUuid) {
        List<RemovedGroup> removed = null;
        ObjectIterator<Long2ObjectMap.Entry<Group>> iter = this.pending.long2ObjectEntrySet().iterator();
        while (iter.hasNext()) {
            Long2ObjectMap.Entry<Group> entry = (Long2ObjectMap.Entry) iter.next();
            Group group = (Group) entry.getValue();
            if (group.primaryPlayer().equals(playerUuid)) {
                if (removed == null) {
                    removed = new ArrayList<>();
                }
                removed.add(new RemovedGroup(entry.getLongKey(), group));
                iter.remove();
            } else {
                group.attached().removeIf(a -> {
                    return a.playerUuid().equals(playerUuid);
                });
            }
        }
        return removed != null ? removed : List.of();
    }
}

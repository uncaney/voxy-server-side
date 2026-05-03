package dev.xantha.vss.common.processing;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.UUID;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/SendAction.class */
public interface SendAction {
    UUID playerUuid();

    int requestId();

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    default byte responseType() throws MatchException {
        Objects.requireNonNull(this);
        switch ((int) SwitchBootstraps.typeSwitch(MethodHandles.lookup(), "typeSwitch", MethodType.methodType(Integer.TYPE, Object.class, Integer.TYPE), RateLimited.class, ColumnUpToDate.class, ColumnNotGenerated.class).dynamicInvoker().invoke(this, 0) /* invoke-custom */) {
            case 0:
                return (byte) 0;
            case 1:
                return (byte) 1;
            case 2:
                return (byte) 2;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/SendAction$RateLimited.class */
    public static final class RateLimited extends Record implements SendAction {
        private final UUID playerUuid;
        private final int requestId;

        public RateLimited(UUID playerUuid, int requestId) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, RateLimited.class), RateLimited.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$RateLimited;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$RateLimited;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, RateLimited.class), RateLimited.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$RateLimited;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$RateLimited;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, RateLimited.class, Object.class), RateLimited.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$RateLimited;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$RateLimited;->requestId:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        @Override // dev.xantha.vss.common.processing.SendAction
        public UUID playerUuid() {
            return this.playerUuid;
        }

        @Override // dev.xantha.vss.common.processing.SendAction
        public int requestId() {
            return this.requestId;
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/SendAction$ColumnUpToDate.class */
    public static final class ColumnUpToDate extends Record implements SendAction {
        private final UUID playerUuid;
        private final int requestId;

        public ColumnUpToDate(UUID playerUuid, int requestId) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, ColumnUpToDate.class), ColumnUpToDate.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnUpToDate;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnUpToDate;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, ColumnUpToDate.class), ColumnUpToDate.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnUpToDate;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnUpToDate;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, ColumnUpToDate.class, Object.class), ColumnUpToDate.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnUpToDate;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnUpToDate;->requestId:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        @Override // dev.xantha.vss.common.processing.SendAction
        public UUID playerUuid() {
            return this.playerUuid;
        }

        @Override // dev.xantha.vss.common.processing.SendAction
        public int requestId() {
            return this.requestId;
        }
    }

    /* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/SendAction$ColumnNotGenerated.class */
    public static final class ColumnNotGenerated extends Record implements SendAction {
        private final UUID playerUuid;
        private final int requestId;

        public ColumnNotGenerated(UUID playerUuid, int requestId) {
            this.playerUuid = playerUuid;
            this.requestId = requestId;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, ColumnNotGenerated.class), ColumnNotGenerated.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnNotGenerated;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnNotGenerated;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, ColumnNotGenerated.class), ColumnNotGenerated.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnNotGenerated;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnNotGenerated;->requestId:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, ColumnNotGenerated.class, Object.class), ColumnNotGenerated.class, "playerUuid;requestId", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnNotGenerated;->playerUuid:Ljava/util/UUID;", "FIELD:Ldev/xantha/vss/common/processing/SendAction$ColumnNotGenerated;->requestId:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        @Override // dev.xantha.vss.common.processing.SendAction
        public UUID playerUuid() {
            return this.playerUuid;
        }

        @Override // dev.xantha.vss.common.processing.SendAction
        public int requestId() {
            return this.requestId;
        }
    }
}

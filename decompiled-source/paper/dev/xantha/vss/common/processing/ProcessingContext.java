package dev.xantha.vss.common.processing;

import dev.xantha.vss.common.processing.OffThreadProcessor;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.concurrent.ConcurrentLinkedQueue;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/common/processing/ProcessingContext.class */
final class ProcessingContext extends Record {
    private final ConcurrentLinkedQueue<SendAction> sendActions;
    private final ConcurrentLinkedQueue<OffThreadProcessor.GenerationTicketRequest> generationTicketRequests;
    private final ProcessingDiagnostics diagnostics;
    private final SequenceCounter sequence;

    ProcessingContext(ConcurrentLinkedQueue<SendAction> sendActions, ConcurrentLinkedQueue<OffThreadProcessor.GenerationTicketRequest> generationTicketRequests, ProcessingDiagnostics diagnostics, SequenceCounter sequence) {
        this.sendActions = sendActions;
        this.generationTicketRequests = generationTicketRequests;
        this.diagnostics = diagnostics;
        this.sequence = sequence;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, ProcessingContext.class), ProcessingContext.class, "sendActions;generationTicketRequests;diagnostics;sequence", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->sendActions:Ljava/util/concurrent/ConcurrentLinkedQueue;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->generationTicketRequests:Ljava/util/concurrent/ConcurrentLinkedQueue;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->diagnostics:Ldev/xantha/vss/common/processing/ProcessingDiagnostics;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->sequence:Ldev/xantha/vss/common/processing/SequenceCounter;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, ProcessingContext.class), ProcessingContext.class, "sendActions;generationTicketRequests;diagnostics;sequence", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->sendActions:Ljava/util/concurrent/ConcurrentLinkedQueue;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->generationTicketRequests:Ljava/util/concurrent/ConcurrentLinkedQueue;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->diagnostics:Ldev/xantha/vss/common/processing/ProcessingDiagnostics;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->sequence:Ldev/xantha/vss/common/processing/SequenceCounter;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, ProcessingContext.class, Object.class), ProcessingContext.class, "sendActions;generationTicketRequests;diagnostics;sequence", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->sendActions:Ljava/util/concurrent/ConcurrentLinkedQueue;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->generationTicketRequests:Ljava/util/concurrent/ConcurrentLinkedQueue;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->diagnostics:Ldev/xantha/vss/common/processing/ProcessingDiagnostics;", "FIELD:Ldev/xantha/vss/common/processing/ProcessingContext;->sequence:Ldev/xantha/vss/common/processing/SequenceCounter;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public ConcurrentLinkedQueue<SendAction> sendActions() {
        return this.sendActions;
    }

    public ConcurrentLinkedQueue<OffThreadProcessor.GenerationTicketRequest> generationTicketRequests() {
        return this.generationTicketRequests;
    }

    public ProcessingDiagnostics diagnostics() {
        return this.diagnostics;
    }

    public SequenceCounter sequence() {
        return this.sequence;
    }
}

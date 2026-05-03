package dev.xantha.vss.common.processing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/SendActionBatcher.class */
public final class SendActionBatcher {
    private final Map<UUID, PlayerBatch> batches = new HashMap();
    private static final int INITIAL_CAPACITY = 64;

    /* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/SendActionBatcher$BatchConsumer.class */
    @FunctionalInterface
    public interface BatchConsumer {
        void accept(UUID uuid, byte[] bArr, int[] iArr, int i);
    }

    public void add(UUID playerUuid, byte responseType, int requestId) {
        PlayerBatch batch = this.batches.computeIfAbsent(playerUuid, k -> {
            return new PlayerBatch();
        });
        batch.add(responseType, requestId);
    }

    public boolean isEmpty() {
        return this.batches.isEmpty();
    }

    public void forEach(BatchConsumer consumer) {
        for (Map.Entry<UUID, PlayerBatch> entry : this.batches.entrySet()) {
            PlayerBatch batch = entry.getValue();
            if (batch.count > 0) {
                consumer.accept(entry.getKey(), batch.types, batch.requestIds, batch.count);
            }
        }
    }

    public void clear() {
        this.batches.clear();
    }

    /* JADX INFO: loaded from: common-0.3.0.jar:dev/xantha/vss/common/processing/SendActionBatcher$PlayerBatch.class */
    private static final class PlayerBatch {
        byte[] types = new byte[64];
        int[] requestIds = new int[64];
        int count;

        private PlayerBatch() {
        }

        void add(byte responseType, int requestId) {
            if (this.count >= this.types.length) {
                this.types = Arrays.copyOf(this.types, this.types.length * 2);
                this.requestIds = Arrays.copyOf(this.requestIds, this.requestIds.length * 2);
            }
            this.types[this.count] = responseType;
            this.requestIds[this.count] = requestId;
            this.count++;
        }
    }
}

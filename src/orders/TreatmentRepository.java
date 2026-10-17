package orders;

import storage.FileStore;

public class TreatmentRepository {
    private final FileStore store;

    public TreatmentRepository(FileStore store) {
        this.store = store;
    }

    public void save(TreatmentOrders orders) {
        store.save("orders", orders.getId(), orders.getStatus().name());
    }

    public TreatmentOrders find(String id) {
        String saved = store.read("orders", id);
        return saved == null ? null : TreatmentOrders.restore(id, saved);
    }
}

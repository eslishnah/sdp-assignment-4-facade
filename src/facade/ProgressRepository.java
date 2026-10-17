package facade;

import storage.FileStore;

public class ProgressRepository {
    private final FileStore store;

    public ProgressRepository(FileStore store) {
        this.store = store;
    }

    public void save(AdmissionProgress progress) {
        store.save("progress", progress.getId(), progress.getStatus().name());
    }

    public AdmissionProgress find(String id) {
        String saved = store.read("progress", id);
        return saved == null ? null : AdmissionProgress.restore(id, saved);
    }
}

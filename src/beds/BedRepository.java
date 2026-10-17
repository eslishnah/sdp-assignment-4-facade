package beds;

import storage.FileStore;

public class BedRepository {
    private final FileStore store;

    public BedRepository(FileStore store) {
        this.store = store;
    }

    public void save(BedAssignment bed) {
        store.save("beds", bed.getId(), bed.getRoomNumber() + "," + bed.getStatus());
    }

    public BedAssignment find(String id) {
        String saved = store.read("beds", id);
        if (saved == null) {
            return null;
        }
        String[] parts = saved.split(",", -1);
        return BedAssignment.restore(id, Integer.parseInt(parts[0]), parts[1]);
    }
}

package intake;

import storage.FileStore;

public class RegistrationRepository {
    private final FileStore store;

    public RegistrationRepository(FileStore store) {
        this.store = store;
    }

    public void save(Registration registration) {
        store.save("registrations", registration.getId(),
                registration.getPatientName() + "," + registration.getStatus());
    }

    public Registration find(String id) {
        String saved = store.read("registrations", id);
        if (saved == null) {
            return null;
        }
        String[] parts = saved.split(",", -1);
        return Registration.restore(id, parts[0], parts[1]);
    }
}

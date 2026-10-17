package intake;

public class Registration {
    public enum Status { NEW, REGISTERED, CANCELLED }

    private final String id;
    private final String patientName;
    private Status status;

    public Registration(String id, String patientName) {
        this.id = id;
        this.patientName = patientName;
        status = Status.NEW;
    }

    public void register() {
        if (status != Status.NEW) {
            throw new IllegalStateException("Registration cannot be confirmed.");
        }
        status = Status.REGISTERED;
    }

    public void cancel() {
        if (status != Status.REGISTERED) {
            throw new IllegalStateException("Registration cannot be cancelled.");
        }
        status = Status.CANCELLED;
    }

    public static Registration restore(String id, String name, String savedStatus) {
        Registration registration = new Registration(id, name);
        Status status = Status.valueOf(savedStatus);
        if (status == Status.REGISTERED || status == Status.CANCELLED) {
            registration.register();
        }
        if (status == Status.CANCELLED) {
            registration.cancel();
        }
        return registration;
    }

    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public Status getStatus() { return status; }
}

package facade;

public class AdmissionProgress {
    public enum Status { PENDING_B, PENDING_C, COMPLETED, COMPENSATING, COMPENSATED }

    private final String id;
    private Status status;

    public AdmissionProgress(String id) {
        this(id, Status.PENDING_B);
    }

    private AdmissionProgress(String id, Status status) {
        this.id = id;
        this.status = status;
    }

    public static AdmissionProgress restore(String id, String savedStatus) {
        return new AdmissionProgress(id, Status.valueOf(savedStatus));
    }

    public void nextStage() {
        if (status != Status.PENDING_B) {
            throw new IllegalStateException("Cannot move to stage C.");
        }
        status = Status.PENDING_C;
    }

    public void complete() {
        if (status != Status.PENDING_C) {
            throw new IllegalStateException("Cannot complete admission.");
        }
        status = Status.COMPLETED;
    }

    public void startCompensation() {
        if (status != Status.PENDING_B && status != Status.PENDING_C
                && status != Status.COMPLETED) {
            throw new IllegalStateException("Cannot start compensation.");
        }
        status = Status.COMPENSATING;
    }

    public void finishCompensation() {
        if (status != Status.COMPENSATING) {
            throw new IllegalStateException("Cannot finish compensation.");
        }
        status = Status.COMPENSATED;
    }

    public String getId() { return id; }
    public Status getStatus() { return status; }
}

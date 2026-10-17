package orders;

public class TreatmentOrders {
    public enum Status { NEW, CONFIRMED, CANCELLED }

    private final String id;
    private Status status;

    public TreatmentOrders(String id) {
        this.id = id;
        status = Status.NEW;
    }

    public void confirm() {
        if (status != Status.NEW) {
            throw new IllegalStateException("Orders cannot be confirmed.");
        }
        status = Status.CONFIRMED;
    }

    public void cancel() {
        if (status != Status.CONFIRMED) {
            throw new IllegalStateException("Orders cannot be cancelled.");
        }
        status = Status.CANCELLED;
    }

    public static TreatmentOrders restore(String id, String savedStatus) {
        TreatmentOrders orders = new TreatmentOrders(id);
        Status status = Status.valueOf(savedStatus);
        if (status == Status.CONFIRMED || status == Status.CANCELLED) {
            orders.confirm();
        }
        if (status == Status.CANCELLED) {
            orders.cancel();
        }
        return orders;
    }

    public String getId() { return id; }
    public Status getStatus() { return status; }
}

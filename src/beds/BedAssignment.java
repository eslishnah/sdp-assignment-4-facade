package beds;

public class BedAssignment {
    public enum Status { NEW, ALLOCATED, RELEASED }

    private final String id;
    private final int roomNumber;
    private Status status;

    public BedAssignment(String id, int roomNumber) {
        this.id = id;
        this.roomNumber = roomNumber;
        status = Status.NEW;
    }

    public void allocate() {
        if (status != Status.NEW) {
            throw new IllegalStateException("Bed cannot be allocated.");
        }
        status = Status.ALLOCATED;
    }

    public void release() {
        if (status != Status.ALLOCATED) {
            throw new IllegalStateException("Bed cannot be released.");
        }
        status = Status.RELEASED;
    }

    public static BedAssignment restore(String id, int room, String savedStatus) {
        BedAssignment bed = new BedAssignment(id, room);
        Status status = Status.valueOf(savedStatus);
        if (status == Status.ALLOCATED || status == Status.RELEASED) {
            bed.allocate();
        }
        if (status == Status.RELEASED) {
            bed.release();
        }
        return bed;
    }

    public String getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    public Status getStatus() { return status; }
}

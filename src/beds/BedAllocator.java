package beds;

public class BedAllocator {
    public void check(int roomNumber, boolean bedAvailable) {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Room number must be positive.");
        }
        if (!bedAvailable) {
            throw new IllegalStateException("No bed is available in the chosen room.");
        }
    }
}

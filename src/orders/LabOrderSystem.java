package orders;

public class LabOrderSystem {
    public void check(boolean labReady) {
        if (!labReady) {
            throw new IllegalStateException("Lab cannot accept the order.");
        }
    }
}

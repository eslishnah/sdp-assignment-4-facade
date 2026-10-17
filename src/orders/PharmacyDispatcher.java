package orders;

public class PharmacyDispatcher {
    public void check(boolean pharmacyReady) {
        if (!pharmacyReady) {
            throw new IllegalStateException("Pharmacy cannot prepare the medication.");
        }
    }
}

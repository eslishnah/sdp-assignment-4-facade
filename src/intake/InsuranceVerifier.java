package intake;

public class InsuranceVerifier {
    public void verify(boolean validInsurance) {
        if (!validInsurance) {
            throw new IllegalArgumentException("Patient insurance is not valid.");
        }
    }
}

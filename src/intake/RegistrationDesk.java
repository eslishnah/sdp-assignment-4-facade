package intake;

public class RegistrationDesk {
    public void check(String id, String patientName) {
        if (id == null || !id.matches("[A-Za-z0-9-]{1,20}")) {
            throw new IllegalArgumentException("ID must contain 1-20 letters, digits or hyphens.");
        }
        if (patientName == null || !patientName.matches("[A-Za-z ]{2,40}")) {
            throw new IllegalArgumentException("Enter a patient name (2-40 English letters). ");
        }
    }
}

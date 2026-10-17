import beds.BedAllocator;
import beds.BedAssignment;
import beds.BedRepository;
import beds.EquipmentReserver;
import facade.AdmissionAdminFacade;
import facade.AdmissionFacade;
import facade.AdmissionProgress;
import facade.ProgressRepository;
import intake.InsuranceVerifier;
import intake.Registration;
import intake.RegistrationDesk;
import intake.RegistrationRepository;
import orders.LabOrderSystem;
import orders.PharmacyDispatcher;
import orders.TreatmentOrders;
import orders.TreatmentRepository;
import storage.FileStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AdmissionTests {
    private static int passed;

    public static void main(String[] args) throws IOException {
        Path temp = Files.createTempDirectory("hospital-tests-");
        try {
            checkSuccess(temp.resolve("success"));
            checkFullFacade(temp.resolve("full-facade"));
            checkPauseAfterBed(temp.resolve("pause"));
            checkBedFailure(temp.resolve("bed-failure"));
            checkEquipmentFailure(temp.resolve("equipment-failure"));
            checkLabFailure(temp.resolve("lab-failure"));
            checkPharmacyFailure(temp.resolve("pharmacy-failure"));
            checkInvalidInput(temp.resolve("invalid"));
            checkDuplicate(temp.resolve("duplicate"));
            checkPersistenceReload(temp.resolve("reload"));
            checkRecoverSavedBed(temp.resolve("saved-bed"));
            checkResumePendingC(temp.resolve("pending-c"));
            checkResumeCompensation(temp.resolve("compensating"));
            checkAdminCompensation(temp.resolve("admin"));
            checkManualBeforeBed(temp.resolve("manual-a"));
            checkManualAfterBed(temp.resolve("manual-b"));
            checkDirectSubsystems(temp.resolve("direct"));
            checkInvalidTransitions();
            checkOtherErrors(temp.resolve("errors"));
            System.out.println("ALL TESTS PASSED: " + passed);
        } finally {
            deleteDirectory(temp.toFile());
        }
    }

    private static void checkSuccess(Path path) {
        Context c = new Context(path);
        c.admission.start("P1", "Alice Smith", true);
        equal(AdmissionProgress.Status.PENDING_B, c.progress.find("P1").getStatus());
        equal("Admission completed for P1.", admit(c, "P1", true, true, true, true));
        Context reopened = new Context(path);
        equal(AdmissionProgress.Status.COMPLETED, reopened.progress.find("P1").getStatus());
        equal(Registration.Status.REGISTERED, reopened.registrations.find("P1").getStatus());
        equal(BedAssignment.Status.ALLOCATED, reopened.beds.find("P1").getStatus());
        equal(TreatmentOrders.Status.CONFIRMED, reopened.orders.find("P1").getStatus());
        contains(reopened.admin.inspect("P1"), "COMPLETED");
        passed++;
    }

    private static void checkFullFacade(Path path) {
        Context c = new Context(path);
        contains(c.admission.admit("P1", "Alice", true, 201,
                true, true, true, true), "completed");
        equal(AdmissionProgress.Status.COMPLETED, c.progress.find("P1").getStatus());
        passed++;
    }

    private static void checkPauseAfterBed(Path path) {
        Context c = startedAdmission(path);
        contains(c.admission.continueAdmission("P1", 101, true, true,
                true, true, true), "Next stage: ORDERS");
        equal(AdmissionProgress.Status.PENDING_C, c.progress.find("P1").getStatus());
        isNull(c.orders.find("P1"));
        equal(BedAssignment.Status.ALLOCATED, c.beds.find("P1").getStatus());
        contains(admit(new Context(path), "P1", true, true, true, true), "completed");
        passed++;
    }

    private static void checkBedFailure(Path path) {
        Context c = startedAdmission(path);
        contains(admit(c, "P1", false, true, true, true), "BED failed");
        equal(AdmissionProgress.Status.COMPENSATED, c.progress.find("P1").getStatus());
        equal(Registration.Status.CANCELLED, new Context(path).registrations.find("P1").getStatus());
        isNull(c.beds.find("P1"));
        isNull(c.orders.find("P1"));
        passed++;
    }

    private static void checkEquipmentFailure(Path path) {
        Context c = startedAdmission(path);
        contains(admit(c, "P1", true, false, true, true), "Required equipment");
        equal(Registration.Status.CANCELLED, c.registrations.find("P1").getStatus());
        isNull(c.beds.find("P1"));
        passed++;
    }

    private static void checkLabFailure(Path path) {
        Context c = startedAdmission(path);
        contains(admit(c, "P1", true, true, false, true), "ORDERS failed");
        Context reopened = new Context(path);
        equal(AdmissionProgress.Status.COMPENSATED, reopened.progress.find("P1").getStatus());
        equal(BedAssignment.Status.RELEASED, reopened.beds.find("P1").getStatus());
        equal(Registration.Status.CANCELLED, reopened.registrations.find("P1").getStatus());
        isNull(reopened.orders.find("P1"));
        passed++;
    }

    private static void checkPharmacyFailure(Path path) {
        Context c = startedAdmission(path);
        contains(admit(c, "P1", true, true, true, false), "Pharmacy");
        equal(BedAssignment.Status.RELEASED, c.beds.find("P1").getStatus());
        equal(Registration.Status.CANCELLED, c.registrations.find("P1").getStatus());
        passed++;
    }

    private static void checkInvalidInput(Path path) {
        Context c = new Context(path);
        expectError(() -> c.admission.start("../bad", "Alice", true));
        expectError(() -> c.admission.start("P1", "1", true));
        expectError(() -> c.admission.start("P1", "Alice", false));
        isNull(c.progress.find("P1"));
        isNull(c.registrations.find("P1"));
        passed++;
    }

    private static void checkDuplicate(Path path) {
        Context c = startedAdmission(path);
        expectError(() -> c.admission.start("P1", "Alice", true));
        equal(AdmissionProgress.Status.PENDING_B, c.progress.find("P1").getStatus());
        passed++;
    }

    private static void checkPersistenceReload(Path path) {
        Context first = startedAdmission(path);
        equal(AdmissionProgress.Status.PENDING_B, first.progress.find("P1").getStatus());
        Context restarted = new Context(path);
        contains(admit(restarted, "P1", true, true, true, true), "completed");
        equal(AdmissionProgress.Status.COMPLETED, new Context(path).progress.find("P1").getStatus());
        passed++;
    }

    private static void checkRecoverSavedBed(Path path) {
        Context c = startedAdmission(path);
        BedAssignment bed = new BedAssignment("P1", 101);
        bed.allocate();
        c.beds.save(bed);
        Context restarted = new Context(path);
        contains(admit(restarted, "P1", false, false, true, true), "completed");
        equal(BedAssignment.Status.ALLOCATED, restarted.beds.find("P1").getStatus());
        passed++;
    }

    private static void checkResumePendingC(Path path) {
        Context c = startedAdmission(path);
        BedAssignment bed = new BedAssignment("P1", 101);
        bed.allocate();
        c.beds.save(bed);
        AdmissionProgress progress = c.progress.find("P1");
        progress.nextStage();
        c.progress.save(progress);
        Context restarted = new Context(path);
        contains(admit(restarted, "P1", false, false, true, true), "completed");
        equal(TreatmentOrders.Status.CONFIRMED, restarted.orders.find("P1").getStatus());
        passed++;
    }

    private static void checkResumeCompensation(Path path) {
        Context c = startedAdmission(path);
        BedAssignment bed = new BedAssignment("P1", 101);
        bed.allocate();
        c.beds.save(bed);
        AdmissionProgress progress = c.progress.find("P1");
        progress.nextStage();
        progress.startCompensation();
        c.progress.save(progress);
        contains(admit(new Context(path), "P1", true, true, true, true), "Interrupted compensation");
        equal(BedAssignment.Status.RELEASED, new Context(path).beds.find("P1").getStatus());
        equal(Registration.Status.CANCELLED, new Context(path).registrations.find("P1").getStatus());
        passed++;
    }

    private static void checkAdminCompensation(Path path) {
        Context c = startedAdmission(path);
        admit(c, "P1", true, true, true, true);
        c.admin.compensate("P1");
        equal(TreatmentOrders.Status.CANCELLED, c.orders.find("P1").getStatus());
        equal(BedAssignment.Status.RELEASED, c.beds.find("P1").getStatus());
        equal(Registration.Status.CANCELLED, c.registrations.find("P1").getStatus());
        equal(AdmissionProgress.Status.COMPENSATED, c.progress.find("P1").getStatus());
        c.admin.compensate("P1");
        passed++;
    }

    private static void checkManualBeforeBed(Path path) {
        Context c = startedAdmission(path);
        c.admin.compensate("P1");
        equal(AdmissionProgress.Status.COMPENSATED, c.progress.find("P1").getStatus());
        isNull(c.beds.find("P1"));
        passed++;
    }

    private static void checkManualAfterBed(Path path) {
        Context c = startedAdmission(path);
        BedAssignment bed = new BedAssignment("P1", 101);
        bed.allocate();
        c.beds.save(bed);
        AdmissionProgress progress = c.progress.find("P1");
        progress.nextStage();
        c.progress.save(progress);
        c.admin.compensate("P1");
        equal(BedAssignment.Status.RELEASED, c.beds.find("P1").getStatus());
        passed++;
    }

    private static void checkDirectSubsystems(Path path) {
        Context c = new Context(path);
        new RegistrationDesk().check("P1", "Alice");
        new InsuranceVerifier().verify(true);
        Registration registration = new Registration("P2", "Bob");
        registration.register();
        c.registrations.save(registration);
        equal(Registration.Status.REGISTERED, c.registrations.find("P2").getStatus());
        new BedAllocator().check(102, true);
        new EquipmentReserver().check(true);
        BedAssignment bed = new BedAssignment("P2", 102);
        bed.allocate();
        c.beds.save(bed);
        equal(102, c.beds.find("P2").getRoomNumber());
        new LabOrderSystem().check(true);
        new PharmacyDispatcher().check(true);
        TreatmentOrders orders = new TreatmentOrders("P2");
        orders.confirm();
        c.orders.save(orders);
        equal(TreatmentOrders.Status.CONFIRMED, c.orders.find("P2").getStatus());
        passed++;
    }

    private static void checkInvalidTransitions() {
        Registration a = new Registration("P", "Alice");
        expectError(a::cancel);
        a.register();
        expectError(a::register);
        a.cancel();
        expectError(a::cancel);
        BedAssignment b = new BedAssignment("P", 10);
        expectError(b::release);
        b.allocate();
        expectError(b::allocate);
        b.release();
        expectError(b::release);
        TreatmentOrders c = new TreatmentOrders("P");
        expectError(c::cancel);
        c.confirm();
        expectError(c::confirm);
        c.cancel();
        expectError(c::cancel);
        AdmissionProgress p = new AdmissionProgress("P");
        expectError(p::complete);
        expectError(p::finishCompensation);
        p.nextStage();
        expectError(p::nextStage);
        p.complete();
        expectError(p::complete);
        p.startCompensation();
        expectError(p::startCompensation);
        p.finishCompensation();
        expectError(p::startCompensation);
        passed++;
    }

    private static void checkOtherErrors(Path path) {
        Context c = new Context(path);
        contains(c.admin.inspect("X"), "not found");
        expectError(() -> c.admin.compensate("X"));
        expectError(() -> admit(c, "X", true, true, true, true));
        c.admission.start("P1", "Alice", true);
        contains(c.admin.inspect("P1"), "Bed: NONE");
        contains(c.admission.continueAdmission("P1", 0, true, true, true, true), "BED failed");
        expectError(() -> admit(c, "P1", true, true, true, true));
        c.admission.start("P2", "Bob", true);
        admit(c, "P2", true, true, true, true);
        expectError(() -> admit(c, "P2", true, true, true, true));
        expectError(() -> new BedAllocator().check(10, false));
        expectError(() -> new EquipmentReserver().check(false));
        expectError(() -> new LabOrderSystem().check(false));
        expectError(() -> new PharmacyDispatcher().check(false));
        passed++;
    }

    private static Context startedAdmission(Path path) {
        Context c = new Context(path);
        c.admission.start("P1", "Alice", true);
        return c;
    }

    private static String admit(Context c, String id, boolean bedAvailable,
                                boolean equipmentAvailable, boolean labReady, boolean pharmacyReady) {
        return c.admission.continueAdmission(id, 101, bedAvailable,
                equipmentAvailable, labReady, pharmacyReady);
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static void isNull(Object value) {
        if (value != null) {
            throw new AssertionError("Expected null, got " + value);
        }
    }

    private static void contains(String text, String expected) {
        if (!text.contains(expected)) {
            throw new AssertionError("Expected text to contain '" + expected + "': " + text);
        }
    }

    private static void expectError(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected an exception");
        } catch (IllegalArgumentException | IllegalStateException expected) {
            // This failure is the expected result of the test.
        }
    }

    private static void deleteDirectory(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteDirectory(child);
            }
        }
        if (!file.delete()) {
            throw new IllegalStateException("Cannot delete test file: " + file);
        }
    }

    private static class Context {
        final RegistrationRepository registrations;
        final BedRepository beds;
        final TreatmentRepository orders;
        final ProgressRepository progress;
        final AdmissionAdminFacade admin;
        final AdmissionFacade admission;

        Context(Path directory) {
            FileStore store = new FileStore(directory);
            registrations = new RegistrationRepository(store);
            beds = new BedRepository(store);
            orders = new TreatmentRepository(store);
            progress = new ProgressRepository(store);
            admin = new AdmissionAdminFacade(registrations, beds, orders, progress);
            admission = new AdmissionFacade(registrations, beds, orders, progress, admin);
        }
    }
}

package facade;

import beds.BedAssignment;
import beds.BedRepository;
import intake.Registration;
import intake.RegistrationRepository;
import orders.TreatmentOrders;
import orders.TreatmentRepository;

public class AdmissionAdminFacade {
    private final RegistrationRepository registrations;
    private final BedRepository beds;
    private final TreatmentRepository orders;
    private final ProgressRepository progressStore;

    public AdmissionAdminFacade(RegistrationRepository registrations, BedRepository beds,
                                TreatmentRepository orders, ProgressRepository progressStore) {
        this.registrations = registrations;
        this.beds = beds;
        this.orders = orders;
        this.progressStore = progressStore;
    }

    public String inspect(String id) {
        AdmissionProgress progress = progressStore.find(id);
        if (progress == null) {
            return "Admission not found: " + id;
        }
        Registration registration = registrations.find(id);
        BedAssignment bed = beds.find(id);
        TreatmentOrders treatment = orders.find(id);
        return "ID: " + id
                + "\nProgress: " + progress.getStatus()
                + "\nRegistration: " + (registration == null ? "NONE" : registration.getStatus())
                + "\nBed: " + (bed == null ? "NONE" : bed.getStatus() + " (room " + bed.getRoomNumber() + ")")
                + "\nTreatment orders: " + (treatment == null ? "NONE" : treatment.getStatus());
    }

    public void compensate(String id) {
        AdmissionProgress progress = progressStore.find(id);
        if (progress == null) {
            throw new IllegalArgumentException("Admission does not exist.");
        }
        if (progress.getStatus() == AdmissionProgress.Status.COMPENSATED) {
            return;
        }
        if (progress.getStatus() != AdmissionProgress.Status.COMPENSATING) {
            progress.startCompensation();
            progressStore.save(progress);
        }

        TreatmentOrders treatment = orders.find(id);
        if (treatment != null && treatment.getStatus() == TreatmentOrders.Status.CONFIRMED) {
            treatment.cancel();
            orders.save(treatment);
        }
        BedAssignment bed = beds.find(id);
        if (bed != null && bed.getStatus() == BedAssignment.Status.ALLOCATED) {
            bed.release();
            beds.save(bed);
        }
        Registration registration = registrations.find(id);
        if (registration != null && registration.getStatus() == Registration.Status.REGISTERED) {
            registration.cancel();
            registrations.save(registration);
        }
        progress.finishCompensation();
        progressStore.save(progress);
    }
}

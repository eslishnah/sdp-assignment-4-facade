package facade;

import beds.BedAllocator;
import beds.BedAssignment;
import beds.BedRepository;
import beds.EquipmentReserver;
import intake.InsuranceVerifier;
import intake.Registration;
import intake.RegistrationDesk;
import intake.RegistrationRepository;
import orders.LabOrderSystem;
import orders.PharmacyDispatcher;
import orders.TreatmentOrders;
import orders.TreatmentRepository;

public class AdmissionFacade {
    private final RegistrationDesk desk;
    private final InsuranceVerifier insurance;
    private final RegistrationRepository registrations;
    private final BedAllocator allocator;
    private final EquipmentReserver equipment;
    private final BedRepository beds;
    private final LabOrderSystem lab;
    private final PharmacyDispatcher pharmacy;
    private final TreatmentRepository orders;
    private final ProgressRepository progressStore;
    private final AdmissionAdminFacade admin;

    public AdmissionFacade(RegistrationRepository registrations, BedRepository beds,
                           TreatmentRepository orders, ProgressRepository progressStore,
                           AdmissionAdminFacade admin) {
        this.desk = new RegistrationDesk();
        this.insurance = new InsuranceVerifier();
        this.allocator = new BedAllocator();
        this.equipment = new EquipmentReserver();
        this.lab = new LabOrderSystem();
        this.pharmacy = new PharmacyDispatcher();
        this.registrations = registrations;
        this.beds = beds;
        this.orders = orders;
        this.progressStore = progressStore;
        this.admin = admin;
    }

    public String start(String id, String name, boolean insuranceValid) {
        desk.check(id, name);
        insurance.verify(insuranceValid);
        if (progressStore.find(id) != null || registrations.find(id) != null) {
            throw new IllegalArgumentException("Admission ID already exists.");
        }
        Registration registration = new Registration(id, name);
        registration.register();
        registrations.save(registration);
        progressStore.save(new AdmissionProgress(id));
        return "Patient registered. Next stage: BED.";
    }

    public String admit(String id, String name, boolean insuranceValid, int room,
                        boolean bedAvailable, boolean equipmentAvailable,
                        boolean labReady, boolean pharmacyReady) {
        start(id, name, insuranceValid);
        return continueAdmission(id, room, bedAvailable,
                equipmentAvailable, labReady, pharmacyReady);
    }

    public String continueAdmission(String id, int room, boolean bedAvailable,
                                    boolean equipmentAvailable, boolean labReady,
                                    boolean pharmacyReady) {
        return continueAdmission(id, room, bedAvailable, equipmentAvailable,
                labReady, pharmacyReady, false);
    }

    public String continueAdmission(String id, int room, boolean bedAvailable,
                                    boolean equipmentAvailable, boolean labReady,
                                    boolean pharmacyReady, boolean pauseAfterBed) {
        AdmissionProgress progress = progressStore.find(id);
        if (progress == null) {
            throw new IllegalArgumentException("Admission not found.");
        }
        if (progress.getStatus() == AdmissionProgress.Status.COMPENSATING) {
            admin.compensate(id);
            return "Interrupted compensation was completed from disk.";
        }
        if (progress.getStatus() == AdmissionProgress.Status.COMPENSATED
                || progress.getStatus() == AdmissionProgress.Status.COMPLETED) {
            throw new IllegalStateException("Admission is already finished.");
        }

        if (progress.getStatus() == AdmissionProgress.Status.PENDING_B) {
            try {
                BedAssignment bed = beds.find(id);
                if (bed == null) {
                    allocator.check(room, bedAvailable);
                    equipment.check(equipmentAvailable);
                    bed = new BedAssignment(id, room);
                    bed.allocate();
                    beds.save(bed);
                } else if (bed.getStatus() != BedAssignment.Status.ALLOCATED) {
                    throw new IllegalStateException("Saved bed is not allocated.");
                }
                progress.nextStage();
                progressStore.save(progress);
                if (pauseAfterBed) {
                    return "Bed allocated. Next stage: ORDERS.";
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                admin.compensate(id);
                return "BED failed: " + e.getMessage() + " Registration cancelled.";
            }
        }

        if (progress.getStatus() == AdmissionProgress.Status.PENDING_C) {
            try {
                TreatmentOrders treatment = orders.find(id);
                if (treatment == null) {
                    lab.check(labReady);
                    pharmacy.check(pharmacyReady);
                    treatment = new TreatmentOrders(id);
                    treatment.confirm();
                    orders.save(treatment);
                } else if (treatment.getStatus() != TreatmentOrders.Status.CONFIRMED) {
                    throw new IllegalStateException("Saved orders are not confirmed.");
                }
                progress.complete();
                progressStore.save(progress);
            } catch (IllegalStateException | IllegalArgumentException e) {
                admin.compensate(id);
                return "ORDERS failed: " + e.getMessage() + " Bed released, registration cancelled.";
            }
        }
        return "Admission completed for " + id + ".";
    }
}

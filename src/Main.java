import beds.BedRepository;
import facade.AdmissionAdminFacade;
import facade.AdmissionFacade;
import facade.ProgressRepository;
import intake.RegistrationRepository;
import orders.TreatmentRepository;
import storage.FileStore;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FileStore store = new FileStore(Path.of("data"));
        RegistrationRepository registrations = new RegistrationRepository(store);
        BedRepository beds = new BedRepository(store);
        TreatmentRepository orders = new TreatmentRepository(store);
        ProgressRepository progress = new ProgressRepository(store);
        AdmissionAdminFacade admin = new AdmissionAdminFacade(registrations, beds, orders, progress);
        AdmissionFacade admission = new AdmissionFacade(registrations, beds, orders, progress, admin);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- HOSPITAL ADMISSION ---");
            System.out.println("1. Full admission (A -> B -> C)");
            System.out.println("2. Register patient only (A)");
            System.out.println("3. Continue admission (B -> C)");
            System.out.println("4. Inspect saved admission");
            System.out.println("5. Manually compensate admission");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String option = scanner.nextLine().trim();
            try {
                if (option.equals("0")) {
                    return;
                } else if (option.equals("1") || option.equals("2")) {
                    System.out.print("Admission ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.print("Patient name (English letters): ");
                    String name = scanner.nextLine().trim();
                    boolean insured = askYesNo(scanner, "Insurance valid");
                    if (option.equals("2")) {
                        System.out.println(admission.start(id, name, insured));
                    } else {
                        System.out.print("Room number: ");
                        int room = Integer.parseInt(scanner.nextLine().trim());
                        boolean bed = askYesNo(scanner, "Bed available");
                        boolean equipment = askYesNo(scanner, "Equipment available");
                        boolean lab = askYesNo(scanner, "Lab ready");
                        boolean pharmacy = askYesNo(scanner, "Pharmacy ready");
                        System.out.println(admission.admit(id, name, insured,
                                room, bed, equipment, lab, pharmacy));
                    }
                } else if (option.equals("3")) {
                    System.out.print("Admission ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.print("Room number: ");
                    int room = Integer.parseInt(scanner.nextLine().trim());
                    boolean bed = askYesNo(scanner, "Bed available");
                    boolean equipment = askYesNo(scanner, "Equipment available");
                    boolean pause = askYesNo(scanner, "Pause after stage B");
                    boolean lab = true;
                    boolean pharmacy = true;
                    if (!pause) {
                        lab = askYesNo(scanner, "Lab ready");
                        pharmacy = askYesNo(scanner, "Pharmacy ready");
                    }
                    System.out.println(admission.continueAdmission(id, room,
                            bed, equipment, lab, pharmacy, pause));
                } else if (option.equals("4")) {
                    System.out.print("Admission ID: ");
                    System.out.println(admin.inspect(scanner.nextLine().trim()));
                } else if (option.equals("5")) {
                    System.out.print("Admission ID: ");
                    String id = scanner.nextLine().trim();
                    admin.compensate(id);
                    System.out.println("Compensation complete: " + id);
                } else {
                    System.out.println("Unknown option.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static boolean askYesNo(Scanner scanner, String question) {
        System.out.print(question + "? (y/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }
}

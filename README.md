# Hospital Admission — Assignment 4

**Software Design Patterns | Facade Pattern | Variant 4**

## About the Project

This is a simple Java console application for hospital patient admission. It uses the Facade Design Pattern to connect three independent subsystems:

- A — Registration:** patient registration and insurance verification.
- **B — Bed Allocation:** checking bed and equipment availability.
- **C — Final Orders:** checking laboratory and pharmacy orders.

`AdmissionFacade` manages the whole process from A to C. `AdmissionAdminFacade` is used to check saved admissions and cancel them when necessary.

## How to Run

Open the project in IntelliJ IDEA and run `Main.java`.

You can also run it from the project folder:

**Windows:**
```bat
run.bat
```


## Tests

The tests are in `test/AdmissionTests.java`. They use plain Java assertions without JUnit.

To run them, execute `AdmissionTests.java` in IntelliJ or use:

```bat
test.bat
```

All 19 tests passed. They check successful admission, failures in stages B and C, compensation, invalid input, and persistence after restarting.

**Line coverage results:**

| Package | Coverage |
|---|---|
| intake | 100% |
| beds | 100% |
| orders | 100% |
| facade | 97.6% |

The coverage report is available in `docs/coverage/`.

## Persistence and Compensation

The application saves admission data in CSV files inside the `data/` folder. The data remain available after restarting the program.

If stage B fails, the registration from stage A is cancelled.

If stage C fails, the allocated bed is released first, and then the registration is cancelled.

The program can also continue an unfinished admission after restarting.

To reset saved data, close the program and delete the `data/` folder.

## Principle of Least Knowledge

Without Facade, the client would need to work with several classes directly:

```java
RegistrationDesk desk = new RegistrationDesk();
InsuranceVerifier insurance = new InsuranceVerifier();
BedAllocator allocator = new BedAllocator();
LabOrderSystem lab = new LabOrderSystem();
```

The client would also need to control the order of operations and handle failures.

With Facade, it only needs one method:

```java
admission.admit("P101", "Alice Smith", true, 101, true, true, true, true);
```

This makes the client code simpler and reduces its dependencies on subsystem classes.

## Facade vs Adapter

I used Facade because the main goal is to simplify the hospital admission process. The three subsystems already have their own methods, but the client should not have to manage them separately.

Facade combines their operations into one workflow and handles failures between stages. Adapter would be useful if two classes had incompatible interfaces and needed to work together.

In this project, there is no interface compatibility problem, so Facade is more suitable.

# Lab Review Preparation - Hospital Management System

This document maps specific lab review topics to the codebase of the Hospital Management System project.

## 1. Database Schema Design and Normalization

**Requirement:** *Design and normalize a relational database schema that models a hospital management domain effectively.*

*   **Project Implementation:** Verified in `src/main/resources/db/schema.sql`.
*   **Entities:**
    *   **Patient**: Stores core personal info (`patient_id`, `first_name`, `email`).
    *   **Doctor**: Stores provider info and links to `department` (`doctor_id`, `specialization`).
    *   **Appointment**: Junction table linking `Patient` and `Doctor` with a specific time (`appointment_id`, `status`).
    *   **Medical Record/Prescription**: Clinical data linked to patients.
*   **Normalization (3NF):**
    *   **1NF (Atomic Values):** All columns like `first_name`, `phone` contain single values. No repeating groups (e.g., no "List of Symptoms" column in `patient` table; moved to `medical_record`).
    *   **2NF (No Partial Dependencies):** Primary keys are single attributes (e.g., `patient_id`), so all non-key attributes fully depend on the PK.
    *   **3NF (No Transitive Dependencies):** Address/Contact info depends only on the Patient, not on the Doctor. Doctor details (Name, Specialization) are stored in the `doctor` table, referenced only by `doctor_id` in the `appointment` table, preventing data redundancy.

## 2. Conceptual, Logical, and Physical Models

**Requirement:** *Develop conceptual, logical, and physical database models to ensure scalability and maintainability.*

*   **Conceptual Model (ER Diagram):**
    *   *Entities*: Patient, Doctor, Department.
    *   *Relationships*: A Doctor *belongs to* a Department. A Patient *makes* an Appointment with a Doctor. A Doctor *writes* a Prescription for a Patient.
*   **Logical Model (Relational Schema):**
    *   Foreign Keys enforce integrity. Example: `CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id)`.
    *   This ensures you cannot delete a patient who has active appointments without cascading effects (line 49 `ON DELETE CASCADE`), maintaining data referential integrity.
*   **Physical Model (PostgreSQL):**
    *   Implementation using specific SQL types: `serial` (auto-increment int), `varchar` (variable strings), `date`, `text`.
    *   *Scalability*: Use of `SERIAL` primary keys allows for millions of records. `HikariCP` connection pooling manages high concurrent loads.

## 3. CRUD Operations and Complex Queries with JDBC

**Requirement:** *Implement CRUD operations and complex queries using SQL and JDBC.*

*   **Implementation Location:** `src/main/java/com/hospital/dao/PatientDAOImpl.java`.
*   **CRUD Mapping:**
    *   **Create**: `create(Patient p)` uses `INSERT INTO patient ... RETURNING patient_id` (Lines 25-52).
    *   **Read**: `findById(int id)` uses `SELECT ... WHERE patient_id = ?` (Lines 62-74).
    *   **Update**: `update(Patient p)` uses `UPDATE patient SET ... WHERE patient_id = ?` (Lines 104-122).
    *   **Delete**: `delete(int id)` uses `DELETE FROM patient WHERE ...` (Lines 132-140).
*   **Complex Queries:**
    *   **Search**: `searchByName(String name)` (Line 150) uses pattern matching:
        ```sql
        SELECT ... FROM patient WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?
        ```
    *   This allows case-insensitive partial searching, which is more complex than a standard lookup.
*   **JDBC Features:**
    *   **PreparedStatement**: Used everywhere (e.g., line 28 `conn.prepareStatement(sql)`) to prevent SQL Injection attacks.
    *   **Resource Management**: Uses try-with-resources (Java 7+) to ensure `Connection`, `Statement`, and `ResultSet` are closed automatically (Lines 27, 40).

## 4. Algorithms, Indexing, and Optimization

**Requirement:** *Apply indexing, hashing, searching, and sorting algorithms to optimize database access.*

*   **Indexing:**
    *   Defined in `schema.sql` (Lines 142-150):
        *   `CREATE INDEX idx_patient_name ON patient (last_name, first_name);` -> Optimizes the `searchByName` query significantly (O(log n) vs O(n)).
        *   `CREATE INDEX idx_appointment_date ON appointment (appointment_date);` -> Optimizes finding appointments for a specific day.
*   **Searching:**
    *   SQL `LIKE` operator is used for string pattern matching.
    *   Primary Key lookups (`WHERE patient_id = ?`) use the underlying B-Tree index of the database for O(log n) access.
*   **Sorting:**
    *   While not explicitly shown in current DAO methods, adding `ORDER BY date_of_birth DESC` to SQL queries would offload sorting algorithms (usually Merge Sort or Quick Sort variants) to the Database Engine, which is highly optimized.

## 5. JavaFX Integration

**Requirement:** *Integrate database operations into a JavaFX application interface.*

*   **Architecture:** `MVC (Model-View-Controller)`.
*   **Flow Example:**
    1.  **View (FXML)**: User fills form in `patient-registration.fxml`.
    2.  **Controller**: `PatientManagementController.java` captures input on button click.
    3.  **Service**: Controller calls `PatientService.registerPatient()`.
    4.  **DAO**: Service calls `PatientDAO.create()`.
    5.  **Database**: Data persists to PostgreSQL.
    6.  **Feedback**: Controller receives success ID and shows a JavaFX `Alert`.
*   **Async Operations**: Database calls should ideally run on background threads (using `Task` or `CompletableFuture`) to keep the JavaFX UI responsive (prevent freezing).

## 6. Relational vs NoSQL for Unstructured Data

**Requirement:** *Compare relational and NoSQL designs for unstructured data storage such as patient notes or medical logs.*

*   **Relational (PostgreSQL) - "Structured":**
    *   Used for: `Patients`, `Departments`.
    *   Why: Structure is known and rigid. A patient always has a name and DOB. Strong consistency (ACID) is required.
*   **NoSQL (MongoDB) - "Unstructured":**
    *   **Project Implementation:** `com.hospital.util.MongoConnection` and `PatientNoteDAO`.
    *   Used for: `Patient Notes / Medical Logs`.
    *   Why: Doctors might write free-form notes, attach random metadata, or logs might vary in length and structure per visit. MongoDB (Document store) allows storing JSON-like documents (`BSON`) without predefined columns.
    *   *Code Example*: `PatientNoteDAO.save()` simply inserts a `Document`. No `CREATE TABLE` updates needed if we want to add a "temperature" field tomorrow.

## 7. Performance Measurement and Optimization

**Requirement:** *Measure and document performance improvement through optimization and indexing.*

*   **Optimization Strategy:**
    *   **Connection Pooling**: Uses `HikariCP` (`DatabaseConnection.java`). Instead of creating a new heavy TCP connection for every query (taking ~100ms+), it reuses open connections, reducing latency to <1ms.
    *   **Indexing**: The `idx_patient_name` index allows the database to jump directly to "Smith" in the B-Tree instead of scanning every row in the table.
*   **Measurement (Hypothetical Lab Scenario):**
    *   *Before Index*: Run `EXPLAIN ANALYZE SELECT * FROM patient WHERE last_name = 'Doe'` on 100k rows. Result: Sequential Scan (Slow).
    *   *After Index*: Run same command. Result: Index Scan (Fast).

## 8. Project Structure & File Layout

**Purpose:** *Explain the file structure and what each folder is doing in relation to the project.*

*   **`src/main/java/com/hospital`**: Root package for Java source code.
    *   **`controller/`**: Handles user interactions from JavaFX views. Connects the View (FXML) to the Service layer.
        *   *Key Files*: `DashboardController.java`, `PatientManagementController.java`.
    *   **`dao/` (Data Access Object)**: Directly interacts with the database. Contains SQL queries and MongoDB commands. Abstraction layer to hide DB details from the rest of the app.
        *   *Key Files*: `PatientDAOImpl.java` (PostgreSQL), `PatientNoteDAO.java` (MongoDB).
    *   **`model/`**: POJOs (Plain Old Java Objects) representing database entities.
        *   *Key Files*: `Patient.java`, `PatientNote.java`.
    *   **`service/`**: Business logic layer. Validates input from controllers before calling DAOs.
        *   *Key Files*: `PatientService.java`.
    *   **`util/`**: Helper typically static classes for configurations and common tasks.
        *   *Key Files*: `DatabaseConnection.java` (HikariCP), `MongoConnection.java`, `ValidationUtil.java`.
    *   **`Main.java`**: Entry point of the Application. Launches the JavaFX stage.

*   **`src/main/resources`**: Non-code assets.
    *   **`db/`**: Database scripts.
        *   *Key Files*: `schema.sql` (Creates tables and indexes).
    *   **`fxml/`**: UI layout files defined in XML format.
        *   *Key Files*: `main-view.fxml`, `patient-registration.fxml`.
    *   **`styles.css`**: CSS stylesheet for customizing the look and feel of the JavaFX application.
    *   **`config.properties`**: Configuration file for database credentials (`DB_URL`, `DB_USER`).

# Hospital Management System

This repository contains a **Medical Record System** built with **JavaFX** (MVC pattern), utilizing a hybrid database architecture:
-   **PostgreSQL**: For structured data (Patients, Doctors, Appointments, Departments).
-   **MongoDB**: For unstructured data (Patient Medical Logs/Notes).

## Prerequisites
-   Java 17+
-   Maven
-   PostgreSQL local server (default port 5432)
-   MongoDB local server (default port 27017)

## Quick Start

1.  **Database Configuration**
    -   Configure PostgreSQL settings in `src/main/resources/config.properties`.
    -   Ensure MongoDB is running locally on port 27017 (standard).

2.  **PostgreSQL Setup**
    -   Ensure the `hospital_db` database exists.
    -   Run the schema script located at `src/main/resources/db/schema.sql` using `psql`:
        ```bash
        psql -h localhost -U postgres -d hospital_db -f src/main/resources/db/schema.sql
        ```
    -   *Or manually create the tables:*
        ```sql
        CREATE TABLE patients (
          patient_id SERIAL PRIMARY KEY,
          first_name TEXT,
          last_name TEXT,
          date_of_birth DATE,
          gender TEXT,
          phone TEXT,
          email TEXT,
          address TEXT,
          blood_group TEXT,
          registration_date DATE
        );
        ```

3.  **Build**
    ```bash
    mvn clean package
    ```

4.  **Run**
    -   Launch `com.hospital.Main` from your IDE.
    -   *Or via Maven:*
        ```bash
        mvn javafx:run
        ```

## Architecture Notes
-   **Design Pattern**: MVC (Model-View-Controller).
-   **Persistence**:
    -   `HikariCP` connection pooling for PostgreSQL.
    -   `mongodb-driver-sync` for MongoDB interactions.
-   **UI**: JavaFX with FXML and CSS styling.
-   **Utilities**: Centralized database connection handling (`DatabaseConnection`, `MongoConnection`) and Validation utilities.

## Next Steps
-   [ ] Implement Authentication/Login screen.
-   [ ] Add advanced reporting and analytics.
-   [ ] Enhance UI with more interactive dashboards.

Hospital Management System - Starter

This repository contains a starter JavaFX + JDBC (PostgreSQL) project with MVC structure.

Quick start

1. Configure database in `src/main/resources/config.properties` or via environment variables `DB_URL`, `DB_USER`, `DB_PASS`.
2. Ensure PostgreSQL is running and the `patients` table exists. Example schema snippet:

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

3. Build in IDE or with Maven:

```bash
mvn clean package
```

4. Run from IDE by launching `com.hospital.Main` or use Maven exec plugin (IDE recommended for JavaFX runtime args).

Notes
- Uses HikariCP for connection pooling.
- PreparedStatements used throughout to avoid SQL injection.
- Basic in-memory cache implemented for patients.
- Logging uses `System.out.println` for now.

Database schema

You can apply the provided schema with psql. The SQL file is at `src/main/resources/db/schema.sql`.
Apply it with:

```bash
psql -h localhost -U postgres -d hospital_db -f src/main/resources/db/schema.sql
```

Next steps
- Implement navigation to load `patient-registration.fxml` into the main view.
- Add validations and more DAOs/services for doctors, departments, appointments.
- Replace `System.out.println` with a logging framework.

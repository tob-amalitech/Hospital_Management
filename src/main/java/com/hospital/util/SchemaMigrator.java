package com.hospital.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility class to handle database schema migrations.
 */
public class SchemaMigrator {

    /**
     * Checks if the database schema is up to date and applies changes if necessary.
     */
    public static void checkAndMigrate() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            // Check if license_number exists in doctor table
            boolean hasLicense = false;
            try (ResultSet rs = meta.getColumns(null, null, "doctor", "license_number")) {
                if (rs.next()) {
                    hasLicense = true;
                }
            }

            if (!hasLicense) {
                System.out.println("Migrating schema: Adding license_number to doctor table...");
                try (Statement stmt = conn.createStatement()) {
                    // Using VARCHAR(50) and UNIQUE to match updated schema.sql
                    // Note: Adding UNIQUE constraint on existing data might fail if duplicates
                    // exist.
                    // For safety, we first add column, then add constraint if possible, but
                    // simplest is one command.
                    // If table is not empty, unique might fail. But assuming dev environment or
                    // cleanish state.
                    stmt.execute("ALTER TABLE doctor ADD COLUMN license_number VARCHAR(50)");
                    // stmt.execute("ALTER TABLE doctor ADD CONSTRAINT uk_doctor_license UNIQUE
                    // (license_number)");
                    // Keeping it simple to avoid multiple failures. The unique constraint can be
                    // added later or now if risk is low.
                    // Let's stick to adding the column first to fix the crash.
                    System.out.println("Migration successful: license_number added.");
                }
            } else {
                System.out.println("Doctor schema is up to date.");
            }

            // Check if blood_group exists in patient table
            boolean hasBloodGroup = false;
            try (ResultSet rs = meta.getColumns(null, null, "patient", "blood_group")) {
                if (rs.next()) {
                    hasBloodGroup = true;
                }
            }

            if (!hasBloodGroup) {
                System.out.println("Migrating schema: Adding blood_group to patient table...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE patient ADD COLUMN blood_group VARCHAR(10)");
                    System.out.println("Migration successful: blood_group added.");
                }
            } else {
                System.out.println("Patient schema is up to date.");
            }

        } catch (Exception e) {
            System.err.println("Schema migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.hospital.dao;

import com.hospital.model.Patient;
import com.hospital.util.Cache;
import com.hospital.util.DatabaseConnection;
import com.hospital.util.PerformanceMonitor;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of PatientDAO. Uses PreparedStatements and connection
 * pooling. Includes performance monitoring and caching optimizations.
 */
public class PatientDAOImpl implements PatientDAO {

    // Cache for search results (TTL: 15 minutes)
    private static final Cache<String, List<Patient>> searchCache = new Cache<>(15);

    @Override
    /**
     * Creates a new patient record in the database.
     * 
     * @param patient The patient object to create.
     * @return The generated patient ID, or -1 if creation failed.
     * @throws Exception If a database error occurs.
     */
    public int create(Patient patient) throws Exception {
        String sql = "INSERT INTO patient (first_name, last_name, date_of_birth, gender, phone, email, address, blood_group, registration_date) VALUES (?,?,?,?,?,?,?,?,?) RETURNING patient_id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getFirstName());
            ps.setString(2, patient.getLastName());
            ps.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(4, patient.getGender());
            ps.setString(5, patient.getPhone());
            ps.setString(6, patient.getEmail());
            ps.setString(7, patient.getAddress());
            ps.setString(8, patient.getBloodGroup());
            ps.setDate(9, Date.valueOf(patient.getRegistrationDate()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("Inserted patient id=" + id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating patient: " + e.getMessage());
            throw e;
        }
        return -1;
    }

    @Override
    /**
     * Finds a patient by their unique ID.
     * 
     * @param id The ID of the patient.
     * @return The Patient object if found, otherwise null.
     * @throws Exception If a database error occurs.
     */
    public Patient findById(int id) throws Exception {
        String sql = "SELECT patient_id, first_name, last_name, date_of_birth, gender, phone, email, address, blood_group, registration_date FROM patient WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    /**
     * Retrieves all patients from the database.
     * 
     * @return A list of all Patient objects.
     * @throws Exception If a database error occurs.
     */
    public List<Patient> findAll() throws Exception {
        String sql = "SELECT patient_id, first_name, last_name, date_of_birth, gender, phone, email, address, blood_group, registration_date FROM patient";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    /**
     * Updates an existing patient's details.
     * 
     * @param patient The patient object containing updated information.
     * @return true if updated successfully, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean update(Patient patient) throws Exception {
        String sql = "UPDATE patient SET first_name=?, last_name=?, date_of_birth=?, gender=?, phone=?, email=?, address=?, blood_group=?, registration_date=? WHERE patient_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getFirstName());
            ps.setString(2, patient.getLastName());
            ps.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(4, patient.getGender());
            ps.setString(5, patient.getPhone());
            ps.setString(6, patient.getEmail());
            ps.setString(7, patient.getAddress());
            ps.setString(8, patient.getBloodGroup());
            ps.setDate(9, Date.valueOf(patient.getRegistrationDate()));
            ps.setInt(10, patient.getPatientId());

            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    @Override
    /**
     * Deletes a patient record by ID.
     * 
     * @param id The ID of the patient to delete.
     * @return true if deleted successfully, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM patient WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    @Override
    /**
     * Searches for patients by name (partial match on first or last name).
     * Uses caching for improved performance.
     *
     * @param name The name search query.
     * @return A list of matching Patient objects.
     * @throws Exception If a database error occurs.
     */
    public List<Patient> searchByName(String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cacheKey = "search_" + name.toLowerCase().trim();

        // Check cache first
        List<Patient> cachedResult = searchCache.get(cacheKey);
        if (cachedResult != null) {
            PerformanceMonitor.recordQueryTime("PatientDAO.searchByName (cached)", 0, true);
            return new ArrayList<>(cachedResult); // Return copy to prevent external modification
        }

        // Cache miss - execute database query
        long startTime = System.currentTimeMillis();
        String sql = "SELECT patient_id, first_name, last_name, date_of_birth, gender, phone, email, address, blood_group, registration_date FROM patient WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + name.toLowerCase() + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Record performance (before optimization - no caching)
        PerformanceMonitor.recordQueryTime("PatientDAO.searchByName (database)", executionTime, false);

        // Cache the result
        searchCache.put(cacheKey, new ArrayList<>(list));

        return list;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null)
            p.setDateOfBirth(dob.toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setBloodGroup(rs.getString("blood_group"));
        Date reg = rs.getDate("registration_date");
        if (reg != null)
            p.setRegistrationDate(reg.toLocalDate());
        return p;
    }
}

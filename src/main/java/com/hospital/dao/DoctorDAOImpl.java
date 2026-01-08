package com.hospital.dao;

import com.hospital.model.Doctor;
import com.hospital.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for DoctorDAO
 */
public class DoctorDAOImpl implements DoctorDAO {

    @Override
    /**
     * Creates a new doctor record.
     * 
     * @param doctor The doctor object to create.
     * @return The generated doctor ID.
     * @throws Exception If a database error occurs.
     */
    public int create(Doctor doctor) throws Exception {
        String sql = "INSERT INTO doctor (first_name, last_name, specialization, phone, email, department_id, license_number) VALUES (?,?,?,?,?,?,?) RETURNING doctor_id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getFirstName());
            ps.setString(2, doctor.getLastName());
            ps.setString(3, doctor.getSpecialization());
            ps.setString(4, doctor.getPhone());
            ps.setString(5, doctor.getEmail());
            ps.setInt(6, doctor.getDepartmentId() == null ? 0 : doctor.getDepartmentId());
            ps.setString(7, doctor.getLicenseNumber());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("Inserted doctor id=" + id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating doctor: " + e.getMessage());
            throw e;
        }
        return -1;
    }

    @Override
    /**
     * Finds a doctor by ID.
     * 
     * @param id The doctor's ID.
     * @return The Doctor object if found, null otherwise.
     * @throws Exception If a database error occurs.
     */
    public Doctor findById(int id) throws Exception {
        String sql = "SELECT doctor_id, first_name, last_name, specialization, phone, email, department_id, license_number FROM doctor WHERE doctor_id = ?";
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
     * Retrieves all doctors.
     * 
     * @return A list of all Doctor objects.
     * @throws Exception If a database error occurs.
     */
    public List<Doctor> findAll() throws Exception {
        String sql = "SELECT doctor_id, first_name, last_name, specialization, phone, email, department_id, license_number FROM doctor";
        List<Doctor> list = new ArrayList<>();
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
     * Finds doctors belonging to a specific department.
     * 
     * @param departmentId The department ID.
     * @return A list of doctors in the department.
     * @throws Exception If a database error occurs.
     */
    public List<Doctor> findByDepartment(int departmentId) throws Exception {
        String sql = "SELECT doctor_id, first_name, last_name, specialization, phone, email, department_id, license_number FROM doctor WHERE department_id = ?";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    /**
     * Finds doctors by specialization.
     * 
     * @param specialization The specialization to filter by.
     * @return A list of doctors with the given specialization.
     * @throws Exception If a database error occurs.
     */
    public List<Doctor> findBySpecialization(String specialization) throws Exception {
        String sql = "SELECT doctor_id, first_name, last_name, specialization, phone, email, department_id, license_number FROM doctor WHERE specialization = ?";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, specialization);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    /**
     * Updates a doctor's information.
     * 
     * @param doctor The doctor object with updated details.
     * @return true if successful, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean update(Doctor doctor) throws Exception {
        String sql = "UPDATE doctor SET first_name=?, last_name=?, specialization=?, phone=?, email=?, department_id=?, license_number=? WHERE doctor_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getFirstName());
            ps.setString(2, doctor.getLastName());
            ps.setString(3, doctor.getSpecialization());
            ps.setString(4, doctor.getPhone());
            ps.setString(5, doctor.getEmail());
            ps.setInt(6, doctor.getDepartmentId() == null ? 0 : doctor.getDepartmentId());
            ps.setString(7, doctor.getLicenseNumber());
            ps.setInt(8, doctor.getDoctorId());
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    @Override
    /**
     * Deletes a doctor by ID.
     * 
     * @param id The doctor's ID.
     * @return true if successful, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM doctor WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    @Override
    /**
     * Searches for doctors by name (partial match).
     * 
     * @param name The name search query.
     * @return A list of matching Doctor objects.
     * @throws Exception If a database error occurs.
     */
    public List<Doctor> searchByName(String name) throws Exception {
        String sql = "SELECT doctor_id, first_name, last_name, specialization, phone, email, department_id, license_number FROM doctor WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        List<Doctor> list = new ArrayList<>();
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
        return list;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setFirstName(rs.getString("first_name"));
        d.setLastName(rs.getString("last_name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setLicenseNumber(rs.getString("license_number"));
        int dept = rs.getInt("department_id");
        if (!rs.wasNull())
            d.setDepartmentId(dept);
        return d;
    }
}

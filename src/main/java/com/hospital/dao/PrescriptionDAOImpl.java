package com.hospital.dao;

import com.hospital.model.Prescription;
import com.hospital.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for PrescriptionDAO
 */
public class PrescriptionDAOImpl implements PrescriptionDAO {

    @Override
    /**
     * Creates a new prescription record.
     * 
     * @param patientId     The ID of the patient.
     * @param doctorId      The ID of the doctor issuing the prescription.
     * @param appointmentId The ID of the associated appointment (nullable).
     * @return The generated prescription ID.
     * @throws Exception If a database error occurs.
     */
    public int create(int patientId, int doctorId, Integer appointmentId) throws Exception {
        String sql = "INSERT INTO prescription (patient_id, doctor_id, appointment_id) VALUES (?,?,?) RETURNING prescription_id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            if (appointmentId == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        return -1;
    }

    @Override
    /**
     * Finds a prescription by its ID.
     * 
     * @param id The ID of the prescription.
     * @return The Prescription object if found, null otherwise.
     * @throws Exception If a database error occurs.
     */
    public Prescription findById(int id) throws Exception {
        String sql = "SELECT prescription_id, prescription_date, patient_id, doctor_id, appointment_id FROM prescription WHERE prescription_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prescription p = mapRow(rs);
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    /**
     * Retrieves all prescriptions associated with a specific patient.
     * 
     * @param patientId The ID of the patient.
     * @return A list of Prescription objects.
     * @throws Exception If a database error occurs.
     */
    public List<Prescription> findByPatient(int patientId) throws Exception {
        String sql = "SELECT prescription_id, prescription_date, patient_id, doctor_id, appointment_id FROM prescription WHERE patient_id = ?";
        List<Prescription> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    /**
     * Finds a prescription associated with a specific appointment.
     * 
     * @param appointmentId The ID of the appointment.
     * @return The Prescription object if found, null otherwise.
     * @throws Exception If a database error occurs.
     */
    public Prescription findByAppointment(int appointmentId) throws Exception {
        String sql = "SELECT prescription_id, prescription_date, patient_id, doctor_id, appointment_id FROM prescription WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    /**
     * Updates an existing prescription.
     * 
     * @param prescription The prescription object with updated details.
     * @return true if updated successfully, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean update(Prescription prescription) throws Exception {
        String sql = "UPDATE prescription SET patient_id = ?, doctor_id = ?, appointment_id = ? WHERE prescription_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prescription.getPatientId());
            ps.setInt(2, prescription.getDoctorId());
            if (prescription.getAppointmentId() == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, prescription.getAppointmentId());
            ps.setInt(4, prescription.getPrescriptionId());
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    @Override
    /**
     * Deletes a prescription by its ID.
     * 
     * @param id The ID of the prescription to delete.
     * @return true if deleted successfully, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM prescription WHERE prescription_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        }
    }

    private Prescription mapRow(ResultSet rs) throws SQLException {
        Prescription p = new Prescription();
        p.setPrescriptionId(rs.getInt("prescription_id"));
        Date d = rs.getDate("prescription_date");
        if (d != null)
            p.setPrescriptionDate(d.toLocalDate());
        p.setPatientId(rs.getInt("patient_id"));
        p.setDoctorId(rs.getInt("doctor_id"));
        int apId = rs.getInt("appointment_id");
        if (!rs.wasNull())
            p.setAppointmentId(apId);
        return p;
    }
}

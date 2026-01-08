package com.hospital.dao;

import com.hospital.model.Appointment;
import com.hospital.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for AppointmentDAO
 */
public class AppointmentDAOImpl implements AppointmentDAO {

    @Override
    /**
     * Creates a new appointment.
     * 
     * @param appointment The appointment details.
     * @return The generated appointment ID.
     * @throws Exception If a database error occurs.
     */
    public int create(Appointment appointment) throws Exception {
        String sql = "INSERT INTO appointment (appointment_date, appointment_time, status, patient_id, doctor_id) VALUES (?,?,?,?,?) RETURNING appointment_id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(2, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(3, appointment.getStatus());
            ps.setInt(4, appointment.getPatientId());
            ps.setInt(5, appointment.getDoctorId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("Inserted appointment id=" + id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating appointment: " + e.getMessage());
            throw e;
        }
        return -1;
    }

    @Override
    /**
     * Finds an appointment by ID.
     * 
     * @param id The appointment ID.
     * @return The Appointment object if found, null otherwise.
     * @throws Exception If a database error occurs.
     */
    public Appointment findById(int id) throws Exception {
        String sql = "SELECT appointment_id, appointment_date, appointment_time, status, patient_id, doctor_id FROM appointment WHERE appointment_id = ?";
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
     * Retrieves all appointments.
     * 
     * @return A list of all Appointment objects.
     * @throws Exception If a database error occurs.
     */
    public List<Appointment> findAll() throws Exception {
        String sql = "SELECT appointment_id, appointment_date, appointment_time, status, patient_id, doctor_id FROM appointment";
        List<Appointment> list = new ArrayList<>();
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
     * Updates an existing appointment.
     * 
     * @param appointment The updated appointment object.
     * @return true if successful, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean update(Appointment appointment) throws Exception {
        String sql = "UPDATE appointment SET appointment_date=?, appointment_time=?, status=?, patient_id=?, doctor_id=? WHERE appointment_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(2, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(3, appointment.getStatus());
            ps.setInt(4, appointment.getPatientId());
            ps.setInt(5, appointment.getDoctorId());
            ps.setInt(6, appointment.getAppointmentId());
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    @Override
    /**
     * Deletes an appointment by ID.
     * 
     * @param id The appointment ID.
     * @return true if successful, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM appointment WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    @Override
    /**
     * Finds appointments by date.
     * 
     * @param date The date to filter by.
     * @return A list of appointments on the given date.
     * @throws Exception If a database error occurs.
     */
    public List<Appointment> findByDate(LocalDate date) throws Exception {
        String sql = "SELECT appointment_id, appointment_date, appointment_time, status, patient_id, doctor_id FROM appointment WHERE appointment_date = ?";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    /**
     * Finds appointments for a specific doctor.
     * 
     * @param doctorId The doctor's ID.
     * @return A list of appointments for the doctor.
     * @throws Exception If a database error occurs.
     */
    public List<Appointment> findByDoctorId(int doctorId) throws Exception {
        String sql = "SELECT appointment_id, appointment_date, appointment_time, status, patient_id, doctor_id FROM appointment WHERE doctor_id = ?";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    /**
     * Finds appointments for a specific patient.
     * 
     * @param patientId The patient's ID.
     * @return A list of appointments for the patient.
     * @throws Exception If a database error occurs.
     */
    public List<Appointment> findByPatientId(int patientId) throws Exception {
        String sql = "SELECT appointment_id, appointment_date, appointment_time, status, patient_id, doctor_id FROM appointment WHERE patient_id = ?";
        List<Appointment> list = new ArrayList<>();
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
     * Updates the status of an appointment.
     * 
     * @param appointmentId The appointment ID.
     * @param status        The new status string.
     * @return true if successful, false otherwise.
     * @throws Exception If a database error occurs.
     */
    public boolean updateStatus(int appointmentId, String status) throws Exception {
        String sql = "UPDATE appointment SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        Date d = rs.getDate("appointment_date");
        if (d != null)
            a.setAppointmentDate(d.toLocalDate());
        Time t = rs.getTime("appointment_time");
        if (t != null)
            a.setAppointmentTime(t.toLocalTime());
        a.setStatus(rs.getString("status"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        return a;
    }
}

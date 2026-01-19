package com.hospital.dao;

import com.hospital.model.MedicalRecord;
import com.hospital.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAOImpl implements MedicalRecordDAO {

    @Override
    public void add(MedicalRecord record) {
        String sql = "INSERT INTO medical_record (diagnosis, treatment, record_date, patient_id, doctor_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.getDiagnosis());
            stmt.setString(2, record.getTreatment());
            stmt.setDate(3, Date.valueOf(record.getRecordDate()));
            stmt.setInt(4, record.getPatientId());
            stmt.setInt(5, record.getDoctorId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding medical record", e);
        }
    }

    @Override
    public List<MedicalRecord> getByPatientId(int patientId) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM medical_record WHERE patient_id = ? ORDER BY record_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MedicalRecord r = new MedicalRecord();
                    r.setRecordId(rs.getInt("record_id"));
                    r.setDiagnosis(rs.getString("diagnosis"));
                    r.setTreatment(rs.getString("treatment"));
                    r.setRecordDate(rs.getDate("record_date").toLocalDate());
                    r.setPatientId(rs.getInt("patient_id"));
                    r.setDoctorId(rs.getInt("doctor_id"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching medical records", e);
        }
        return list;
    }
}

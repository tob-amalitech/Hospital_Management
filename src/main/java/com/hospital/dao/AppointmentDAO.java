package com.hospital.dao;

import com.hospital.model.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentDAO {
    int create(Appointment appointment) throws Exception;
    Appointment findById(int id) throws Exception;
    List<Appointment> findAll() throws Exception;
    boolean update(Appointment appointment) throws Exception;
    boolean delete(int id) throws Exception;
    List<Appointment> findByDate(LocalDate date) throws Exception;
    List<Appointment> findByDoctorId(int doctorId) throws Exception;
    List<Appointment> findByPatientId(int patientId) throws Exception;
    boolean updateStatus(int appointmentId, String status) throws Exception;
}

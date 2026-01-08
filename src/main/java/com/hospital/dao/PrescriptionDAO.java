package com.hospital.dao;

import com.hospital.model.Appointment;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;

import java.util.List;

public interface PrescriptionDAO {
    int create(int patientId, int doctorId, Integer appointmentId) throws Exception;
    Prescription findById(int id) throws Exception;
    List<Prescription> findByPatient(int patientId) throws Exception;
    Prescription findByAppointment(int appointmentId) throws Exception;
    boolean update(Prescription prescription) throws Exception;
    boolean delete(int id) throws Exception;
}

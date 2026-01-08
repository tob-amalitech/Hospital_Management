package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.dao.PatientDAOImpl;
import com.hospital.model.Patient;
import com.hospital.util.Cache;

import java.util.List;

/**
 * Business logic for patient operations. Uses DAO and Cache.
 */
public class PatientService {
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final Cache<Integer, Patient> cache = new Cache<>();

    public int registerPatient(Patient patient) throws Exception {
        int id = patientDAO.create(patient);
        if (id > 0) {
            patient.setPatientId(id);
            cache.put(id, patient);
        }
        return id;
    }

    public Patient getPatient(int id) throws Exception {
        Patient p = cache.get(id);
        if (p != null) return p;
        p = patientDAO.findById(id);
        if (p != null) cache.put(id, p);
        return p;
    }

    public List<Patient> getAllPatients() throws Exception {
        return patientDAO.findAll();
    }

    public boolean updatePatient(Patient patient) throws Exception {
        boolean ok = patientDAO.update(patient);
        if (ok) cache.put(patient.getPatientId(), patient);
        return ok;
    }

    public boolean deletePatient(int id) throws Exception {
        boolean ok = patientDAO.delete(id);
        if (ok) cache.remove(id);
        return ok;
    }

    public List<Patient> searchPatients(String name) throws Exception {
        return patientDAO.searchByName(name);
    }
}

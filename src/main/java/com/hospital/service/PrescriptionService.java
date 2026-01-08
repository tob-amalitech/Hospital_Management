package com.hospital.service;

import com.hospital.dao.PrescriptionDAO;
import com.hospital.dao.PrescriptionDAOImpl;
import com.hospital.model.Prescription;
import com.hospital.util.Cache;

import java.util.List;

public class PrescriptionService {
    private final PrescriptionDAO dao = new PrescriptionDAOImpl();
    private final Cache<Integer, Prescription> cache = new Cache<>();

    public int createPrescription(int patientId, int doctorId, Integer appointmentId) throws Exception {
        int id = dao.create(patientId, doctorId, appointmentId);
        return id;
    }

    public Prescription getPrescription(int id) throws Exception { Prescription p = cache.get(id); if (p!=null) return p; p = dao.findById(id); if (p!=null) cache.put(id,p); return p; }
    public List<Prescription> getByPatient(int patientId) throws Exception { return dao.findByPatient(patientId); }
    public Prescription getByAppointment(int appointmentId) throws Exception { return dao.findByAppointment(appointmentId); }
    public boolean updatePrescription(Prescription p) throws Exception { boolean ok = dao.update(p); if (ok) cache.put(p.getPrescriptionId(), p); return ok; }
    public boolean deletePrescription(int id) throws Exception { boolean ok = dao.delete(id); if (ok) cache.remove(id); return ok; }
}

package com.hospital.service;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.AppointmentDAOImpl;
import com.hospital.model.Appointment;
import com.hospital.util.Cache;
import com.hospital.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for scheduling appointments with simple rules:
 * - No past appointments
 * - No double-booking for same doctor at same date/time
 */
public class AppointmentService {
    private final AppointmentDAO dao = new AppointmentDAOImpl();
    private final Cache<Integer, Appointment> cache = new Cache<>();

    public int scheduleAppointment(Appointment a) throws Exception {
        if (a.getAppointmentDate() == null || a.getAppointmentTime() == null) throw new IllegalArgumentException("Date and time required");
        if (!ValidationUtil.validateDateNotPast(a.getAppointmentDate())) throw new IllegalArgumentException("Cannot schedule past date");
        // check double-booking
        List<Appointment> docs = dao.findByDoctorId(a.getDoctorId());
        for (Appointment ex : docs) {
            if (ex.getAppointmentDate().equals(a.getAppointmentDate()) && ex.getAppointmentTime().equals(a.getAppointmentTime())) {
                throw new IllegalStateException("Doctor is already booked at this time");
            }
        }
        int id = dao.create(a);
        if (id > 0) { a.setAppointmentId(id); cache.put(id, a); }
        return id;
    }

    public Appointment getAppointment(int id) throws Exception { Appointment a = cache.get(id); if (a!=null) return a; a = dao.findById(id); if (a!=null) cache.put(id,a); return a; }
    public List<Appointment> getByDate(LocalDate date) throws Exception { return dao.findByDate(date); }
    public List<Appointment> getByDoctor(int doctorId) throws Exception { return dao.findByDoctorId(doctorId); }
    public List<Appointment> getByPatient(int patientId) throws Exception { return dao.findByPatientId(patientId); }
    public boolean updateAppointment(Appointment a) throws Exception { boolean ok = dao.update(a); if (ok) cache.put(a.getAppointmentId(), a); return ok; }
    public boolean updateStatus(int id, String status) throws Exception { boolean ok = dao.updateStatus(id, status); if (ok) { Appointment a = cache.get(id); if (a!=null) { a.setStatus(status); cache.put(id,a); } } return ok; }
    public boolean deleteAppointment(int id) throws Exception { boolean ok = dao.delete(id); if (ok) cache.remove(id); return ok; }
}

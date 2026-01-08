package com.hospital.service;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.DoctorDAOImpl;
import com.hospital.model.Doctor;
import com.hospital.util.Cache;
import com.hospital.util.ValidationUtil;

import java.util.List;

/**
 * Service layer for Doctor operations with simple caching and validation
 */
public class DoctorService {
    private final DoctorDAO doctorDAO = new DoctorDAOImpl();
    private final Cache<Integer, Doctor> cache = new Cache<>();

    public int addDoctor(Doctor doctor) throws Exception {
        if (!ValidationUtil.validateRequired(doctor.getFirstName())
                || !ValidationUtil.validateRequired(doctor.getLastName())) {
            throw new IllegalArgumentException("First and last name are required");
        }
        if (!ValidationUtil.validateEmail(doctor.getEmail())) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (!ValidationUtil.validateRequired(doctor.getLicenseNumber())) {
            throw new IllegalArgumentException("License number is required");
        }

        // Check for duplicate license
        List<Doctor> all = doctorDAO.findAll();
        for (Doctor d : all) {
            // Using equalsIgnoreCase for safety, though strict equality might be intended
            if (d.getLicenseNumber() != null && d.getLicenseNumber().equalsIgnoreCase(doctor.getLicenseNumber())) {
                throw new IllegalArgumentException(
                        "Doctor with this license number already exists: " + doctor.getLicenseNumber());
            }
        }

        int id = doctorDAO.create(doctor);
        if (id > 0) {
            doctor.setDoctorId(id);
            cache.put(id, doctor);
        }
        return id;
    }

    public Doctor getDoctor(int id) throws Exception {
        Doctor d = cache.get(id);
        if (d != null)
            return d;
        d = doctorDAO.findById(id);
        if (d != null)
            cache.put(id, d);
        return d;
    }

    public List<Doctor> getAll() throws Exception {
        return doctorDAO.findAll();
    }

    public List<Doctor> getByDepartment(int deptId) throws Exception {
        return doctorDAO.findByDepartment(deptId);
    }

    public List<Doctor> search(String q) throws Exception {
        return doctorDAO.searchByName(q);
    }

    public boolean updateDoctor(Doctor doctor) throws Exception {
        if (!ValidationUtil.validateRequired(doctor.getLicenseNumber())) {
            throw new IllegalArgumentException("License number is required");
        }

        // Check for duplicate license (excluding self)
        List<Doctor> all = doctorDAO.findAll();
        for (Doctor d : all) {
            if (d.getDoctorId().intValue() != doctor.getDoctorId().intValue() &&
                    d.getLicenseNumber() != null &&
                    d.getLicenseNumber().equalsIgnoreCase(doctor.getLicenseNumber())) {
                throw new IllegalArgumentException(
                        "Doctor with this license number already exists: " + doctor.getLicenseNumber());
            }
        }

        boolean ok = doctorDAO.update(doctor);
        if (ok)
            cache.put(doctor.getDoctorId(), doctor);
        return ok;
    }

    public boolean deleteDoctor(int id) throws Exception {
        // Business rule: prevent delete if doctor has appointments. AppointmentService
        // should be used to check, but to avoid circular dependency
        boolean ok = doctorDAO.delete(id);
        if (ok)
            cache.remove(id);
        return ok;
    }
}

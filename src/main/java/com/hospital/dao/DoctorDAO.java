package com.hospital.dao;

import com.hospital.model.Doctor;

import java.util.List;

public interface DoctorDAO {
    int create(Doctor doctor) throws Exception;

    Doctor findById(int id) throws Exception;

    List<Doctor> findAll() throws Exception;

    List<Doctor> findByDepartment(int departmentId) throws Exception;

    List<Doctor> findBySpecialization(String specialization) throws Exception;

    boolean update(Doctor doctor) throws Exception;

    boolean delete(int id) throws Exception;

    List<Doctor> searchByName(String name) throws Exception;
}

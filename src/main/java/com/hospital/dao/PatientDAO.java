package com.hospital.dao;

import com.hospital.model.Patient;

import java.util.List;

public interface PatientDAO {
    int create(Patient patient) throws Exception;
    Patient findById(int id) throws Exception;
    List<Patient> findAll() throws Exception;
    boolean update(Patient patient) throws Exception;
    boolean delete(int id) throws Exception;
    List<Patient> searchByName(String name) throws Exception;
}

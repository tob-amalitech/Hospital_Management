package com.hospital.service;

import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.MedicalRecordDAOImpl;
import com.hospital.model.MedicalRecord;

import java.util.List;

public class MedicalRecordService {
    private final MedicalRecordDAO dao = new MedicalRecordDAOImpl();

    public List<MedicalRecord> getByPatient(int patientId) {
        return dao.getByPatientId(patientId);
    }

    public void addRecord(MedicalRecord record) {
        dao.add(record);
    }
}

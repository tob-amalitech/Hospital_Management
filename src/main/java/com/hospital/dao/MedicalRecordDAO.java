package com.hospital.dao;

import com.hospital.model.MedicalRecord;
import java.util.List;

public interface MedicalRecordDAO {
    void add(MedicalRecord record);

    List<MedicalRecord> getByPatientId(int patientId);
    // Add other methods as needed
}

package com.hospital.model;

import java.time.LocalDate;

/**
 * Medical Record model
 */
public class MedicalRecord {
    private Integer recordId;
    private String diagnosis;
    private String treatment;
    private LocalDate recordDate;
    private Integer patientId;
    private Integer doctorId;

    public MedicalRecord() {
    }

    public MedicalRecord(Integer recordId, String diagnosis, String treatment, LocalDate recordDate, Integer patientId,
            Integer doctorId) {
        this.recordId = recordId;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.recordDate = recordDate;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "recordId=" + recordId +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatment='" + treatment + '\'' +
                ", recordDate=" + recordDate +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                '}';
    }
}

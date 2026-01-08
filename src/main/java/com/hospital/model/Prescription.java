package com.hospital.model;

import java.time.LocalDate;

/**
 * Prescription model
 */
public class Prescription {
    private Integer prescriptionId;
    private LocalDate prescriptionDate;
    private Integer patientId;
    private Integer doctorId;
    private Integer appointmentId;

    public Prescription() {}

    public Prescription(Integer prescriptionId, LocalDate prescriptionDate, Integer patientId, Integer doctorId, Integer appointmentId) {
        this.prescriptionId = prescriptionId;
        this.prescriptionDate = prescriptionDate;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
    }

    public Integer getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Integer prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
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

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "prescriptionId=" + prescriptionId +
                ", prescriptionDate=" + prescriptionDate +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", appointmentId=" + appointmentId +
                '}';
    }
}

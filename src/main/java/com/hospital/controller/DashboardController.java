package com.hospital.controller;

import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.concurrent.Task;

import java.time.LocalDate;

public class DashboardController {
    @FXML
    private Label lblPatients;
    @FXML
    private Label lblDoctors;
    @FXML
    private Label lblToday;

    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private final AppointmentService appointmentService = new AppointmentService();

    /**
     * Initializes the dashboard.
     */
    @FXML
    public void initialize() {
        refresh();
    }

    /**
     * Refreshes dashboard statistics in background threads.
     */
    private void refresh() {
        // Run independently to not block each other? Or serial?
        // Parallel is fine.

        Task<Integer> taskPatients = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return patientService.getAllPatients().size();
            }
        };
        taskPatients.setOnSucceeded(e -> lblPatients.setText(String.valueOf(taskPatients.getValue())));
        new Thread(taskPatients).start();

        Task<Integer> taskDoctors = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return doctorService.getAll().size();
            }
        };
        taskDoctors.setOnSucceeded(e -> lblDoctors.setText(String.valueOf(taskDoctors.getValue())));
        new Thread(taskDoctors).start();

        Task<Integer> taskAppt = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return appointmentService.getByDate(LocalDate.now()).size();
            }
        };
        taskAppt.setOnSucceeded(e -> lblToday.setText(String.valueOf(taskAppt.getValue())));
        new Thread(taskAppt).start();
    }

    @FXML
    public void onNewPatient() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().openPatientRegistration();
        }
    }

    @FXML
    public void onNewAppointment() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().openAppointments();
        }
    }
}

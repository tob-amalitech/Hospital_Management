package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import com.hospital.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AppointmentSchedulingController {
    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<Doctor> cmbDoctor;
    @FXML
    private ComboBox<Patient> cmbPatient;
    @FXML
    private TextField txtTime;
    @FXML
    private TableView<Appointment> tableAppointments;
    @FXML
    private TableColumn<Appointment, Integer> colId;
    @FXML
    private TableColumn<Appointment, String> colDate;
    @FXML
    private TableColumn<Appointment, String> colTime;
    @FXML
    private TableColumn<Appointment, String> colPatient;
    @FXML
    private TableColumn<Appointment, String> colDoctor;
    @FXML
    private TableColumn<Appointment, String> colStatus;

    private final AppointmentService appointmentService = new AppointmentService();
    private final DoctorService doctorService = new DoctorService();
    private final PatientService patientService = new PatientService();

    private final ObservableList<Appointment> data = FXCollections.observableArrayList();
    private final ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();

    /**
     * Initializes the controller, sets up bindings and loads initial data.
     */
    @FXML
    public void initialize() {
        System.out.println("AppointmentSchedulingController initialized");
        tableAppointments.setItems(data);
        cmbDoctor.setItems(doctors);
        cmbPatient.setItems(patients);

        loadDoctors();
        loadPatients();
        refreshTable();
    }

    /**
     * Loads doctors in a background thread.
     */
    private void loadDoctors() {
        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() throws Exception {
                return doctorService.getAll();
            }
        };
        task.setOnSucceeded(e -> doctors.setAll(task.getValue()));
        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load doctors: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Loads patients in a background thread.
     */
    private void loadPatients() {
        Task<List<Patient>> task = new Task<>() {
            @Override
            protected List<Patient> call() throws Exception {
                return patientService.getAllPatients();
            }
        };
        task.setOnSucceeded(e -> patients.setAll(task.getValue()));
        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load patients: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Refreshes the appointment table in a background thread based on selected
     * date.
     */
    private void refreshTable() {
        LocalDate d = dpDate.getValue();
        final LocalDate date = (d == null) ? LocalDate.now() : d;

        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() throws Exception {
                return appointmentService.getByDate(date);
            }
        };
        task.setOnSucceeded(e -> data.setAll(task.getValue()));
        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load appointments: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Handles the schedule button click. Validates input and creates appointment in
     * background.
     */
    @FXML
    public void onSchedule() {
        Doctor doc = cmbDoctor.getSelectionModel().getSelectedItem();
        Patient pat = cmbPatient.getSelectionModel().getSelectedItem();
        LocalDate date = dpDate.getValue();
        String timeStr = txtTime.getText();

        if (doc == null) {
            AlertUtil.showError("Validation", "Select a doctor");
            return;
        }
        if (pat == null) {
            AlertUtil.showError("Validation", "Select a patient");
            return;
        }
        if (date == null || !ValidationUtil.validateDateNotPast(date)) {
            AlertUtil.showError("Validation", "Invalid date (cannot be past)");
            return;
        }
        if (!ValidationUtil.validateRequired(timeStr)) {
            AlertUtil.showError("Validation", "Time is required");
            return;
        }

        try {
            final LocalTime time = LocalTime.parse(timeStr);
            final Appointment a = new Appointment();
            a.setDoctorId(doc.getDoctorId());
            a.setPatientId(pat.getPatientId());
            a.setAppointmentDate(date);
            a.setAppointmentTime(time);
            a.setStatus("Scheduled");

            Task<Integer> task = new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return appointmentService.scheduleAppointment(a);
                }
            };
            task.setOnSucceeded(e -> {
                AlertUtil.showInfo("Scheduled", "Appointment id=" + task.getValue());
                refreshTable();
            });
            task.setOnFailed(e -> AlertUtil.showError("Schedule Error", task.getException().getMessage()));
            new Thread(task).start();

        } catch (Exception e) {
            AlertUtil.showError("Validation", "Invalid time format (use HH:mm)");
        }
    }
}

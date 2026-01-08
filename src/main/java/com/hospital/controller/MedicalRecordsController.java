package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;
import com.hospital.service.AppointmentService;
import com.hospital.service.PatientService;

// import com.hospital.service.PrescriptionService;
import com.hospital.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Controller for the Medical Records module.
 */
public class MedicalRecordsController {

    @FXML
    private ComboBox<Patient> cmbPatients;
    @FXML
    private TabPane tabPane;

    @FXML
    private TableView<Appointment> tableAppointments;
    @FXML
    private TableColumn<Appointment, String> colDate;
    @FXML
    private TableColumn<Appointment, String> colTime;
    @FXML
    private TableColumn<Appointment, String> colDoctor; // ID for now, later name
    @FXML
    private TableColumn<Appointment, String> colStatus;

    @FXML
    private ListView<Prescription> listPrescriptions;

    @FXML
    private Label lblId;
    @FXML
    private Label lblName;
    @FXML
    private Label lblDob;
    @FXML
    private Label lblContact;
    @FXML
    private Label lblEmail;

    private final PatientService patientService = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();
    // private final PrescriptionService prescriptionService = new
    // PrescriptionService();

    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final ObservableList<Prescription> prescriptions = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        setupTable();
        setupComboBox();
        loadPatients();
    }

    private void setupTable() {
        colDate.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getAppointmentDate().toString()));
        colTime.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getAppointmentTime().toString()));
        colDoctor.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getDoctorId()))); // TODO:
                                                                                                          // Resolve to
                                                                                                          // Doctor Name
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        tableAppointments.setItems(appointments);
        listPrescriptions.setItems(prescriptions);
    }

    private void setupComboBox() {
        cmbPatients.setItems(patients);
        cmbPatients.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient object) {
                return object == null ? ""
                        : object.getFirstName() + " " + object.getLastName() + " (ID: " + object.getPatientId() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null; // Not needed
            }
        });
    }

    /**
     * Loads the list of patients into the ComboBox.
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
     * Loads records for the selected patient.
     */
    @FXML
    public void onLoadRecords() {
        Patient selectedPatient = cmbPatients.getValue();
        if (selectedPatient == null) {
            AlertUtil.showError("Validation", "Please select a patient.");
            return;
        }

        // Populate Personal Info
        lblId.setText(String.valueOf(selectedPatient.getPatientId()));
        lblName.setText(selectedPatient.getFirstName() + " " + selectedPatient.getLastName());
        lblDob.setText(selectedPatient.getDateOfBirth().toString());
        lblContact.setText(selectedPatient.getPhone());
        lblEmail.setText(selectedPatient.getEmail());

        loadAppointments(selectedPatient.getPatientId());
        // loadPrescriptions(selectedPatient.getPatientId());
    }

    private void loadAppointments(int patientId) {
        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() throws Exception {
                return appointmentService.getByPatient(patientId);
            }
        };

        task.setOnSucceeded(e -> {
            appointments.setAll(task.getValue());
        });

        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load appointments: " + task.getException().getMessage()));

        new Thread(task).start();
    }
}

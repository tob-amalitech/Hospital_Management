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
    private final com.hospital.service.PrescriptionService prescriptionService = new com.hospital.service.PrescriptionService();
    private final com.hospital.service.MedicalRecordService medicalRecordService = new com.hospital.service.MedicalRecordService();
    private final com.hospital.service.NoteService noteService = new com.hospital.service.NoteService();

    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final ObservableList<Prescription> prescriptions = FXCollections.observableArrayList();
    private final ObservableList<com.hospital.model.MedicalRecord> medicalRecords = FXCollections.observableArrayList();
    private final ObservableList<com.hospital.model.PatientNote> notes = FXCollections.observableArrayList();

    @FXML
    private TableView<com.hospital.model.MedicalRecord> tableMedicalRecords;
    @FXML
    private TableColumn<com.hospital.model.MedicalRecord, String> colRecordDate;
    @FXML
    private TableColumn<com.hospital.model.MedicalRecord, String> colDiagnosis;
    @FXML
    private TableColumn<com.hospital.model.MedicalRecord, String> colTreatment;

    @FXML
    private TextArea txtNewNote;
    @FXML
    private ListView<com.hospital.model.PatientNote> listNotes;

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

        // Medical Record Columns
        if (colRecordDate != null) {
            colRecordDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRecordDate().toString()));
            colDiagnosis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDiagnosis()));
            colTreatment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTreatment()));
        }

        tableAppointments.setItems(appointments);
        listPrescriptions.setItems(prescriptions);
        if (tableMedicalRecords != null) {
            tableMedicalRecords.setItems(medicalRecords);
        }
        if (listNotes != null) {
            listNotes.setItems(notes);
        }

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
        loadPrescriptions(selectedPatient.getPatientId());
        loadMedicalRecords(selectedPatient.getPatientId());
        loadNotes(selectedPatient.getPatientId());
    }

    private void loadAppointments(int patientId) {
        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() throws Exception {
                return appointmentService.getByPatient(patientId);
            }
        };
        task.setOnSucceeded(e -> appointments.setAll(task.getValue()));
        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load appointments: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void loadPrescriptions(int patientId) {
        Task<List<Prescription>> task = new Task<>() {
            @Override
            protected List<Prescription> call() throws Exception {
                return prescriptionService.getByPatient(patientId);
            }
        };
        task.setOnSucceeded(e -> prescriptions.setAll(task.getValue()));
        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load prescriptions: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void loadMedicalRecords(int patientId) {
        Task<List<com.hospital.model.MedicalRecord>> task = new Task<>() {
            @Override
            protected List<com.hospital.model.MedicalRecord> call() throws Exception {
                return medicalRecordService.getByPatient(patientId);
            }
        };
        task.setOnSucceeded(e -> medicalRecords.setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError("Error",
                "Failed to load medical records: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void loadNotes(int patientId) {
        if (listNotes == null)
            return;
        Task<List<com.hospital.model.PatientNote>> task = new Task<>() {
            @Override
            protected List<com.hospital.model.PatientNote> call() throws Exception {
                return noteService.getNotes(patientId);
            }
        };
        task.setOnSucceeded(e -> notes.setAll(task.getValue()));
        task.setOnFailed(e -> System.err.println("Could not load notes: " + e.getSource().getException()));
        new Thread(task).start();
    }

    @FXML
    public void onSaveNote() {
        Patient selectedPatient = cmbPatients.getValue();
        if (selectedPatient == null) {
            AlertUtil.showError("Validation", "Select a patient first.");
            return;
        }
        if (txtNewNote.getText().isBlank())
            return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                noteService.addNote(selectedPatient.getPatientId(), txtNewNote.getText());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            txtNewNote.clear();
            loadNotes(selectedPatient.getPatientId());
        });
        task.setOnFailed(e -> AlertUtil.showError("Save Error", "Could not save note (Is MongoDB running?)"));
        new Thread(task).start();
    }
}

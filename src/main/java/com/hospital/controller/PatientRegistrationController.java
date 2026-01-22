package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import com.hospital.util.ValidationUtil;
import com.hospital.util.AlertUtil;

import java.time.LocalDate;

/**
 * Controller for patient registration form.
 */
public class PatientRegistrationController {
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private DatePicker dpDob;
    @FXML
    private ComboBox<String> cmbGender;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextArea txtAddress;
    @FXML
    private TextField txtBloodGroup;
    @FXML
    private Button btnSubmit;

    private final PatientService patientService = new PatientService();
    private Patient editingPatient = null; // null means we're creating a new patient

    @FXML
    public void initialize() {
        System.out.println("PatientRegistrationController initialized");
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
    }

    /**
     * Sets the patient to edit. If patient is null, we're creating a new patient.
     */
    public void setEditingPatient(Patient patient) {
        this.editingPatient = patient;
        if (patient != null) {
            populateFormForEditing(patient);
            btnSubmit.setText("Update Patient");
        } else {
            btnSubmit.setText("Register Patient");
        }
    }

    /**
     * Populates the form with patient data for editing.
     */
    private void populateFormForEditing(Patient patient) {
        txtFirstName.setText(patient.getFirstName());
        txtLastName.setText(patient.getLastName());
        dpDob.setValue(patient.getDateOfBirth());
        cmbGender.setValue(patient.getGender());
        txtPhone.setText(patient.getPhone());
        txtEmail.setText(patient.getEmail());
        txtAddress.setText(patient.getAddress());
        txtBloodGroup.setText(patient.getBloodGroup());
    }

    /**
     * Handles patient registration/update form submission.
     */
    @FXML
    public void onSubmit() {
        String fname = txtFirstName.getText();
        String lname = txtLastName.getText();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();
        LocalDate dob = dpDob.getValue();

        if (!ValidationUtil.validateRequired(fname)) {
            AlertUtil.showError("Validation", "First name required");
            return;
        }
        if (!ValidationUtil.validateRequired(lname)) {
            AlertUtil.showError("Validation", "Last name required");
            return;
        }
        if (!ValidationUtil.validatePhone(phone)) {
            AlertUtil.showError("Validation", "Invalid phone format");
            return;
        }
        if (!ValidationUtil.validateEmail(email)) {
            AlertUtil.showError("Validation", "Invalid email format");
            return;
        }
        if (dob != null && !ValidationUtil.validateDatePast(dob)) {
            AlertUtil.showError("Validation", "Date of Birth must be in the past.");
        }

        if (editingPatient != null) {
            // Update existing patient
            handleUpdatePatient(fname, lname, phone, email, dob);
        } else {
            // Create new patient
            handleCreatePatient(fname, lname, phone, email, dob);
        }
    }

    /**
     * Handles creating a new patient.
     */
    private void handleCreatePatient(String fname, String lname, String phone, String email, LocalDate dob) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                Patient p = new Patient();
                p.setFirstName(fname);
                p.setLastName(lname);
                p.setDateOfBirth(dob == null ? LocalDate.now() : dob);
                p.setGender(cmbGender.getValue());
                p.setPhone(phone);
                p.setEmail(email);
                p.setAddress(txtAddress.getText());
                p.setBloodGroup(txtBloodGroup.getText());
                p.setRegistrationDate(LocalDate.now());

                return patientService.registerPatient(p);
            }
        };

        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("Success", "Registered patient with id=" + task.getValue());
            clearForm();
            if (MainController.getInstance() != null) {
                MainController.getInstance().openPatients();
            }
        });

        task.setOnFailed(e -> AlertUtil.showError("Error", "Registration failed: " + task.getException().getMessage()));

        new Thread(task).start();
    }

    /**
     * Handles updating an existing patient.
     */
    private void handleUpdatePatient(String fname, String lname, String phone, String email, LocalDate dob) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Patient p = editingPatient;
                p.setFirstName(fname);
                p.setLastName(lname);
                p.setDateOfBirth(dob);
                p.setGender(cmbGender.getValue());
                p.setPhone(phone);
                p.setEmail(email);
                p.setAddress(txtAddress.getText());
                p.setBloodGroup(txtBloodGroup.getText());

                return patientService.updatePatient(p);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Success", "Patient updated successfully");
                clearForm();
                editingPatient = null;
                btnSubmit.setText("Register Patient");
                if (MainController.getInstance() != null) {
                    MainController.getInstance().openPatients();
                }
            } else {
                AlertUtil.showError("Update Failed", "Failed to update patient");
            }
        });

        task.setOnFailed(e -> AlertUtil.showError("Error", "Update failed: " + task.getException().getMessage()));

        new Thread(task).start();
    }

    private void clearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        dpDob.setValue(null);
        cmbGender.getSelectionModel().clearSelection();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
        txtBloodGroup.clear();
        editingPatient = null;
        btnSubmit.setText("Register Patient");
    }
}

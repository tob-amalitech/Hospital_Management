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
    private ComboBox<String> cmbBloodGroup;
    @FXML
    private Button btnSubmit;

    private final PatientService patientService = new PatientService();
    private Patient editingPatient = null; // null means we're creating a new patient

    @FXML
    public void initialize() {
        System.out.println("PatientRegistrationController initialized");
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        cmbBloodGroup.setItems(FXCollections.observableArrayList(ValidationUtil.getValidBloodGroups()));
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
        cmbBloodGroup.setValue(patient.getBloodGroup());
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
        String bloodGroup = cmbBloodGroup.getValue();
        LocalDate dob = dpDob.getValue();

        // Validate first name
        if (!ValidationUtil.validateRequired(fname)) {
            AlertUtil.showError("Validation", "First name is required");
            return;
        }
        if (!ValidationUtil.validateName(fname)) {
            AlertUtil.showError("Validation", "First name must start with a letter and contain only letters and spaces");
            return;
        }

        // Validate last name
        if (!ValidationUtil.validateRequired(lname)) {
            AlertUtil.showError("Validation", "Last name is required");
            return;
        }
        if (!ValidationUtil.validateName(lname)) {
            AlertUtil.showError("Validation", "Last name must start with a letter and contain only letters and spaces");
            return;
        }

        // Validate phone
        if (!ValidationUtil.validatePhone(phone)) {
            AlertUtil.showError("Validation", "Invalid phone format");
            return;
        }

        // Validate email
        if (!ValidationUtil.validateEmail(email)) {
            AlertUtil.showError("Validation", "Invalid email format");
            return;
        }

        // Validate blood group if provided
        if (bloodGroup != null && !bloodGroup.trim().isEmpty() && !ValidationUtil.validateBloodGroup(bloodGroup)) {
            AlertUtil.showError("Validation", "Invalid blood group. Valid groups are: A+, A-, B+, B-, AB+, AB-, O+, O-");
            return;
        }

        // Validate date of birth
        if (dob != null && !ValidationUtil.validateDatePast(dob)) {
            AlertUtil.showError("Validation", "Date of Birth must be in the past.");
            return;
        }

        if (editingPatient != null) {
            // Update existing patient
            handleUpdatePatient(fname, lname, phone, email, bloodGroup, dob);
        } else {
            // Create new patient
            handleCreatePatient(fname, lname, phone, email, bloodGroup, dob);
        }
    }

    /**
     * Handles creating a new patient.
     */
    private void handleCreatePatient(String fname, String lname, String phone, String email, String bloodGroup, LocalDate dob) {
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
                p.setBloodGroup(bloodGroup);
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
    private void handleUpdatePatient(String fname, String lname, String phone, String email, String bloodGroup, LocalDate dob) {
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
                p.setBloodGroup(bloodGroup);

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
        cmbBloodGroup.getSelectionModel().clearSelection();
        editingPatient = null;
        btnSubmit.setText("Register Patient");
    }
}

package com.hospital.controller;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.util.List;

/**
 * Controller for doctor registration form.
 */
public class DoctorRegistrationController {
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtSpecialization;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtLicense;
    @FXML
    private ComboBox<Department> cmbDepartment;
    @FXML
    private Button btnSubmit;

    private final DoctorService doctorService = new DoctorService();
    private final DepartmentService departmentService = new DepartmentService();
    private Doctor editingDoctor = null; // null means we're creating a new doctor

    @FXML
    public void initialize() {
        System.out.println("DoctorRegistrationController initialized");
        loadDepartments();

        // Setup ComboBox to show Department names
        cmbDepartment.setCellFactory(new Callback<ListView<Department>, ListCell<Department>>() {
            @Override
            public ListCell<Department> call(ListView<Department> p) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Department item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getDepartmentName());
                        }
                    }
                };
            }
        });
        cmbDepartment.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getDepartmentName());
                }
            }
        });
    }

    /**
     * Sets the doctor to edit. If doctor is null, we're creating a new doctor.
     */
    public void setEditingDoctor(Doctor doctor) {
        this.editingDoctor = doctor;
        if (doctor != null) {
            populateFormForEditing(doctor);
            btnSubmit.setText("Update Doctor");
        } else {
            btnSubmit.setText("Register Doctor");
        }
    }

    /**
     * Populates the form with doctor data for editing.
     */
    private void populateFormForEditing(Doctor doctor) {
        txtFirstName.setText(doctor.getFirstName());
        txtLastName.setText(doctor.getLastName());
        txtSpecialization.setText(doctor.getSpecialization());
        txtPhone.setText(doctor.getPhone());
        txtEmail.setText(doctor.getEmail());
        txtLicense.setText(doctor.getLicenseNumber());

        // Set department selection
        loadDepartmentsAndSelect(doctor.getDepartmentId());
    }

    /**
     * Loads departments and selects the specified department.
     */
    private void loadDepartmentsAndSelect(int departmentId) {
        Task<List<Department>> task = new Task<>() {
            @Override
            protected List<Department> call() throws Exception {
                return departmentService.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            List<Department> departments = task.getValue();
            cmbDepartment.setItems(FXCollections.observableArrayList(departments));

            // Find and select the doctor's department
            for (Department dept : departments) {
                if (dept.getDepartmentId() == departmentId) {
                    cmbDepartment.setValue(dept);
                    break;
                }
            }
        });
        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load departments: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void loadDepartments() {
        Task<List<Department>> task = new Task<>() {
            @Override
            protected List<Department> call() throws Exception {
                return departmentService.getAll();
            }
        };
        task.setOnSucceeded(e -> cmbDepartment.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(
                e -> AlertUtil.showError("Error", "Failed to load departments: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    public void onSubmit() {
        String fname = txtFirstName.getText();
        String lname = txtLastName.getText();
        String spec = txtSpecialization.getText();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();
        String license = txtLicense.getText();
        Department dept = cmbDepartment.getValue();

        if (!ValidationUtil.validateRequired(fname)) {
            AlertUtil.showError("Validation", "First name required");
            return;
        }
        if (!ValidationUtil.validateRequired(lname)) {
            AlertUtil.showError("Validation", "Last name required");
            return;
        }
        if (!ValidationUtil.validateRequired(spec)) {
            AlertUtil.showError("Validation", "Specialization required");
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
        if (!ValidationUtil.validateRequired(license)) {
            AlertUtil.showError("Validation", "License number required");
            return;
        }
        if (dept == null) {
            AlertUtil.showError("Validation", "Department selection required");
            return;
        }

        if (editingDoctor != null) {
            // Update existing doctor
            handleUpdateDoctor(fname, lname, spec, phone, email, license, dept);
        } else {
            // Create new doctor
            handleCreateDoctor(fname, lname, spec, phone, email, license, dept);
        }
    }

    /**
     * Handles creating a new doctor.
     */
    private void handleCreateDoctor(String fname, String lname, String spec, String phone, String email, String license, Department dept) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                Doctor d = new Doctor();
                d.setFirstName(fname);
                d.setLastName(lname);
                d.setSpecialization(spec);
                d.setPhone(phone);
                d.setEmail(email);
                d.setLicenseNumber(license);
                d.setDepartmentId(dept.getDepartmentId());

                return doctorService.addDoctor(d);
            }
        };

        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("Success", "Registered doctor with id=" + task.getValue());
            clearForm();
            if (MainController.getInstance() != null) {
                MainController.getInstance().openDoctors();
            }
        });

        task.setOnFailed(e -> AlertUtil.showError("Error", "Registration failed: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Handles updating an existing doctor.
     */
    private void handleUpdateDoctor(String fname, String lname, String spec, String phone, String email, String license, Department dept) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Doctor d = editingDoctor;
                d.setFirstName(fname);
                d.setLastName(lname);
                d.setSpecialization(spec);
                d.setPhone(phone);
                d.setEmail(email);
                d.setLicenseNumber(license);
                d.setDepartmentId(dept.getDepartmentId());

                return doctorService.updateDoctor(d);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Success", "Doctor updated successfully");
                clearForm();
                editingDoctor = null;
                btnSubmit.setText("Register Doctor");
                if (MainController.getInstance() != null) {
                    MainController.getInstance().openDoctors();
                }
            } else {
                AlertUtil.showError("Update Failed", "Failed to update doctor");
            }
        });

        task.setOnFailed(e -> AlertUtil.showError("Error", "Update failed: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void clearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        txtSpecialization.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtLicense.clear();
        cmbDepartment.getSelectionModel().clearSelection();
        editingDoctor = null;
        btnSubmit.setText("Register Doctor");
    }
}

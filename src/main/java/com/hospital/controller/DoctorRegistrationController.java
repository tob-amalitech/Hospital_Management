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

    private final DoctorService doctorService = new DoctorService();
    private final DepartmentService departmentService = new DepartmentService();

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

    private void clearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        txtSpecialization.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtLicense.clear();
        cmbDepartment.getSelectionModel().clearSelection();
    }
}

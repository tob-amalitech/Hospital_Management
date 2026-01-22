package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.concurrent.Task;

/**
 * Controller for main application window with sidebar navigation.
 */
public class MainController {
    @FXML
    private AnchorPane contentPane;
    @FXML
    private Label lblConnectionStatus;
    @FXML
    private ToggleGroup navGroup;
    @FXML
    private ToggleButton btnDashboard;
    @FXML
    private ToggleButton btnPatients;
    @FXML
    private ToggleButton btnDoctors;
    @FXML
    private ToggleButton btnAppointments;
    @FXML
    private ToggleButton btnDepartments;
    @FXML
    private ToggleButton btnRecords;
    @FXML
    private ToggleButton btnReports;

    /**
     * Initializes the controller and loads the default dashboard view.
     */
    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    /**
     * Initializes the controller and loads the default dashboard view.
     */
    @FXML
    public void initialize() {
        instance = this;
        System.out.println("MainController initialized");
        com.hospital.util.SchemaMigrator.checkAndMigrate();
        checkDatabaseConnection();
        // Select dashboard by default and load it
        btnDashboard.setSelected(true);
        openDashboard();
    }

    private void checkDatabaseConnection() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // Simple check: try to get a connection and close it
                try (java.sql.Connection conn = DatabaseConnection.getConnection()) {
                    return conn != null && !conn.isClosed();
                }
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                lblConnectionStatus.setText("Connected to Database");
                lblConnectionStatus.setStyle("-fx-text-fill: green;");
            } else {
                lblConnectionStatus.setText("Database Connection Failed");
                lblConnectionStatus.setStyle("-fx-text-fill: red;");
            }
        });

        task.setOnFailed(e -> {
            lblConnectionStatus.setText("Connection Error: " + task.getException().getMessage());
            lblConnectionStatus.setStyle("-fx-text-fill: red;");
        });

        new Thread(task).start();
    }

    private void loadView(String resource) {
        try {
            java.net.URL url = getClass().getResource(resource);
            if (url == null) {
                com.hospital.util.AlertUtil.showError("Navigation Error", "Cannot find resource: " + resource);
                return;
            }
            Parent root = FXMLLoader.load(url);
            contentPane.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (Exception e) {
            System.err.println("Failed to load view: " + resource + " -> " + e.getMessage());
            e.printStackTrace();
            com.hospital.util.AlertUtil.showError("Navigation Error",
                    "Failed to load view " + resource + ":\n" + e.getMessage());
        }
    }

    /**
     * Exits the application.
     */
    @FXML
    public void onExit() {
        System.exit(0);
    }

    /**
     * Navigates to the Patient Registration view.
     */
    public void openPatientRegistration() {
        // Keep Patients selected as it is a sub-action
        if (btnPatients != null)
            btnPatients.setSelected(true);
        loadView("/fxml/patient-registration.fxml");
    }

    /**
     * Navigates to the Patient Registration view with a patient to edit.
     */
    public void openPatientRegistrationForEdit(com.hospital.model.Patient patient) {
        // Keep Patients selected as it is a sub-action
        if (btnPatients != null)
            btnPatients.setSelected(true);

        try {
            java.net.URL url = getClass().getResource("/fxml/patient-registration.fxml");
            if (url == null) {
                com.hospital.util.AlertUtil.showError("Navigation Error", "Cannot find patient registration resource");
                return;
            }

            // Create FXMLLoader instance to get access to the controller
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            contentPane.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            // Get the controller and set the patient for editing
            PatientRegistrationController controller = loader.getController();
            if (controller != null && patient != null) {
                controller.setEditingPatient(patient);
            }
        } catch (Exception e) {
            System.err.println("Failed to load view: /fxml/patient-registration.fxml -> " + e.getMessage());
            e.printStackTrace();
            com.hospital.util.AlertUtil.showError("Navigation Error",
                    "Failed to load patient registration:\n" + e.getMessage());
        }
    }

    /**
     * Navigates to the Patient Management view.
     */
    @FXML
    public void openPatients() {
        if (btnPatients != null)
            btnPatients.setSelected(true);
        loadView("/fxml/patient-management.fxml");
    }

    /**
     * Navigates to the Doctor Registration view.
     */
    public void openDoctorRegistration() {
        if (btnDoctors != null)
            btnDoctors.setSelected(true);
        loadView("/fxml/doctor-registration.fxml");
    }

    /**
     * Navigates to the Doctor Registration view with a doctor to edit.
     */
    public void openDoctorRegistrationForEdit(com.hospital.model.Doctor doctor) {
        // Keep Doctors selected as it is a sub-action
        if (btnDoctors != null)
            btnDoctors.setSelected(true);

        try {
            java.net.URL url = getClass().getResource("/fxml/doctor-registration.fxml");
            if (url == null) {
                com.hospital.util.AlertUtil.showError("Navigation Error", "Cannot find doctor registration resource");
                return;
            }

            // Create FXMLLoader instance to get access to the controller
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            contentPane.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            // Get the controller and set the doctor for editing
            DoctorRegistrationController controller = loader.getController();
            if (controller != null && doctor != null) {
                controller.setEditingDoctor(doctor);
            }
        } catch (Exception e) {
            System.err.println("Failed to load view: /fxml/doctor-registration.fxml -> " + e.getMessage());
            e.printStackTrace();
            com.hospital.util.AlertUtil.showError("Navigation Error",
                    "Failed to load doctor registration:\n" + e.getMessage());
        }
    }

    /**
     * Navigates to the Doctor Management view.
     */
    @FXML
    public void openDoctors() {
        if (btnDoctors != null)
            btnDoctors.setSelected(true);
        loadView("/fxml/doctor-management.fxml");
    }

    /**
     * Navigates to the Appointment Scheduling view.
     */
    @FXML
    public void openAppointments() {
        if (btnAppointments != null)
            btnAppointments.setSelected(true);
        loadView("/fxml/appointment-scheduling.fxml");
    }

    /**
     * Navigates to the Department Management view.
     */
    @FXML
    public void openDepartments() {
        if (btnDepartments != null)
            btnDepartments.setSelected(true);
        loadView("/fxml/department-management.fxml");
    }

    /**
     * Navigates to the Dashboard view.
     */
    @FXML
    public void openDashboard() {
        if (btnDashboard != null)
            btnDashboard.setSelected(true);
        loadView("/fxml/dashboard.fxml");
    }

    /**
     * Navigates to the Medical Records view.
     */
    @FXML
    public void openMedicalRecords() {
        if (btnRecords != null)
            btnRecords.setSelected(true);
        loadView("/fxml/medical-records.fxml");
    }

    /**
     * Navigates to the Reports view.
     */
    @FXML
    public void openReports() {
        // Placeholder for phase 4 feature - creating basic view if not exists or
        // showing alert
        // For now, let's try to load it, if it fails, the error handler will catch it
        if (btnReports != null)
            btnReports.setSelected(true);
        loadView("/fxml/report-view.fxml");
    }
}

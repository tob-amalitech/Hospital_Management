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
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.concurrent.Task;
import com.hospital.util.ValidationUtil;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private Button btnSchedule;
    @FXML
    private TextField txtSearch;
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
    @FXML
    private TableColumn<Appointment, String> colActions;
    @FXML
    private Label lblAppointmentCount;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblLastUpdate;

    private final AppointmentService appointmentService = new AppointmentService();
    private final DoctorService doctorService = new DoctorService();
    private final PatientService patientService = new PatientService();

    private final ObservableList<Appointment> data = FXCollections.observableArrayList();
    private final ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private FilteredList<Appointment> filteredData;

    /**
     * Initializes the controller, sets up bindings and loads initial data.
     */
    @FXML
    public void initialize() {
        System.out.println("AppointmentSchedulingController initialized");

        // Set up table
        tableAppointments.setItems(data);
        cmbDoctor.setItems(doctors);
        cmbPatient.setItems(patients);

        // Set up filtered data for search
        filteredData = new FilteredList<>(data, p -> true);
        tableAppointments.setItems(filteredData);

        // Set up table columns
        setupTableColumns();

        // Set up combo box display
        setupComboBoxes();

        // Load initial data
        loadDoctors();
        loadPatients();
        refreshTable();

        // Set initial status
        updateStatus("Ready");
    }

    /**
     * Sets up table column properties and custom cell factories.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAppointmentId()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        colTime.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        colPatient.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            getPatientName(c.getValue().getPatientId())));
        colDoctor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            getDoctorName(c.getValue().getDoctorId())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom cell factory for status column with color coding
        colStatus.setCellFactory(new Callback<TableColumn<Appointment, String>, TableCell<Appointment, String>>() {
            @Override
            public TableCell<Appointment, String> call(TableColumn<Appointment, String> param) {
                return new TableCell<Appointment, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);
                        if (empty || status == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(status);
                            switch (status.toLowerCase()) {
                                case "scheduled":
                                    setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;");
                                    break;
                                case "completed":
                                    setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                                    break;
                                case "cancelled":
                                    setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                                    break;
                                default:
                                    setStyle("");
                                    break;
                            }
                        }
                    }
                };
            }
        });

        // Custom cell factory for actions column with buttons
        colActions.setCellFactory(new Callback<TableColumn<Appointment, String>, TableCell<Appointment, String>>() {
            @Override
            public TableCell<Appointment, String> call(TableColumn<Appointment, String> param) {
                return new ActionButtonCell(AppointmentSchedulingController.this);
            }
        });
    }

    /**
     * Sets up combo box display formatting.
     */
    private void setupComboBoxes() {
        cmbDoctor.setConverter(new javafx.util.StringConverter<Doctor>() {
            @Override
            public String toString(Doctor doctor) {
                return doctor == null ? "" : doctor.getFirstName() + " " + doctor.getLastName() +
                       " (" + doctor.getSpecialization() + ")";
            }

            @Override
            public Doctor fromString(String string) {
                return null; // Not used
            }
        });

        cmbPatient.setConverter(new javafx.util.StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getFirstName() + " " + patient.getLastName() +
                       " (ID: " + patient.getPatientId() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null; // Not used
            }
        });
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

        updateStatus("Loading appointments...");

        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() throws Exception {
                return appointmentService.getByDate(date);
            }
        };
        task.setOnSucceeded(e -> {
            data.setAll(task.getValue());
            updateAppointmentCount();
            updateLastUpdateTime();
            updateStatus("Ready");
        });
        task.setOnFailed(e -> {
            AlertUtil.showError("Error", "Failed to load appointments: " + task.getException().getMessage());
            updateStatus("Error loading appointments");
        });
        new Thread(task).start();
    }

    /**
     * Updates the appointment count label.
     */
    private void updateAppointmentCount() {
        int count = filteredData.size();
        lblAppointmentCount.setText("(" + count + " appointment" + (count != 1 ? "s" : "") + ")");
    }

    /**
     * Updates the last update time label.
     */
    private void updateLastUpdateTime() {
        String time = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lblLastUpdate.setText(time);
    }

    /**
     * Updates the status label.
     */
    private void updateStatus(String status) {
        lblStatus.setText(status);
    }

    /**
     * Gets patient name by ID.
     */
    private String getPatientName(int patientId) {
        for (Patient patient : patients) {
            if (patient.getPatientId() == patientId) {
                return patient.getFirstName() + " " + patient.getLastName();
            }
        }
        return "Patient #" + patientId;
    }

    /**
     * Gets doctor name by ID.
     */
    private String getDoctorName(int doctorId) {
        for (Doctor doctor : doctors) {
            if (doctor.getDoctorId() == doctorId) {
                return "Dr. " + doctor.getFirstName() + " " + doctor.getLastName();
            }
        }
        return "Doctor #" + doctorId;
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

    /**
     * Handles the refresh button click.
     */
    @FXML
    public void onRefresh() {
        refreshTable();
    }

    /**
     * Handles the search button click.
     */
    @FXML
    public void onSearch() {
        String searchText = txtSearch.getText().toLowerCase();
        if (searchText == null || searchText.isEmpty()) {
            filteredData.setPredicate(p -> true);
        } else {
            filteredData.setPredicate(appointment -> {
                String patientName = getPatientName(appointment.getPatientId()).toLowerCase();
                String doctorName = getDoctorName(appointment.getDoctorId()).toLowerCase();
                String status = appointment.getStatus().toLowerCase();

                return patientName.contains(searchText) ||
                       doctorName.contains(searchText) ||
                       status.contains(searchText) ||
                       appointment.getAppointmentDate().toString().contains(searchText);
            });
        }
        updateAppointmentCount();
    }

    /**
     * Handles the edit button click.
     */
    @FXML
    public void onEdit() {
        Appointment selected = tableAppointments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Edit", "No appointment selected");
            return;
        }

        // Create edit dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Appointment");
        dialog.setHeaderText("Edit appointment #" + selected.getAppointmentId());

        // Create form fields
        DatePicker editDatePicker = new DatePicker(selected.getAppointmentDate());
        TextField editTimeField = new TextField(selected.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        ComboBox<Doctor> editDoctorCombo = new ComboBox<>();
        ComboBox<Patient> editPatientCombo = new ComboBox<>();

        // Setup combo boxes
        editDoctorCombo.setItems(doctors);
        editPatientCombo.setItems(patients);

        // Set current values
        editDoctorCombo.setValue(doctors.stream()
            .filter(d -> d.getDoctorId() == selected.getDoctorId())
            .findFirst().orElse(null));
        editPatientCombo.setValue(patients.stream()
            .filter(p -> p.getPatientId() == selected.getPatientId())
            .findFirst().orElse(null));

        // Setup combo box display
        editDoctorCombo.setConverter(new javafx.util.StringConverter<Doctor>() {
            @Override
            public String toString(Doctor doctor) {
                return doctor == null ? "" : doctor.getFirstName() + " " + doctor.getLastName() +
                       " (" + doctor.getSpecialization() + ")";
            }
            @Override
            public Doctor fromString(String string) { return null; }
        });

        editPatientCombo.setConverter(new javafx.util.StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getFirstName() + " " + patient.getLastName() +
                       " (ID: " + patient.getPatientId() + ")";
            }
            @Override
            public Patient fromString(String string) { return null; }
        });

        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Date:"), 0, 0);
        grid.add(editDatePicker, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(editTimeField, 1, 1);
        grid.add(new Label("Doctor:"), 0, 2);
        grid.add(editDoctorCombo, 1, 2);
        grid.add(new Label("Patient:"), 0, 3);
        grid.add(editPatientCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Handle OK button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Validate input
                if (editDatePicker.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select a date");
                    return null;
                }
                if (editTimeField.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validation Error", "Please enter a time");
                    return null;
                }
                if (editDoctorCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select a doctor");
                    return null;
                }
                if (editPatientCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select a patient");
                    return null;
                }

                // Update appointment
                try {
                    LocalTime time = LocalTime.parse(editTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    selected.setAppointmentDate(editDatePicker.getValue());
                    selected.setAppointmentTime(time);
                    selected.setDoctorId(editDoctorCombo.getValue().getDoctorId());
                    selected.setPatientId(editPatientCombo.getValue().getPatientId());

                    updateAppointment(selected);
                } catch (Exception e) {
                    AlertUtil.showError("Error", "Invalid time format. Use HH:MM format.");
                    return null;
                }
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    /**
     * Updates an existing appointment.
     */
    private void updateAppointment(Appointment appointment) {
        updateStatus("Updating appointment...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return appointmentService.updateAppointment(appointment);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Success", "Appointment updated successfully");
                refreshTable();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update appointment");
            }
            updateStatus("Ready");
        });

        task.setOnFailed(e -> {
            AlertUtil.showError("Update Error", task.getException().getMessage());
            updateStatus("Update failed");
        });

        new Thread(task).start();
    }

    /**
     * Handles the complete button click.
     */
    @FXML
    public void onComplete() {
        Appointment selected = tableAppointments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Complete", "No appointment selected");
            return;
        }

        if (!"Scheduled".equals(selected.getStatus())) {
            AlertUtil.showError("Complete", "Only scheduled appointments can be marked as completed");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Complete",
            "Mark appointment #" + selected.getAppointmentId() + " as completed?");
        if (!confirm) return;

        updateAppointmentStatus(selected, "Completed");
    }

    /**
     * Handles the cancel button click.
     */
    @FXML
    public void onCancel() {
        Appointment selected = tableAppointments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Cancel", "No appointment selected");
            return;
        }

        if (!"Scheduled".equals(selected.getStatus())) {
            AlertUtil.showError("Cancel", "Only scheduled appointments can be cancelled");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Cancel",
            "Cancel appointment #" + selected.getAppointmentId() + "?");
        if (!confirm) return;

        updateAppointmentStatus(selected, "Cancelled");
    }

    /**
     * Handles the delete button click.
     */
    @FXML
    public void onDelete() {
        Appointment selected = tableAppointments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Delete", "No appointment selected");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Delete",
            "Permanently delete appointment #" + selected.getAppointmentId() + "? This action cannot be undone.");
        if (!confirm) return;

        updateStatus("Deleting appointment...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return appointmentService.deleteAppointment(selected.getAppointmentId());
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Deleted", "Appointment deleted successfully");
                refreshTable();
            } else {
                AlertUtil.showError("Delete Failed", "Failed to delete appointment");
            }
            updateStatus("Ready");
        });

        task.setOnFailed(e -> {
            AlertUtil.showError("Delete Error", task.getException().getMessage());
            updateStatus("Delete failed");
        });

        new Thread(task).start();
    }

    /**
     * Handles the view statistics button click.
     */
    @FXML
    public void onViewStats() {
        // Calculate statistics
        long scheduled = data.stream().filter(a -> "Scheduled".equals(a.getStatus())).count();
        long completed = data.stream().filter(a -> "Completed".equals(a.getStatus())).count();
        long cancelled = data.stream().filter(a -> "Cancelled".equals(a.getStatus())).count();

        String stats = String.format(
            "Appointment Statistics for %s:\n\n" +
            "📅 Scheduled: %d\n" +
            "✅ Completed: %d\n" +
            "❌ Cancelled: %d\n" +
            "📊 Total: %d",
            dpDate.getValue() != null ? dpDate.getValue().toString() : "today",
            scheduled, completed, cancelled, data.size()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Statistics");
        alert.setHeaderText("Daily Summary");
        alert.setContentText(stats);
        alert.showAndWait();
    }

    /**
     * Custom table cell with action buttons for each appointment row.
     */
    public static class ActionButtonCell extends TableCell<Appointment, String> {
        private final Button btnComplete = new Button("✅");
        private final Button btnCancel = new Button("❌");
        private final HBox container = new HBox(5);

        public ActionButtonCell(AppointmentSchedulingController controller) {
            btnComplete.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 5;");
            btnCancel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 5;");

            btnComplete.setOnAction(e -> {
                Appointment appointment = getTableView().getItems().get(getIndex());
                controller.handleCompleteAction(appointment);
            });

            btnCancel.setOnAction(e -> {
                Appointment appointment = getTableView().getItems().get(getIndex());
                controller.handleCancelAction(appointment);
            });

            container.getChildren().addAll(btnComplete, btnCancel);
            container.setAlignment(javafx.geometry.Pos.CENTER);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                Appointment appointment = getTableView().getItems().get(getIndex());
                String status = appointment.getStatus();

                // Only show buttons for scheduled appointments
                if ("Scheduled".equals(status)) {
                    setGraphic(container);
                } else {
                    setGraphic(null);
                }
            }
        }
    }

    /**
     * Handles the complete action from the table cell button.
     */
    public void handleCompleteAction(Appointment appointment) {
        boolean confirm = AlertUtil.confirm("Confirm Complete",
            "Mark appointment #" + appointment.getAppointmentId() + " as completed?");
        if (confirm) {
            updateAppointmentStatus(appointment, "Completed");
        }
    }

    /**
     * Handles the cancel action from the table cell button.
     */
    public void handleCancelAction(Appointment appointment) {
        boolean confirm = AlertUtil.confirm("Confirm Cancel",
            "Cancel appointment #" + appointment.getAppointmentId() + "?");
        if (confirm) {
            updateAppointmentStatus(appointment, "Cancelled");
        }
    }

    /**
     * Updates the status of an appointment.
     */
    private void updateAppointmentStatus(Appointment appointment, String newStatus) {
        updateStatus("Updating appointment...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return appointmentService.updateStatus(appointment.getAppointmentId(), newStatus);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Success", "Appointment status updated to: " + newStatus);
                refreshTable();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update appointment status");
            }
            updateStatus("Ready");
        });

        task.setOnFailed(e -> {
            AlertUtil.showError("Update Error", task.getException().getMessage());
            updateStatus("Update failed");
        });

        new Thread(task).start();
    }
}

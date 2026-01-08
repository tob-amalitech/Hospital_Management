package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;

import java.util.List;

public class PatientManagementController {
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Patient> tablePatients;
    @FXML
    private TableColumn<Patient, Integer> colId;
    @FXML
    private TableColumn<Patient, String> colFirst;
    @FXML
    private TableColumn<Patient, String> colLast;
    @FXML
    private TableColumn<Patient, String> colPhone;
    @FXML
    private TableColumn<Patient, String> colEmail;

    private final PatientService service = new PatientService();
    private final ObservableList<Patient> data = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        System.out.println("PatientManagementController initialized");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getPatientId()));
        colFirst.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFirstName()));
        colLast.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLastName()));
        colPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        tablePatients.setItems(data);
        refreshTable();
    }

    /**
     * Refreshes the patient table in a background thread.
     */
    private void refreshTable() {
        Task<List<Patient>> task = new Task<>() {
            @Override
            protected List<Patient> call() throws Exception {
                return service.getAllPatients();
            }
        };
        task.setOnSucceeded(e -> data.setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError("Load Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Searches for patients in a background thread.
     */
    @FXML
    public void onSearch() {
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            refreshTable();
            return;
        }

        Task<List<Patient>> task = new Task<>() {
            @Override
            protected List<Patient> call() throws Exception {
                return service.searchPatients(q);
            }
        };
        task.setOnSucceeded(e -> data.setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError("Search Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    public void onAdd() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().openPatientRegistration();
        }
    }

    @FXML
    public void onEdit() {
        AlertUtil.showInfo("Not Implemented", "Edit function not implemented in this scaffold.");
    }

    /**
     * Deletes a selected patient in a background thread.
     */
    @FXML
    public void onDelete() {
        Patient sel = tablePatients.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showError("Delete", "No patient selected");
            return;
        }
        boolean ok = AlertUtil.confirm("Confirm Delete", "Delete patient id=" + sel.getPatientId() + "?");
        if (!ok)
            return;

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return service.deletePatient(sel.getPatientId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Deleted", "Patient deleted");
                refreshTable();
            } else {
                AlertUtil.showError("Delete", "Failed to delete patient");
            }
        });
        task.setOnFailed(e -> AlertUtil.showError("Delete Error", task.getException().getMessage()));
        new Thread(task).start();
    }
}

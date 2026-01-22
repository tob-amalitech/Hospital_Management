package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import com.hospital.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;

import java.util.List;

public class DoctorManagementController {
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Doctor> tableDoctors;
    @FXML
    private TableColumn<Doctor, Integer> colId;
    @FXML
    private TableColumn<Doctor, String> colFirst;
    @FXML
    private TableColumn<Doctor, String> colLast;
    @FXML
    private TableColumn<Doctor, String> colSpec;
    @FXML
    private TableColumn<Doctor, String> colEmail;

    private final DoctorService service = new DoctorService();
    private final ObservableList<Doctor> data = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        System.out.println("DoctorManagementController initialized");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDoctorId()));
        colFirst.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFirstName()));
        colLast.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLastName()));
        colSpec.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSpecialization()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        tableDoctors.setItems(data);
        refreshTable();
    }

    /**
     * Refreshes the doctor list in a background thread.
     */
    private void refreshTable() {
        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() throws Exception {
                return service.getAll();
            }
        };
        task.setOnSucceeded(e -> data.setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError("Load Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Searches for doctors in a background thread.
     */
    @FXML
    public void onSearch() {
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            refreshTable();
            return;
        }

        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() throws Exception {
                return service.search(q);
            }
        };
        task.setOnSucceeded(e -> data.setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError("Search Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    public void onAdd() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().openDoctorRegistration();
        }
    }

    @FXML
    public void onEdit() {
        Doctor selectedDoctor = tableDoctors.getSelectionModel().getSelectedItem();
        if (selectedDoctor == null) {
            AlertUtil.showError("Edit", "No doctor selected");
            return;
        }

        if (MainController.getInstance() != null) {
            MainController.getInstance().openDoctorRegistrationForEdit(selectedDoctor);
        }
    }

    /**
     * Deletes a selected doctor in a background thread.
     */
    @FXML
    public void onDelete() {
        Doctor sel = tableDoctors.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showError("Delete", "No doctor selected");
            return;
        }
        boolean ok = AlertUtil.confirm("Confirm Delete", "Delete doctor id=" + sel.getDoctorId() + "?");
        if (!ok)
            return;

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return service.deleteDoctor(sel.getDoctorId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Deleted", "Doctor deleted");
                refreshTable();
            } else {
                AlertUtil.showError("Delete", "Failed to delete doctor");
            }
        });
        task.setOnFailed(e -> AlertUtil.showError("Delete Error", task.getException().getMessage()));
        new Thread(task).start();
    }
}

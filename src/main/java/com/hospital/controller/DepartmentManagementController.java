package com.hospital.controller;

import com.hospital.model.Department;
import com.hospital.service.DepartmentService;
import com.hospital.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;
import com.hospital.util.ValidationUtil;

import java.util.List;

public class DepartmentManagementController {
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLocation;
    @FXML
    private ListView<Department> listDepartments;

    private final DepartmentService service = new DepartmentService();
    private final ObservableList<Department> data = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        listDepartments.setItems(data);
        refreshList();
    }

    /**
     * Refreshes the department list from the database in a background thread.
     */
    private void refreshList() {
        Task<List<Department>> task = new Task<>() {
            @Override
            protected List<Department> call() throws Exception {
                return service.getAll();
            }
        };
        task.setOnSucceeded(e -> data.setAll(task.getValue()));
        task.setOnFailed(e -> AlertUtil.showError("Load Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Handles adding a new department.
     */
    @FXML
    public void onAdd() {
        String name = txtName.getText();
        String loc = txtLocation.getText();

        if (!ValidationUtil.validateRequired(name)) {
            AlertUtil.showError("Validation", "Name is required");
            return;
        }
        if (!ValidationUtil.validateRequired(loc)) {
            AlertUtil.showError("Validation", "Location is required");
            return;
        }

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                Department d = new Department();
                d.setDepartmentName(name);
                d.setLocation(loc);
                return service.createDepartment(d);
            }
        };
        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("Added", "Department id=" + task.getValue());
            refreshList();
        });
        task.setOnFailed(e -> AlertUtil.showError("Add Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Handles editing a department (Placeholder).
     */
    @FXML
    public void onEdit() {
        AlertUtil.showInfo("Not Implemented", "Edit department not implemented.");
    }

    /**
     * Handles deleting a selected department.
     */
    @FXML
    public void onDelete() {
        Department sel = listDepartments.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showError("Delete", "No department selected");
            return;
        }
        boolean ok = AlertUtil.confirm("Confirm Delete", "Delete department id=" + sel.getDepartmentId() + "?");
        if (!ok)
            return;

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return service.deleteDepartment(sel.getDepartmentId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Deleted", "Department deleted");
                refreshList();
            } else {
                AlertUtil.showError("Delete", "Failed to delete department");
            }
        });
        task.setOnFailed(e -> AlertUtil.showError("Delete Error", task.getException().getMessage()));
        new Thread(task).start();
    }
}

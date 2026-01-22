package com.hospital.controller;

import com.hospital.model.Department;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
import com.hospital.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import com.hospital.util.ValidationUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DepartmentManagementController {
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLocation;
    @FXML
    private Button btnAdd;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Department> tableDepartments;
    @FXML
    private TableColumn<Department, Integer> colId;
    @FXML
    private TableColumn<Department, String> colName;
    @FXML
    private TableColumn<Department, String> colLocation;
    @FXML
    private TableColumn<Department, Integer> colStaffCount;
    @FXML
    private TableColumn<Department, String> colActions;
    @FXML
    private Label lblDepartmentCount;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblLastUpdate;

    private final DepartmentService departmentService = new DepartmentService();
    private final DoctorService doctorService = new DoctorService();
    private final ObservableList<Department> data = FXCollections.observableArrayList();
    private FilteredList<Department> filteredData;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        filteredData = new FilteredList<>(data, p -> true);
        tableDepartments.setItems(filteredData);
        refreshTable();
    }

    /**
     * Sets up table column properties and custom cell factories.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("departmentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStaffCount.setCellValueFactory(c -> {
            Department dept = c.getValue();
            int count = getStaffCount(dept.getDepartmentId());
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });

        // Custom cell factory for actions column with buttons
        colActions.setCellFactory(new Callback<TableColumn<Department, String>, TableCell<Department, String>>() {
            @Override
            public TableCell<Department, String> call(TableColumn<Department, String> param) {
                return new ActionButtonCell(DepartmentManagementController.this);
            }
        });
    }

    /**
     * Gets the staff count for a department.
     */
    private int getStaffCount(int departmentId) {
        try {
            return doctorService.getByDepartment(departmentId).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Refreshes the department table from the database in a background thread.
     */
    private void refreshTable() {
        updateStatus("Loading departments...");

        Task<List<Department>> task = new Task<>() {
            @Override
            protected List<Department> call() throws Exception {
                return departmentService.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            data.setAll(task.getValue());
            updateDepartmentCount();
            updateLastUpdate();
            updateStatus("Ready");
        });

        task.setOnFailed(e -> {
            AlertUtil.showError("Load Error", task.getException().getMessage());
            updateStatus("Load failed");
        });

        new Thread(task).start();
    }

    /**
     * Handles adding a new department.
     */
    @FXML
    public void onAdd() {
        String name = txtName.getText().trim();
        String location = txtLocation.getText().trim();

        if (!ValidationUtil.validateRequired(name)) {
            AlertUtil.showError("Validation", "Department name is required");
            return;
        }
        if (!ValidationUtil.validateRequired(location)) {
            AlertUtil.showError("Validation", "Location is required");
            return;
        }

        updateStatus("Adding department...");

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                Department department = new Department();
                department.setDepartmentName(name);
                department.setLocation(location);
                return departmentService.createDepartment(department);
            }
        };

        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("Success", "Department added successfully with ID: " + task.getValue());
            txtName.clear();
            txtLocation.clear();
            refreshTable();
        });

        task.setOnFailed(e -> {
            AlertUtil.showError("Add Error", task.getException().getMessage());
            updateStatus("Add failed");
        });

        new Thread(task).start();
    }

    /**
     * Handles editing a selected department.
     */
    @FXML
    public void onEdit() {
        Department selected = tableDepartments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Edit", "No department selected");
            return;
        }

        // Create edit dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Department");
        dialog.setHeaderText("Edit department #" + selected.getDepartmentId());

        // Create form fields
        TextField editNameField = new TextField(selected.getDepartmentName());
        TextField editLocationField = new TextField(selected.getLocation());

        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Department Name:"), 0, 0);
        grid.add(editNameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(editLocationField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Handle OK button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Validate input
                String newName = editNameField.getText().trim();
                String newLocation = editLocationField.getText().trim();

                if (!ValidationUtil.validateRequired(newName)) {
                    AlertUtil.showError("Validation Error", "Department name is required");
                    return null;
                }
                if (!ValidationUtil.validateRequired(newLocation)) {
                    AlertUtil.showError("Validation Error", "Location is required");
                    return null;
                }

                // Update department
                selected.setDepartmentName(newName);
                selected.setLocation(newLocation);
                updateDepartment(selected);
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    /**
     * Handles deleting a selected department.
     */
    @FXML
    public void onDelete() {
        Department selected = tableDepartments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Delete", "No department selected");
            return;
        }

        int staffCount = getStaffCount(selected.getDepartmentId());
        if (staffCount > 0) {
            AlertUtil.showError("Cannot Delete", "Cannot delete department with " + staffCount + " staff members. Please reassign staff first.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Delete",
            "Delete department '" + selected.getDepartmentName() + "'?\nThis action cannot be undone.");
        if (!confirm) return;

        updateStatus("Deleting department...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return departmentService.deleteDepartment(selected.getDepartmentId());
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Success", "Department deleted successfully");
                refreshTable();
            } else {
                AlertUtil.showError("Delete Failed", "Failed to delete department");
                updateStatus("Delete failed");
            }
        });

        task.setOnFailed(e -> {
            AlertUtil.showError("Delete Error", task.getException().getMessage());
            updateStatus("Delete failed");
        });

        new Thread(task).start();
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    public void onRefresh() {
        refreshTable();
    }

    /**
     * Handles the search functionality.
     */
    @FXML
    public void onSearch() {
        String searchText = txtSearch.getText().toLowerCase();
        filteredData.setPredicate(department -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            return department.getDepartmentName().toLowerCase().contains(searchText) ||
                   department.getLocation().toLowerCase().contains(searchText) ||
                   String.valueOf(department.getDepartmentId()).contains(searchText);
        });
        updateDepartmentCount();
    }

    /**
     * Handles the view statistics button click.
     */
    @FXML
    public void onViewStats() {
        // Calculate statistics
        int totalDepartments = data.size();
        int totalStaff = data.stream().mapToInt(dept -> getStaffCount(dept.getDepartmentId())).sum();
        double avgStaffPerDept = totalDepartments > 0 ? (double) totalStaff / totalDepartments : 0;

        String stats = String.format(
            "Department Statistics:\n\n" +
            "🏥 Total Departments: %d\n" +
            "👥 Total Staff: %d\n" +
            "📊 Average Staff per Department: %.1f\n" +
            "📍 Locations: %d unique locations",
            totalDepartments, totalStaff, avgStaffPerDept,
            data.stream().map(Department::getLocation).distinct().count()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Department Statistics");
        alert.setHeaderText("Hospital Overview");
        alert.setContentText(stats);
        alert.showAndWait();
    }

    /**
     * Updates an existing department.
     */
    private void updateDepartment(Department department) {
        updateStatus("Updating department...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return departmentService.updateDepartment(department);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                AlertUtil.showInfo("Success", "Department updated successfully");
                refreshTable();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update department");
                updateStatus("Update failed");
            }
        });

        task.setOnFailed(e -> {
            AlertUtil.showError("Update Error", task.getException().getMessage());
            updateStatus("Update failed");
        });

        new Thread(task).start();
    }

    /**
     * Updates the department count label.
     */
    private void updateDepartmentCount() {
        int visibleCount = filteredData.size();
        int totalCount = data.size();
        if (visibleCount == totalCount) {
            lblDepartmentCount.setText("(" + totalCount + " departments)");
        } else {
            lblDepartmentCount.setText("(" + visibleCount + " of " + totalCount + " departments)");
        }
    }

    /**
     * Updates the status label.
     */
    private void updateStatus(String status) {
        lblStatus.setText(status);
        lblStatus.setStyle(status.equals("Ready") ?
            "-fx-text-fill: #27ae60;" : "-fx-text-fill: #f39c12;");
    }

    /**
     * Updates the last update timestamp.
     */
    private void updateLastUpdate() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lblLastUpdate.setText(timestamp);
    }

    /**
     * Custom table cell with action buttons for each department row.
     */
    public static class ActionButtonCell extends TableCell<Department, String> {
        private final Button btnEdit = new Button("✏️");
        private final Button btnDelete = new Button("🗑️");
        private final HBox container = new HBox(5);

        public ActionButtonCell(DepartmentManagementController controller) {
            btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 5;");
            btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 5;");

            btnEdit.setOnAction(e -> {
                Department department = getTableView().getItems().get(getIndex());
                controller.handleEditAction(department);
            });

            btnDelete.setOnAction(e -> {
                Department department = getTableView().getItems().get(getIndex());
                controller.handleDeleteAction(department);
            });

            container.getChildren().addAll(btnEdit, btnDelete);
            container.setAlignment(javafx.geometry.Pos.CENTER);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(container);
            }
        }
    }

    /**
     * Handles the edit action from the table cell button.
     */
    public void handleEditAction(Department department) {
        // Set the selected item and call edit
        tableDepartments.getSelectionModel().select(department);
        onEdit();
    }

    /**
     * Handles the delete action from the table cell button.
     */
    public void handleDeleteAction(Department department) {
        // Set the selected item and call delete
        tableDepartments.getSelectionModel().select(department);
        onDelete();
    }
}

package com.hospital.controller;

import com.hospital.util.PerformanceMonitor;
import com.hospital.util.PerformanceTestRunner;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for Report view - Performance Analytics.
 */
public class ReportController {

    @FXML private TableView<PerformanceData> performanceTable;
    @FXML private TableColumn<PerformanceData, String> queryColumn;
    @FXML private TableColumn<PerformanceData, Double> preOptColumn;
    @FXML private TableColumn<PerformanceData, Double> postOptColumn;
    @FXML private TableColumn<PerformanceData, Double> improvementColumn;
    @FXML private TableColumn<PerformanceData, Integer> samplesColumn;

    @FXML private Label cacheStatsLabel;
    @FXML private Button generateReportButton;
    @FXML private Button clearDataButton;
    @FXML private Button runTestsButton;
    @FXML private TextArea methodologyTextArea;

    private ObservableList<PerformanceData> performanceData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupMethodologyText();
        updateCacheStats();
        System.out.println("ReportController initialized");
    }

    private void setupTable() {
        queryColumn.setCellValueFactory(new PropertyValueFactory<>("queryName"));
        preOptColumn.setCellValueFactory(new PropertyValueFactory<>("preOptimizationAvg"));
        postOptColumn.setCellValueFactory(new PropertyValueFactory<>("postOptimizationAvg"));
        improvementColumn.setCellValueFactory(new PropertyValueFactory<>("improvementPercentage"));
        samplesColumn.setCellValueFactory(new PropertyValueFactory<>("sampleSize"));

        // Format numeric columns
        preOptColumn.setCellFactory(column -> new TableCell<PerformanceData, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ms", item));
                }
            }
        });

        postOptColumn.setCellFactory(column -> new TableCell<PerformanceData, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ms", item));
                }
            }
        });

        improvementColumn.setCellFactory(column -> new TableCell<PerformanceData, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        performanceTable.setItems(performanceData);
    }

    private void setupMethodologyText() {
        methodologyTextArea.setText(
            "PERFORMANCE OPTIMIZATION METHODOLOGY\n\n" +
            "1. BASELINE MEASUREMENT:\n" +
            "   - All database queries were initially executed without caching\n" +
            "   - Execution times recorded for multiple runs to establish baseline\n\n" +
            "2. OPTIMIZATION IMPLEMENTED:\n" +
            "   - Database indexes on frequently queried columns (name, email, dates)\n" +
            "   - In-memory caching with 15-minute TTL for search results\n" +
            "   - Connection pooling with HikariCP\n\n" +
            "3. POST-OPTIMIZATION MEASUREMENT:\n" +
            "   - Same queries executed with optimizations enabled\n" +
            "   - Cache hits measured separately from database queries\n\n" +
            "4. PERFORMANCE METRICS:\n" +
            "   - Query execution time (milliseconds)\n" +
            "   - Cache hit rate\n" +
            "   - Percentage improvement calculation\n\n" +
            "5. INDEXES IMPLEMENTED:\n" +
            "   - idx_patient_name (last_name, first_name)\n" +
            "   - idx_patient_email (email)\n" +
            "   - idx_appointment_date (appointment_date)\n" +
            "   - idx_appointment_doctor (doctor_id)\n" +
            "   - Additional indexes on foreign keys and search columns"
        );
        methodologyTextArea.setEditable(false);
    }

    @FXML
    private void generateReport() {
        performanceData.clear();

        // Get unique query names from performance records
        List<String> queryNames = PerformanceMonitor.getAllRecords().stream()
                .map(record -> record.getQueryName())
                .distinct()
                .toList();

        for (String queryName : queryNames) {
            PerformanceMonitor.PerformanceComparison comparison =
                PerformanceMonitor.getPerformanceComparison(queryName);

            if (comparison.getPreOptSampleSize() > 0 || comparison.getPostOptSampleSize() > 0) {
                performanceData.add(new PerformanceData(
                    comparison.getQueryName(),
                    comparison.getPreOptimizationAvg(),
                    comparison.getPostOptimizationAvg(),
                    comparison.getImprovementPercentage(),
                    Math.max(comparison.getPreOptSampleSize(), comparison.getPostOptSampleSize())
                ));
            }
        }

        updateCacheStats();
    }

    @FXML
    private void runPerformanceTests() {
        runTestsButton.setDisable(true);
        runTestsButton.setText("Running Tests...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                PerformanceTestRunner.runPerformanceTests();
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            runTestsButton.setDisable(false);
            runTestsButton.setText("Run Performance Tests");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Performance Tests Completed");
            alert.setHeaderText(null);
            alert.setContentText("Performance tests have been completed. Click 'Generate Performance Report' to view the results.");
            alert.showAndWait();

            // Auto-generate report after tests
            generateReport();
        });

        task.setOnFailed(e -> {
            runTestsButton.setDisable(false);
            runTestsButton.setText("Run Performance Tests");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Test Error");
            alert.setHeaderText("Performance tests failed");
            alert.setContentText("Error: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    @FXML
    private void clearPerformanceData() {
        PerformanceMonitor.clearRecords();
        performanceData.clear();
        updateCacheStats();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Data Cleared");
        alert.setHeaderText(null);
        alert.setContentText("All performance monitoring data has been cleared.");
        alert.showAndWait();
    }

    private void updateCacheStats() {
        // Note: This is a simplified cache stats display
        // In a real implementation, you'd aggregate stats from all caches
        String stats = "Cache Status: Performance monitoring active";
        cacheStatsLabel.setText(stats);
    }

    // Data model for the table
    public static class PerformanceData {
        private final String queryName;
        private final double preOptimizationAvg;
        private final double postOptimizationAvg;
        private final double improvementPercentage;
        private final int sampleSize;

        public PerformanceData(String queryName, double preOpt, double postOpt,
                             double improvement, int samples) {
            this.queryName = queryName;
            this.preOptimizationAvg = preOpt;
            this.postOptimizationAvg = postOpt;
            this.improvementPercentage = improvement;
            this.sampleSize = samples;
        }

        // Getters for JavaFX
        public String getQueryName() { return queryName; }
        public double getPreOptimizationAvg() { return preOptimizationAvg; }
        public double getPostOptimizationAvg() { return postOptimizationAvg; }
        public double getImprovementPercentage() { return improvementPercentage; }
        public int getSampleSize() { return sampleSize; }
    }
}

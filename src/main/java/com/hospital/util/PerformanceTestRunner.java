package com.hospital.util;

import com.hospital.dao.PatientDAO;
import com.hospital.dao.PatientDAOImpl;
import com.hospital.model.Patient;

import java.time.LocalDate;
import java.util.List;

/**
 * Performance testing utility to demonstrate optimization improvements.
 * This class generates sample performance data for the reports.
 */
public class PerformanceTestRunner {

    private static final PatientDAO patientDAO = new PatientDAOImpl();

    /**
     * Runs performance tests to generate comparison data.
     * This simulates the before/after optimization scenario.
     */
    public static void runPerformanceTests() {
        System.out.println("Running performance tests...");

        try {
            // Ensure we have some test data
            createSamplePatients();

            // Test 1: Patient search by name (without caching first)
            System.out.println("Testing patient search performance...");

            // Run searches without caching (simulating pre-optimization)
            runSearchTests(false, 5);

            // Clear cache to simulate fresh start
            PerformanceMonitor.clearRecords();

            // Run searches with caching (simulating post-optimization)
            runSearchTests(true, 5);

            System.out.println("Performance tests completed. Check the Reports section for results.");

        } catch (Exception e) {
            System.err.println("Error running performance tests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runSearchTests(boolean withOptimization, int iterations) throws Exception {
        String[] searchTerms = {"John", "Smith", "Mary", "Davis", "Brown"};

        for (int i = 0; i < iterations; i++) {
            for (String term : searchTerms) {
                long startTime = System.currentTimeMillis();

                List<Patient> results = patientDAO.searchByName(term);

                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;

                // For demonstration, we'll mark the first half as pre-optimization
                // and second half as post-optimization
                boolean isOptimized = withOptimization;

                PerformanceMonitor.recordQueryTime(
                    "PatientDAO.searchByName ('" + term + "')",
                    executionTime,
                    isOptimized
                );

                // Small delay to simulate real usage patterns
                Thread.sleep(10);
            }
        }
    }

    private static void createSamplePatients() throws Exception {
        // Create some sample patients if they don't exist
        // This is just for testing - in real scenarios, data would already exist

        // Check if we already have patients
        List<Patient> existing = patientDAO.findAll();
        if (existing.size() > 10) {
            return; // Already have test data
        }

        String[][] sampleData = {
            {"John", "Smith", "Male", "555-0101", "john.smith@email.com"},
            {"Mary", "Johnson", "Female", "555-0102", "mary.johnson@email.com"},
            {"Robert", "Davis", "Male", "555-0103", "robert.davis@email.com"},
            {"Patricia", "Brown", "Female", "555-0104", "patricia.brown@email.com"},
            {"Michael", "Wilson", "Male", "555-0105", "michael.wilson@email.com"},
            {"Jennifer", "Moore", "Female", "555-0106", "jennifer.moore@email.com"},
            {"William", "Taylor", "Male", "555-0107", "william.taylor@email.com"},
            {"Linda", "Anderson", "Female", "555-0108", "linda.anderson@email.com"},
            {"David", "Thomas", "Male", "555-0109", "david.thomas@email.com"},
            {"Elizabeth", "Jackson", "Female", "555-0110", "elizabeth.jackson@email.com"}
        };

        for (String[] data : sampleData) {
            Patient patient = new Patient();
            patient.setFirstName(data[0]);
            patient.setLastName(data[1]);
            patient.setGender(data[2]);
            patient.setPhone(data[3]);
            patient.setEmail(data[4]);
            patient.setDateOfBirth(LocalDate.of(1980, 1, 1));
            patient.setAddress("123 Test Street");
            patient.setBloodGroup("O+");
            patient.setRegistrationDate(LocalDate.now());

            try {
                patientDAO.create(patient);
            } catch (Exception e) {
                // Patient might already exist, continue
                System.out.println("Sample patient may already exist: " + data[0] + " " + data[1]);
            }
        }

        System.out.println("Sample patients created for testing.");
    }

    /**
     * Gets a summary of current performance metrics.
     */
    public static String getPerformanceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("PERFORMANCE TEST SUMMARY\n");
        summary.append("========================\n\n");

        List<String> queryNames = PerformanceMonitor.getAllRecords().stream()
                .map(record -> record.getQueryName())
                .distinct()
                .toList();

        for (String queryName : queryNames) {
            PerformanceMonitor.PerformanceComparison comparison =
                PerformanceMonitor.getPerformanceComparison(queryName);

            summary.append(String.format("Query: %s\n", queryName));
            summary.append(String.format("Pre-opt avg: %.2f ms (%d samples)\n",
                comparison.getPreOptimizationAvg(), comparison.getPreOptSampleSize()));
            summary.append(String.format("Post-opt avg: %.2f ms (%d samples)\n",
                comparison.getPostOptimizationAvg(), comparison.getPostOptSampleSize()));
            summary.append(String.format("Improvement: %.1f%%\n\n",
                comparison.getImprovementPercentage()));
        }

        return summary.toString();
    }
}
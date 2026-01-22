package com.hospital.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for monitoring and recording query performance metrics.
 */
public class PerformanceMonitor {
    private static final List<PerformanceRecord> records = new ArrayList<>();

    public static class PerformanceRecord {
        private String queryName;
        private long executionTimeMs;
        private LocalDateTime timestamp;
        private boolean optimized;

        public PerformanceRecord(String queryName, long executionTimeMs, boolean optimized) {
            this.queryName = queryName;
            this.executionTimeMs = executionTimeMs;
            this.timestamp = LocalDateTime.now();
            this.optimized = optimized;
        }

        // Getters
        public String getQueryName() { return queryName; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isOptimized() { return optimized; }
    }

    /**
     * Records the execution time of a query.
     */
    public static void recordQueryTime(String queryName, long executionTimeMs, boolean optimized) {
        records.add(new PerformanceRecord(queryName, executionTimeMs, optimized));
    }

    /**
     * Measures and records the execution time of a Runnable operation.
     */
    public static void measureAndRecord(String queryName, Runnable operation, boolean optimized) {
        LocalDateTime start = LocalDateTime.now();
        operation.run();
        LocalDateTime end = LocalDateTime.now();
        long executionTimeMs = ChronoUnit.MILLIS.between(start, end);
        recordQueryTime(queryName, executionTimeMs, optimized);
    }

    /**
     * Gets all performance records.
     */
    public static List<PerformanceRecord> getAllRecords() {
        return new ArrayList<>(records);
    }

    /**
     * Gets performance records for a specific query.
     */
    public static List<PerformanceRecord> getRecordsForQuery(String queryName) {
        return records.stream()
                .filter(record -> record.getQueryName().equals(queryName))
                .collect(ArrayList::new, (list, record) -> list.add(record), ArrayList::addAll);
    }

    /**
     * Calculates average execution time for a query before and after optimization.
     */
    public static PerformanceComparison getPerformanceComparison(String queryName) {
        List<PerformanceRecord> queryRecords = getRecordsForQuery(queryName);

        List<Long> preOptTimes = new ArrayList<>();
        List<Long> postOptTimes = new ArrayList<>();

        for (PerformanceRecord record : queryRecords) {
            if (record.isOptimized()) {
                postOptTimes.add(record.getExecutionTimeMs());
            } else {
                preOptTimes.add(record.getExecutionTimeMs());
            }
        }

        double preOptAvg = preOptTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double postOptAvg = postOptTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double improvement = preOptAvg > 0 ? ((preOptAvg - postOptAvg) / preOptAvg) * 100 : 0.0;

        return new PerformanceComparison(queryName, preOptAvg, postOptAvg, improvement,
                                       preOptTimes.size(), postOptTimes.size());
    }

    public static class PerformanceComparison {
        private String queryName;
        private double preOptimizationAvg;
        private double postOptimizationAvg;
        private double improvementPercentage;
        private int preOptSampleSize;
        private int postOptSampleSize;

        public PerformanceComparison(String queryName, double preOptAvg, double postOptAvg,
                                   double improvement, int preOptSamples, int postOptSamples) {
            this.queryName = queryName;
            this.preOptimizationAvg = preOptAvg;
            this.postOptimizationAvg = postOptAvg;
            this.improvementPercentage = improvement;
            this.preOptSampleSize = preOptSamples;
            this.postOptSampleSize = postOptSamples;
        }

        // Getters
        public String getQueryName() { return queryName; }
        public double getPreOptimizationAvg() { return preOptimizationAvg; }
        public double getPostOptimizationAvg() { return postOptimizationAvg; }
        public double getImprovementPercentage() { return improvementPercentage; }
        public int getPreOptSampleSize() { return preOptSampleSize; }
        public int getPostOptSampleSize() { return postOptSampleSize; }
    }

    /**
     * Clears all performance records.
     */
    public static void clearRecords() {
        records.clear();
    }
}
package com.hospital.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton connection pool using HikariCP for PostgreSQL
 */
public class DatabaseConnection {
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            String url = DatabaseConfig.get("DB_URL", "jdbc:postgresql://localhost:5432/hospital_db");
            String user = DatabaseConfig.get("DB_USER", "postgres");
            String pass = DatabaseConfig.get("DB_PASS", "12345");

            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("HMSPool");

            dataSource = new HikariDataSource(config);
            System.out.println("HikariCP pool initialized: " + url);
        } catch (Exception e) {
            System.err.println("Failed to initialize connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a connection from the pool.
     * @return a live JDBC Connection
     * @throws SQLException when getting connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Close the pool when application stops
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariCP pool closed");
        }
    }
}

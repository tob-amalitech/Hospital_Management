package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

	/**
	 * Returns a JDBC Connection to PostgreSQL.
	 *
	 * Environment variables used (with defaults):
	 * - DB_URL (default: jdbc:postgresql://localhost:5432/postgres)
	 * - DB_USER (default: postgres)
	 * - DB_PASS (default: postgres)
	 */
	public static Connection getConnection() throws SQLException {
		String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/hospital_db");
		String user = System.getenv().getOrDefault("DB_USER", "postgres");
		String pass = System.getenv().getOrDefault("DB_PASS", "12345");

		return DriverManager.getConnection(url, user, pass);
	}

}

package com.hospital.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database configuration from resources/config.properties
 */
public class DatabaseConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.out.println("config.properties not found on classpath, falling back to env vars");
            }
        } catch (IOException e) {
            System.err.println("Failed to load config.properties: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, props.getProperty(key, defaultValue));
    }
}

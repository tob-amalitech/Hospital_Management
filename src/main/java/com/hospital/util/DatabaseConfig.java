package com.hospital.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Loads database configuration from resources/config.properties
 */
public class DatabaseConfig {
    private static final Properties props = new Properties();
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.out.println("config.properties not found on classpath");
            }
        } catch (IOException e) {
            System.err.println("Failed to load config.properties: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        // Priority: Top -> Down
        // 1. System Environment Variable
        // 2. .env file
        // 3. config.properties
        // 4. Default Value

        String value = System.getenv(key);
        if (value != null)
            return value;

        value = dotenv.get(key);
        if (value != null)
            return value;

        return props.getProperty(key, defaultValue);
    }
}

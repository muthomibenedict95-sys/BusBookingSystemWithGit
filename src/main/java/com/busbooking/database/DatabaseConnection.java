package com.busbooking.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for managing database connections.
 * Loads connection parameters from resources/config/database.properties.
 * If the configuration file or required properties are missing, it will throw a RuntimeException.
 */
public class DatabaseConnection {
    private static final Properties PROPERTIES = new Properties();
    private static final String CONFIG_FILE = "config/database.properties"; // Path relative to project root

    static {
        try (InputStream input = DatabaseConnection.class
        .getClassLoader()
        .getResourceAsStream(CONFIG_FILE)) {
            // FileInputStream throws FileNotFoundException if the file does not exist,
            // so the 'if (input == null)' check is no longer necessary.
           // PROPERTIES.load(input);
            // Ensure all required properties are present
            if (input == null) {
    throw new IOException("Configuration file not found: " + CONFIG_FILE);
}
       PROPERTIES.load(input);

       // Optional URL override for isolated integration testing.
       String databaseUrlOverride = System.getenv("BUSBOOKING_DB_URL");
       if (databaseUrlOverride != null && !databaseUrlOverride.isBlank()) {
           PROPERTIES.setProperty("db.url", databaseUrlOverride.trim());
       }

            if (!PROPERTIES.containsKey("db.url") ||
                !PROPERTIES.containsKey("db.username") ||
                !PROPERTIES.containsKey("db.password")) {
                throw new RuntimeException("Missing one or more required database properties in " + CONFIG_FILE + ". Ensure db.url, db.username, and db.password are set.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading database configuration from " + CONFIG_FILE + ". Please create it based on database.properties.example and ensure it's in the correct location and readable.", e);
        } catch (RuntimeException e) {
            throw e; // Re-throw the specific RuntimeExceptions for missing file/properties
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during database configuration loading", e);
        }
    }

    /**
     * Obtains a new connection to the MariaDB database.
     * @return a Connection object.
     * @throws RuntimeException if there is a problem with the database configuration or connection.
     */
    public static Connection getConnection() {
        String url = PROPERTIES.getProperty("db.url");
        String user = PROPERTIES.getProperty("db.username");
        String password = PROPERTIES.getProperty("db.password");

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MariaDB JDBC Driver not found in classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database: " + e.getMessage(), e);
        }
    }

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }
}

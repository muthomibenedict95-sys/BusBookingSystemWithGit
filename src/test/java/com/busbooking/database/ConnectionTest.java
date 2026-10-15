package com.busbooking.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTest {

    @Test
    public void testDriverAvailability() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            fail("MariaDB JDBC Driver not found in classpath");
        }
    }

    @Test
    public void testGetConnectionAttempt() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            assertNotNull(conn, "Database connection should be successful");
        } catch (RuntimeException e) {
            fail("Failed to get database connection: " + e.getMessage());
        }
    }
}

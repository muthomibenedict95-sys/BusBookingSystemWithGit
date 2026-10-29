package com.busbooking.dao;

import com.busbooking.database.DatabaseConnection;
import com.busbooking.model.Schedule;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ScheduleDAOIntegrationTest {

    @Test
    void returnsFutureSchedulesFromTestDatabase() throws SQLException {
        String url = System.getenv("BUSBOOKING_DB_URL");

        // Skip unless explicitly configured for the isolated test database.
        assumeTrue(
                url != null && url.matches(
                        "jdbc:mariadb://[^/]+/bus_booking_test(?:\\?.*)?"),
                "Set BUSBOOKING_DB_URL to the isolated bus_booking_test database."
        );

        // Independently verify the database selected by the live connection.
        try (Connection connection = DatabaseConnection.getConnection();
             var statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT DATABASE()")) {

            assertTrue(result.next());
            assertEquals(
                    "bus_booking_test",
                    result.getString(1),
                    "Integration tests must never run against another database."
            );
        }

        List<Schedule> schedules = new ScheduleDAO().findFutureSchedules();
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Nairobi"));

        assertTrue(
                schedules.stream().anyMatch(schedule ->
                        "Test-Maseno".equals(schedule.getRoute().getSource())
                                && "Test-Kisumu".equals(
                                        schedule.getRoute().getDestination())
                                && schedule.getDepartureDate().isAfter(today)),
                "The future test trip should be returned."
        );

        assertFalse(
                schedules.stream().anyMatch(schedule ->
                        "Test-Maseno".equals(schedule.getRoute().getSource())
                                && "Test-Kisumu".equals(
                                        schedule.getRoute().getDestination())
                                && schedule.getDepartureDate().isBefore(today)),
                "The past test trip must not be returned."
        );
    }
}

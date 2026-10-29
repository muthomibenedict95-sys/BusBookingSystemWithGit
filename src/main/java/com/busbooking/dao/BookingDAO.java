package com.busbooking.dao;

import com.busbooking.database.DatabaseConnection;
import com.busbooking.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    /**
     * A snapshot of the schedule's capacity and currently booked seats.
     */
    public record ScheduleAvailability(
            LocalDate departureDate,
            LocalTime departureTime,
            int busCapacity,
            Set<Integer> bookedSeatNumbers) {
        public ScheduleAvailability {
            bookedSeatNumbers = Set.copyOf(bookedSeatNumbers);
        }
    }

    /**
     * Reads schedule details and booked seats in one query.
     * This is for displaying availability, not reserving a seat.
     */
    public ScheduleAvailability getAvailability(int scheduleId) {
        String sql =
                "SELECT s.departure_date, s.departure_time, b.capacity, " +
                "bk.seat_number " +
                "FROM schedules s " +
                "JOIN buses b ON s.bus_id = b.id " +
                "LEFT JOIN bookings bk ON bk.schedule_id = s.id " +
                "WHERE s.id = ? ORDER BY bk.seat_number";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, scheduleId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                LocalDate date = rs.getDate("departure_date").toLocalDate();
                LocalTime time = rs.getTime("departure_time").toLocalTime();
                int capacity = rs.getInt("capacity");
                Set<Integer> bookedSeats = new TreeSet<>();

                do {
                    int seatNumber = rs.getInt("seat_number");
                    if (!rs.wasNull()) {
                        bookedSeats.add(seatNumber);
                    }
                } while (rs.next());

                return new ScheduleAvailability(
                        date, time, capacity, bookedSeats);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve seat availability.", e);
        }
    }

    
    public record ScheduleDetails(
            LocalDate departureDate,
            LocalTime departureTime,
            int busCapacity) {}

    /**
     * Locks the schedule while a booking transaction validates and reserves
     * a seat. The assigned bus capacity is read in the same locking query.
     */
    public ScheduleDetails lockScheduleForBooking(
            Connection connection, int scheduleId) throws SQLException {

        String sql =
                "SELECT s.departure_date, s.departure_time, b.capacity " +
                "FROM schedules s " +
                "JOIN buses b ON s.bus_id = b.id " +
                "WHERE s.id = ? FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, scheduleId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new ScheduleDetails(
                        rs.getDate("departure_date").toLocalDate(),
                        rs.getTime("departure_time").toLocalTime(),
                        rs.getInt("capacity")
                );
            }
        }
    }

    public boolean isSeatBooked(Connection connection, int scheduleId,
                                int seatNumber) throws SQLException {
        String sql =
                "SELECT id FROM bookings " +
                "WHERE schedule_id = ? AND seat_number = ? FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, scheduleId);
            statement.setInt(2, seatNumber);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Uses the caller's connection so insertion participates in the
     * BookingService transaction.
     */
    public void insert(Connection connection, Booking booking)
            throws SQLException {

        String sql =
                "INSERT INTO bookings " +
                "(schedule_id, passenger_name, seat_number) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, booking.getScheduleId());
            statement.setString(2, booking.getPassengerName());
            statement.setInt(3, booking.getSeatNumber());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Booking was not inserted.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "Could not retrieve the new booking ID.");
                }
                booking.setId(keys.getInt(1));
            }
        }

        String timestampSql =
                "SELECT booking_date FROM bookings WHERE id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(timestampSql)) {
            statement.setInt(1, booking.getId());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("booking_date");
                    if (timestamp != null) {
                        booking.setBookingDate(timestamp.toLocalDateTime());
                    }
                }
            }
        }
    }

    public Booking findById(int id) {
        String sql =
                "SELECT id, schedule_id, passenger_name, seat_number, " +
                "booking_date FROM bookings WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve booking.", e);
        }
    }

    public List<Booking> findByScheduleId(int scheduleId) {
        List<Booking> bookings = new ArrayList<>();

        String sql =
                "SELECT id, schedule_id, passenger_name, seat_number, " +
                "booking_date FROM bookings WHERE schedule_id = ? " +
                "ORDER BY booking_date DESC, id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, scheduleId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
            return bookings;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve bookings for this schedule.", e);
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("booking_date");

        return new Booking(
                rs.getInt("id"),
                rs.getInt("schedule_id"),
                rs.getString("passenger_name"),
                rs.getInt("seat_number"),
                timestamp == null ? null : timestamp.toLocalDateTime()
        );
    }
}

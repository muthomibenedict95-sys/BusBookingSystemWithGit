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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BookingDAO {
    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Africa/Nairobi");

    public record ScheduleAvailability(
            LocalDate departureDate,
            LocalTime departureTime,
            int busCapacity,
            Set<Integer> bookedSeatNumbers) {
        public ScheduleAvailability {
            bookedSeatNumbers = Set.copyOf(bookedSeatNumbers);
        }
    }

    public record ScheduleDetails(
            LocalDate departureDate,
            LocalTime departureTime,
            int busCapacity) {}

    /**
     * Returns seats occupied by confirmed reservations or unexpired holds.
     */
    public ScheduleAvailability getAvailability(int scheduleId) {
        String sql =
                "SELECT s.departure_date, s.departure_time, b.capacity, " +
                "sr.seat_number " +
                "FROM schedules s " +
                "JOIN buses b ON s.bus_id = b.id " +
                "LEFT JOIN seat_reservations sr " +
                "ON sr.schedule_id = s.id AND (" +
                "sr.status = 'CONFIRMED' OR " +
                "(sr.status = 'HELD' AND sr.expires_at > ?)) " +
                "WHERE s.id = ? ORDER BY sr.seat_number";

        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, now);
            statement.setInt(2, scheduleId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                LocalDate date =
                        rs.getDate("departure_date").toLocalDate();
                LocalTime time =
                        rs.getTime("departure_time").toLocalTime();
                int capacity = rs.getInt("capacity");
                Set<Integer> occupiedSeats = new TreeSet<>();

                do {
                    int seat = rs.getInt("seat_number");
                    if (!rs.wasNull()) {
                        occupiedSeats.add(seat);
                    }
                } while (rs.next());

                return new ScheduleAvailability(
                        date, time, capacity, occupiedSeats);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve seat availability.", e);
        }
    }

    /**
     * Locks a schedule while the caller validates and reserves a seat.
     * The caller must have started a transaction.
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
                        rs.getInt("capacity"));
            }
        }
    }

    /**
     * Expires old holds for a schedule.
     *
     * The caller must hold the schedule lock and commit this operation
     * together with any subsequent reservation operation.
     */
    public void releaseExpiredHolds(
            Connection connection,
            int scheduleId,
            LocalDateTime now) throws SQLException {

        String expireBookingsSql =
                "UPDATE bookings bk " +
                "JOIN seat_reservations sr ON sr.booking_id = bk.id " +
                "SET bk.status = 'EXPIRED' " +
                "WHERE sr.schedule_id = ? " +
                "AND sr.status = 'HELD' " +
                "AND sr.expires_at <= ? " +
                "AND bk.status = 'PENDING_PAYMENT'";

        try (PreparedStatement statement =
                     connection.prepareStatement(expireBookingsSql)) {
            statement.setInt(1, scheduleId);
            statement.setObject(2, now);
            statement.executeUpdate();
        }

        String expirePaymentsSql =
                "UPDATE payments p " +
                "JOIN seat_reservations sr ON sr.booking_id = p.booking_id " +
                "SET p.status = 'FAILED', p.completed_at = ? " +
                "WHERE sr.schedule_id = ? " +
                "AND sr.status = 'HELD' " +
                "AND sr.expires_at <= ? " +
                "AND p.status = 'PENDING'";

        try (PreparedStatement statement =
                     connection.prepareStatement(expirePaymentsSql)) {
            statement.setObject(1, now);
            statement.setInt(2, scheduleId);
            statement.setObject(3, now);
            statement.executeUpdate();
        }

        String deleteExpiredHoldsSql =
                "DELETE FROM seat_reservations " +
                "WHERE schedule_id = ? " +
                "AND status = 'HELD' AND expires_at <= ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(deleteExpiredHoldsSql)) {
            statement.setInt(1, scheduleId);
            statement.setObject(2, now);
            statement.executeUpdate();
        }
    }

    /**
     * Checks currently active allocations. Expired holds should be removed
     * by releaseExpiredHolds before a new reservation is inserted.
     */
    public boolean isSeatBooked(
            Connection connection,
            int scheduleId,
            int seatNumber,
            LocalDateTime now) throws SQLException {

        String sql =
                "SELECT id FROM seat_reservations " +
                "WHERE schedule_id = ? AND seat_number = ? " +
                "AND (status = 'CONFIRMED' OR " +
                "(status = 'HELD' AND expires_at > ?))";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, scheduleId);
            statement.setInt(2, seatNumber);
            statement.setObject(3, now);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserts a pending booking and its temporary seat hold.
     * Both inserts use the caller's transaction.
     */
    public void insert(Connection connection, Booking booking)
            throws SQLException {

        if (booking.getUserId() <= 0) {
            throw new IllegalArgumentException(
                    "A registered user is required to book a seat.");
        }

        if (booking.getHoldExpiresAt() == null) {
            throw new IllegalArgumentException(
                    "A pending booking must have a hold expiry time.");
        }

        String bookingSql =
                "INSERT INTO bookings " +
                "(user_id, schedule_id, passenger_name, seat_number, status) " +
                "VALUES (?, ?, ?, ?, 'PENDING_PAYMENT')";

        try (PreparedStatement statement = connection.prepareStatement(
                bookingSql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, booking.getUserId());
            statement.setInt(2, booking.getScheduleId());
            statement.setString(3, booking.getPassengerName());
            statement.setInt(4, booking.getSeatNumber());

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

        String reservationSql =
                "INSERT INTO seat_reservations " +
                "(booking_id, schedule_id, seat_number, status, expires_at) " +
                "VALUES (?, ?, ?, 'HELD', ?)";

        try (PreparedStatement statement =
                     connection.prepareStatement(reservationSql)) {

            statement.setInt(1, booking.getId());
            statement.setInt(2, booking.getScheduleId());
            statement.setInt(3, booking.getSeatNumber());
            statement.setObject(4, booking.getHoldExpiresAt());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Seat hold was not created.");
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
                        booking.setBookingDate(
                                timestamp.toLocalDateTime());
                    }
                }
            }
        }

        booking.setStatus(Booking.Status.PENDING_PAYMENT);
    }

    public Booking findById(int id) {
        String sql =
                "SELECT bk.id, bk.user_id, bk.schedule_id, " +
                "bk.passenger_name, bk.seat_number, bk.booking_date, " +
                "bk.status, sr.expires_at AS hold_expires_at " +
                "FROM bookings bk " +
                "LEFT JOIN seat_reservations sr ON sr.booking_id = bk.id " +
                "WHERE bk.id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve booking.", e);
        }
    }

    public List<Booking> findByScheduleId(int scheduleId) {
        List<Booking> bookings = new ArrayList<>();

        String sql =
                "SELECT bk.id, bk.user_id, bk.schedule_id, " +
                "bk.passenger_name, bk.seat_number, bk.booking_date, " +
                "bk.status, sr.expires_at AS hold_expires_at " +
                "FROM bookings bk " +
                "LEFT JOIN seat_reservations sr ON sr.booking_id = bk.id " +
                "WHERE bk.schedule_id = ? " +
                "ORDER BY bk.booking_date DESC, bk.id DESC";

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
        Timestamp bookingTimestamp = rs.getTimestamp("booking_date");
        Timestamp expiryTimestamp = rs.getTimestamp("hold_expires_at");

        return new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("schedule_id"),
                rs.getString("passenger_name"),
                rs.getInt("seat_number"),
                bookingTimestamp == null
                        ? null : bookingTimestamp.toLocalDateTime(),
                Booking.Status.valueOf(rs.getString("status")),
                expiryTimestamp == null
                        ? null : expiryTimestamp.toLocalDateTime()
        );
    }
}

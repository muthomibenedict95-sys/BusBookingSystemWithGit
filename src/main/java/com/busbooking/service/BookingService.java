package com.busbooking.service;

import com.busbooking.dao.BookingDAO;
import com.busbooking.database.DatabaseConnection;
import com.busbooking.model.Booking;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookingService {
    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Africa/Nairobi");

    private static final long HOLD_MINUTES = 10;

    private final BookingDAO bookingDAO;
    private final Clock clock;

    public BookingService() {
        this(new BookingDAO(), Clock.system(BUSINESS_ZONE));
    }

    public BookingService(BookingDAO bookingDAO, Clock clock) {
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Creates a pending booking and temporarily reserves its seat.
     * Payment must succeed before the booking can be confirmed.
     */
    public Booking createPendingBooking(
            int userId,
            int scheduleId,
            String passengerName,
            int seatNumber) {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "A registered customer is required to book a seat.");
        }

        validateBasicInput(scheduleId, passengerName, seatNumber);

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                BookingDAO.ScheduleDetails details =
                        bookingDAO.lockScheduleForBooking(
                                connection, scheduleId);

                if (details == null) {
                    throw new IllegalArgumentException(
                            "The requested schedule does not exist.");
                }

                // Use one consistent instant throughout the transaction.
                LocalDateTime now = LocalDateTime.now(
                        clock.withZone(BUSINESS_ZONE));

                validateBookingDetails(
                        scheduleId,
                        passengerName.trim(),
                        seatNumber,
                        details.departureDate(),
                        details.departureTime(),
                        details.busCapacity(),
                        now
                );

                // Release expired seats while holding the schedule lock.
                bookingDAO.releaseExpiredHolds(
                        connection, scheduleId, now);

                if (bookingDAO.isSeatBooked(
                        connection, scheduleId, seatNumber, now)) {
                    throw new IllegalStateException(
                            "That seat is already reserved. Choose another seat.");
                }

                Booking booking = new Booking(
                        0,
                        userId,
                        scheduleId,
                        passengerName.trim(),
                        seatNumber,
                        null,
                        Booking.Status.PENDING_PAYMENT,
                        now.plusMinutes(HOLD_MINUTES)
                );

                // The booking and its seat hold must commit together.
                bookingDAO.insert(connection, booking);
                connection.commit();

                return booking;

            } catch (SQLException e) {
                rollback(connection, e);

                if (isDuplicateKey(e)) {
                    throw new IllegalStateException(
                            "That seat has already been reserved. " +
                            "Choose another seat.", e);
                }

                throw new IllegalStateException(
                        "Could not create the pending booking.", e);

            } catch (RuntimeException e) {
                rollback(connection, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not open the booking database transaction.", e);
        }
    }

    public Booking getBookingById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Booking ID must be positive.");
        }

        return bookingDAO.findById(id);
    }

    public List<Booking> getBookingsForSchedule(int scheduleId) {
        if (scheduleId <= 0) {
            throw new IllegalArgumentException(
                    "Schedule ID must be positive.");
        }

        return bookingDAO.findByScheduleId(scheduleId);
    }

    /**
     * Returns currently available seats for a future departure.
     * Booking creation independently revalidates availability.
     */
    public List<Integer> getAvailableSeats(int scheduleId) {
        if (scheduleId <= 0) {
            throw new IllegalArgumentException(
                    "Schedule ID must be positive.");
        }

        BookingDAO.ScheduleAvailability availability =
                bookingDAO.getAvailability(scheduleId);

        if (availability == null) {
            throw new IllegalArgumentException(
                    "The requested schedule does not exist.");
        }

        LocalDateTime departure = availability.departureDate()
                .atTime(availability.departureTime());

        LocalDateTime now = LocalDateTime.now(
                clock.withZone(BUSINESS_ZONE));

        if (!departure.isAfter(now)) {
            throw new IllegalStateException(
                    "This trip has already departed or is departing now.");
        }

        List<Integer> availableSeats = new ArrayList<>();

        for (int seat = 1; seat <= availability.busCapacity(); seat++) {
            if (!availability.bookedSeatNumbers().contains(seat)) {
                availableSeats.add(seat);
            }
        }

        return List.copyOf(availableSeats);
    }

    public int getRemainingSeatCount(int scheduleId) {
        return getAvailableSeats(scheduleId).size();
    }

    static void validateBookingDetails(
            int scheduleId,
            String passengerName,
            int seatNumber,
            LocalDate departureDate,
            LocalTime departureTime,
            int busCapacity,
            LocalDateTime now) {

        validateBasicInput(scheduleId, passengerName, seatNumber);

        if (departureDate == null || departureTime == null) {
            throw new IllegalStateException(
                    "The schedule has no valid departure date or time.");
        }

        if (busCapacity <= 0 || seatNumber > busCapacity) {
            throw new IllegalArgumentException(
                    "Seat number must be between 1 and bus capacity (" +
                    busCapacity + ").");
        }

        LocalDateTime departure = departureDate.atTime(departureTime);

        if (!departure.isAfter(now)) {
            throw new IllegalStateException(
                    "This trip has already departed or is departing now. " +
                    "Choose a future departure.");
        }
    }

    private static void validateBasicInput(
            int scheduleId, String passengerName, int seatNumber) {

        if (scheduleId <= 0) {
            throw new IllegalArgumentException(
                    "Schedule ID must be positive.");
        }

        if (passengerName == null || passengerName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Passenger name cannot be empty.");
        }

        if (passengerName.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Passenger name cannot exceed 150 characters.");
        }

        if (seatNumber <= 0) {
            throw new IllegalArgumentException(
                    "Seat number must be positive.");
        }
    }

    private static boolean isDuplicateKey(SQLException exception) {
        for (SQLException current = exception;
             current != null;
             current = current.getNextException()) {
            if (current.getErrorCode() == 1062) {
                return true;
            }
        }

        return false;
    }

    private static void rollback(Connection connection, Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }
}

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

    private final BookingDAO bookingDAO;
    private final Clock clock;

    public BookingService() {
        this(new BookingDAO(), Clock.system(BUSINESS_ZONE));
    }

    public BookingService(BookingDAO bookingDAO, Clock clock) {
        this.bookingDAO = Objects.requireNonNull(bookingDAO);
        this.clock = Objects.requireNonNull(clock);
    }

    public Booking createBooking(int scheduleId, String passengerName,
                                 int seatNumber) {
        validateBasicInput(scheduleId, passengerName, seatNumber);

        Booking booking = new Booking(
                0, scheduleId, passengerName.trim(), seatNumber, null);

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

                // Read the clock after acquiring the schedule lock.
                LocalDateTime now = LocalDateTime.now(
                        clock.withZone(BUSINESS_ZONE));

                validateBookingDetails(
                        scheduleId,
                        booking.getPassengerName(),
                        seatNumber,
                        details.departureDate(),
                        details.departureTime(),
                        details.busCapacity(),
                        now
                );

                if (bookingDAO.isSeatBooked(
                        connection, scheduleId, seatNumber)) {
                    throw new IllegalStateException(
                            "That seat is already booked. Choose another seat.");
                }

                bookingDAO.insert(connection, booking);
                connection.commit();
                return booking;

            } catch (SQLException e) {
                rollback(connection, e);

                if (isDuplicateKey(e)) {
                    throw new IllegalStateException(
                            "That seat has already been booked. " +
                            "Choose another seat.", e);
                }

                throw new IllegalStateException(
                        "Could not create the booking.", e);

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
            throw new IllegalArgumentException("Booking ID must be positive.");
        }
        return bookingDAO.findById(id);
    }

    public List<Booking> getBookingsForSchedule(int scheduleId) {
        if (scheduleId <= 0) {
            throw new IllegalArgumentException("Schedule ID must be positive.");
        }
        return bookingDAO.findByScheduleId(scheduleId);
    }

    /**
     * Returns currently available seat numbers for a future departure.
     * Booking confirmation must independently revalidate availability.
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

    private static void validateBasicInput(int scheduleId,
                                           String passengerName,
                                           int seatNumber) {
        if (scheduleId <= 0) {
            throw new IllegalArgumentException(
                    "Schedule ID must be positive.");
        }

        if (passengerName == null || passengerName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Passenger name cannot be empty.");
        }

        if (passengerName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Passenger name cannot exceed 100 characters.");
        }

        if (seatNumber <= 0) {
            throw new IllegalArgumentException(
                    "Seat number must be positive.");
        }
    }

    /*
     * Package-private so the time and seat rules can be tested without
     * connecting to MariaDB.
     */
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

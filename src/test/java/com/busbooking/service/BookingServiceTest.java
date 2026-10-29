package com.busbooking.service;

import com.busbooking.dao.BookingDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 10, 20, 16, 0);

    @Test
    void acceptsDepartureLaterToday() {
        assertDoesNotThrow(() -> validate(TODAY, LocalTime.of(17, 0), 1));
    }

    @Test
    void rejectsDepartureEarlierToday() {
        assertThrows(IllegalStateException.class,
                () -> validate(TODAY, LocalTime.of(15, 0), 1));
    }

    @Test
    void rejectsDepartureExactlyNow() {
        assertThrows(IllegalStateException.class,
                () -> validate(TODAY, LocalTime.of(16, 0), 1));
    }

    @Test
    void rejectsDepartureOnPreviousDay() {
        assertThrows(IllegalStateException.class,
                () -> validate(TODAY.minusDays(1), LocalTime.of(23, 59), 1));
    }

    @Test
    void rejectsSeatAboveBusCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> validate(TODAY, LocalTime.of(17, 0), 41));
    }

    @Test
    void rejectsSeatNumberZero() {
        assertThrows(IllegalArgumentException.class,
                () -> validate(TODAY, LocalTime.of(17, 0), 0));
    }

    @Test
    void rejectsBlankPassengerName() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingService.validateBookingDetails(
                        1, "   ", 1, TODAY, LocalTime.of(17, 0), 40, NOW));
    }

    @Test
    void rejectsMissingDepartureDate() {
        assertThrows(IllegalStateException.class,
                () -> BookingService.validateBookingDetails(
                        1, "Jane Doe", 1, null,
                        LocalTime.of(17, 0), 40, NOW));
    }

    @Test
    void listsOnlyUnbookedSeats() {
        BookingDAO.ScheduleAvailability availability =
                new BookingDAO.ScheduleAvailability(
                        TODAY, LocalTime.of(17, 0), 40, Set.of(1, 3, 40));

        BookingService service = new BookingService(
                new StubBookingDAO(availability), testClock());

        List<Integer> seats = service.getAvailableSeats(1);

        assertEquals(37, seats.size());
        assertFalse(seats.contains(1));
        assertTrue(seats.contains(2));
        assertFalse(seats.contains(40));
    }

    @Test
    void calculatesRemainingSeatCount() {
        BookingDAO.ScheduleAvailability availability =
                new BookingDAO.ScheduleAvailability(
                        TODAY, LocalTime.of(17, 0), 40, Set.of(1, 3, 40));

        BookingService service = new BookingService(
                new StubBookingDAO(availability), testClock());

        assertEquals(37, service.getRemainingSeatCount(1));
    }

    @Test
    void rejectsAvailabilityForPastDeparture() {
        BookingDAO.ScheduleAvailability availability =
                new BookingDAO.ScheduleAvailability(
                        TODAY.minusDays(1), LocalTime.NOON, 40, Set.of());

        BookingService service = new BookingService(
                new StubBookingDAO(availability), testClock());

        assertThrows(IllegalStateException.class,
                () -> service.getAvailableSeats(1));
    }

    @Test
    void rejectsUnknownSchedule() {
        BookingService service = new BookingService(
                new StubBookingDAO(null), testClock());

        assertThrows(IllegalArgumentException.class,
                () -> service.getAvailableSeats(999));
    }

    @Test
    void rejectsBookingWithoutRegisteredUser() {
        BookingService service = new BookingService(
                new StubBookingDAO(null), testClock());

        assertThrows(IllegalArgumentException.class,
                () -> service.createPendingBooking(
                        0, 1, "Jane Doe", 1));
    }

    private Clock testClock() {
        return Clock.fixed(
                Instant.parse("2026-10-20T13:00:00Z"),
                ZoneId.of("Africa/Nairobi"));
    }

    private void validate(LocalDate date, LocalTime time, int seatNumber) {
        BookingService.validateBookingDetails(
                1, "Jane Doe", seatNumber, date, time, 40, NOW);
    }

    private static class StubBookingDAO extends BookingDAO {
        private final ScheduleAvailability availability;

        private StubBookingDAO(ScheduleAvailability availability) {
            this.availability = availability;
        }

        @Override
        public ScheduleAvailability getAvailability(int scheduleId) {
            return availability;
        }
    }
}

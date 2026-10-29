package com.busbooking.service;

import com.busbooking.dao.ScheduleDAO;
import com.busbooking.model.Schedule;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


class ScheduleServiceTest {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Africa/Nairobi");

    /*
     * Fixed at 16:00 on 20 October 2026 in Nairobi.
     * These tests therefore do not depend on the real current time.
     */
    private Clock testClock() {
        return Clock.fixed(
                Instant.parse("2026-10-20T13:00:00Z"),
                BUSINESS_ZONE
        );
    }

    @Test
    void acceptsDepartureLaterToday() {
        RecordingScheduleDAO dao = new RecordingScheduleDAO();
        ScheduleService service = new ScheduleService(dao, testClock());

        service.addSchedule(schedule(
                LocalDate.of(2026, 10, 20),
                LocalTime.of(17, 0)
        ));

        assertEquals(1, dao.insertCalls);
    }

    @Test
    void rejectsDepartureEarlierToday() {
        RecordingScheduleDAO dao = new RecordingScheduleDAO();
        ScheduleService service = new ScheduleService(dao, testClock());

        service.addSchedule(schedule(
                LocalDate.of(2026, 10, 20),
                LocalTime.of(15, 0)
        ));

        assertEquals(0, dao.insertCalls);
    }

    @Test
    void rejectsDepartureExactlyAtCurrentTime() {
        RecordingScheduleDAO dao = new RecordingScheduleDAO();
        ScheduleService service = new ScheduleService(dao, testClock());

        service.addSchedule(schedule(
                LocalDate.of(2026, 10, 20),
                LocalTime.of(16, 0)
        ));

        assertEquals(0, dao.insertCalls);
    }

    @Test
    void rejectsDepartureOnPreviousDate() {
        RecordingScheduleDAO dao = new RecordingScheduleDAO();
        ScheduleService service = new ScheduleService(dao, testClock());

        service.addSchedule(schedule(
                LocalDate.of(2026, 10, 19),
                LocalTime.of(23, 59)
        ));

        assertEquals(0, dao.insertCalls);
    }

    @Test
    void rejectsMissingDepartureDateOrTime() {
        RecordingScheduleDAO dao = new RecordingScheduleDAO();
        ScheduleService service = new ScheduleService(dao, testClock());

        service.addSchedule(schedule(null, LocalTime.NOON));
        service.addSchedule(schedule(
                LocalDate.of(2026, 10, 21), null
        ));

        assertEquals(0, dao.insertCalls);
    }

    @Test
    void returnsFutureSchedulesFromDao() {
        RecordingScheduleDAO dao = new RecordingScheduleDAO();
        Schedule expected = schedule(
                LocalDate.of(2026, 10, 21),
                LocalTime.of(9, 0)
        );
        dao.futureSchedules = java.util.List.of(expected);

        ScheduleService service = new ScheduleService(dao, testClock());

        assertSame(expected, service.getAvailableSchedules().get(0));
        assertEquals(1, dao.futureScheduleCalls);
    }

    private Schedule schedule(LocalDate date, LocalTime time) {
        Schedule schedule = new Schedule();
        schedule.setDepartureDate(date);
        schedule.setDepartureTime(time);
        return schedule;
    }

    /*
     * Records calls instead of accessing MariaDB.
     */
    private static class RecordingScheduleDAO extends ScheduleDAO {
        private int insertCalls;
        private int futureScheduleCalls;
        private java.util.List<Schedule> futureSchedules = java.util.List.of();

        @Override
        public void insert(Schedule schedule) {
            insertCalls++;
        }

        @Override
        public java.util.List<Schedule> findFutureSchedules() {
            futureScheduleCalls++;
            return futureSchedules;
        }
    }
}

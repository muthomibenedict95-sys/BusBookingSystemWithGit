package com.busbooking.service;

import com.busbooking.dao.ScheduleDAO;
import com.busbooking.model.Schedule;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class ScheduleService {
    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Africa/Nairobi");

    private final ScheduleDAO scheduleDAO;
    private final Clock clock;

    public ScheduleService() {
        this(new ScheduleDAO(), Clock.system(BUSINESS_ZONE));
    }

    public ScheduleService(ScheduleDAO scheduleDAO, Clock clock) {
        this.scheduleDAO = Objects.requireNonNull(scheduleDAO);
        this.clock = Objects.requireNonNull(clock);
    }

    private boolean hasFutureDeparture(Schedule schedule) {
        if (schedule == null
                || schedule.getDepartureDate() == null
                || schedule.getDepartureTime() == null) {
            return false;
        }

        LocalDateTime departure = schedule.getDepartureDate()
                .atTime(schedule.getDepartureTime());

        LocalDateTime now = LocalDateTime.now(clock);

        return departure.isAfter(now);
    }

    public void addSchedule(Schedule schedule) {
        if (!hasFutureDeparture(schedule)) {
            System.err.println(
                    "Departure date and time must be in the future.");
            return;
        }

        scheduleDAO.insert(schedule);
    }

    public Schedule getScheduleById(int id) {
        return scheduleDAO.findById(id);
    }

    public List<Schedule> getAllSchedules() {
        return scheduleDAO.findAll();
    }

    public List<Schedule> getAvailableSchedules() {
        return scheduleDAO.findFutureSchedules();
    }

    public void updateSchedule(Schedule schedule) {
        if (schedule == null || schedule.getId() <= 0) {
            System.err.println("Schedule ID must be valid for update.");
            return;
        }

        if (!hasFutureDeparture(schedule)) {
            System.err.println(
                    "Departure date and time must be in the future.");
            return;
        }

        scheduleDAO.update(schedule);
    }

    public void deleteSchedule(int id) {
        if (id <= 0) {
            System.err.println("Schedule ID must be valid for deletion.");
            return;
        }

        scheduleDAO.delete(id);
    }
}

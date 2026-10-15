package com.busbooking.service;

import com.busbooking.dao.ScheduleDAO;
import com.busbooking.model.Schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ScheduleService {
    private final ScheduleDAO scheduleDAO;

    public ScheduleService() {
        this.scheduleDAO = new ScheduleDAO();
    }

    public void addSchedule(Schedule schedule) {
        // Business logic for adding a schedule
        if (schedule.getDepartureDate().isBefore(LocalDate.now())) {
            System.err.println("Departure date cannot be in the past.");
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

    public void updateSchedule(Schedule schedule) {
        // Business logic for updating a schedule
        if (schedule.getId() <= 0) {
            System.err.println("Schedule ID must be valid for update.");
            return;
        }
        if (schedule.getDepartureDate().isBefore(LocalDate.now())) {
            System.err.println("Departure date cannot be in the past for update.");
            return;
        }
        scheduleDAO.update(schedule);
    }

    public void deleteSchedule(int id) {
        // Business logic for deleting a schedule
        if (id <= 0) {
            System.err.println("Schedule ID must be valid for deletion.");
            return;
        }
        // You might want to check if there are any bookings associated with this schedule
        scheduleDAO.delete(id);
    }
}

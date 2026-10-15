package com.busbooking.dao;

import com.busbooking.database.DatabaseConnection;
import com.busbooking.model.Bus;
import com.busbooking.model.Route;
import com.busbooking.model.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    public Schedule findById(int id) {
        String sql = "SELECT s.*, b.bus_number, b.capacity, b.type, " +
                     "r.source, r.destination, r.distance, r.base_price " +
                     "FROM schedules s " +
                     "JOIN buses b ON s.bus_id = b.id " +
                     "JOIN routes r ON s.route_id = r.id " +
                     "WHERE s.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSchedule(rs);
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error finding schedule by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Schedule> findAll() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, b.bus_number, b.capacity, b.type, " +
                     "r.source, r.destination, r.distance, r.base_price " +
                     "FROM schedules s " +
                     "JOIN buses b ON s.bus_id = b.id " +
                     "JOIN routes r ON s.route_id = r.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                schedules.add(mapRowToSchedule(rs));
            }
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error finding all schedules: " + e.getMessage());
        }
        return schedules;
    }

    private Schedule mapRowToSchedule(ResultSet rs) throws SQLException {
        Bus bus = new Bus(
            rs.getInt("bus_id"),
            rs.getString("bus_number"),
            rs.getInt("capacity"),
            rs.getString("type")
        );

        Route route = new Route(
            rs.getInt("route_id"),
            rs.getString("source"),
            rs.getString("destination"),
            rs.getDouble("distance"),
            rs.getBigDecimal("base_price")
        );

        return new Schedule(
            rs.getInt("id"),
            bus,
            route,
            rs.getDate("departure_date").toLocalDate(),
            rs.getTime("departure_time").toLocalTime()
        );
    }

    public void insert(Schedule schedule) {
        String sql = "INSERT INTO schedules (bus_id, route_id, departure_date, departure_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, schedule.getBus().getId());
            stmt.setInt(2, schedule.getRoute().getId());
            stmt.setDate(3, java.sql.Date.valueOf(schedule.getDepartureDate()));
            stmt.setTime(4, java.sql.Time.valueOf(schedule.getDepartureTime()));
            stmt.executeUpdate();
            System.out.println("Schedule added successfully for bus " + schedule.getBus().getBusNumber() + " on " + schedule.getDepartureDate());
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error adding schedule: " + e.getMessage());
        }
    }

    public void update(Schedule schedule) {
        String sql = "UPDATE schedules SET bus_id = ?, route_id = ?, departure_date = ?, departure_time = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, schedule.getBus().getId());
            stmt.setInt(2, schedule.getRoute().getId());
            stmt.setDate(3, java.sql.Date.valueOf(schedule.getDepartureDate()));
            stmt.setTime(4, java.sql.Time.valueOf(schedule.getDepartureTime()));
            stmt.setInt(5, schedule.getId());
            stmt.executeUpdate();
            System.out.println("Schedule updated successfully for ID " + schedule.getId());
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error updating schedule: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Schedule with ID " + id + " deleted successfully.");
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
        }
    }
}

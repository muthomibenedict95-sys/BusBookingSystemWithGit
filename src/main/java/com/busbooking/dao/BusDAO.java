package com.busbooking.dao;

import com.busbooking.database.DatabaseConnection;
import com.busbooking.model.Bus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BusDAO {

    public Bus findById(int id) {
        String sql = "SELECT * FROM buses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBus(rs);
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error finding bus by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Bus> findAll() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                buses.add(mapRowToBus(rs));
            }
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error finding all buses: " + e.getMessage());
        }
        return buses;
    }

    private Bus mapRowToBus(ResultSet rs) throws SQLException {
        return new Bus(
            rs.getInt("id"),
            rs.getString("bus_number"),
            rs.getInt("capacity"),
            rs.getString("type")
        );
    }

    public void insert(Bus bus) {
        String sql = "INSERT INTO buses (bus_number, capacity, type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bus.getBusNumber());
            stmt.setInt(2, bus.getCapacity());
            stmt.setString(3, bus.getType());
            stmt.executeUpdate();
            System.out.println("Bus added successfully: " + bus.getBusNumber());
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error adding bus: " + e.getMessage());
        }
    }

    public void update(Bus bus) {
        String sql = "UPDATE buses SET bus_number = ?, capacity = ?, type = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bus.getBusNumber());
            stmt.setInt(2, bus.getCapacity());
            stmt.setString(3, bus.getType());
            stmt.setInt(4, bus.getId());
            stmt.executeUpdate();
            System.out.println("Bus updated successfully: " + bus.getBusNumber());
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error updating bus: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM buses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Bus with ID " + id + " deleted successfully.");
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error deleting bus: " + e.getMessage());
        }
    }
}

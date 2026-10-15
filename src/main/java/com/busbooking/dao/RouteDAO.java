package com.busbooking.dao;

import com.busbooking.database.DatabaseConnection;
import com.busbooking.model.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    public Route findById(int id) {
        String sql = "SELECT * FROM routes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoute(rs);
                }
            }
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error finding route by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Route> findAll() {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT * FROM routes";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                routes.add(mapRowToRoute(rs));
            }
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error finding all routes: " + e.getMessage());
        }
        return routes;
    }

    private Route mapRowToRoute(ResultSet rs) throws SQLException {
        return new Route(
            rs.getInt("id"),
            rs.getString("source"),
            rs.getString("destination"),
            rs.getDouble("distance"),
            rs.getBigDecimal("base_price")
        );
    }

    public void insert(Route route) {
        String sql = "INSERT INTO routes (source, destination, distance, base_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, route.getSource());
            stmt.setString(2, route.getDestination());
            stmt.setDouble(3, route.getDistance());
            stmt.setBigDecimal(4, route.getBasePrice());
            stmt.executeUpdate();
            System.out.println("Route added successfully: " + route.getSource() + " to " + route.getDestination());
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error adding route: " + e.getMessage());
        }
    }

    public void update(Route route) {
        String sql = "UPDATE routes SET source = ?, destination = ?, distance = ?, base_price = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, route.getSource());
            stmt.setString(2, route.getDestination());
            stmt.setDouble(3, route.getDistance());
            stmt.setBigDecimal(4, route.getBasePrice());
            stmt.setInt(5, route.getId());
            stmt.executeUpdate();
            System.out.println("Route updated successfully: " + route.getSource() + " to " + route.getDestination());
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error updating route: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM routes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Route with ID " + id + " deleted successfully.");
        } catch (SQLException | NullPointerException e) {
            System.err.println("Error deleting route: " + e.getMessage());
        }
    }
}

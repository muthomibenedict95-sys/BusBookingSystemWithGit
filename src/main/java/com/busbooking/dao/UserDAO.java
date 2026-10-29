package com.busbooking.dao;

import com.busbooking.database.DatabaseConnection;
import com.busbooking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Database operations for customer accounts.
 */
public class UserDAO {

    /**
     * Internal login result. Do not display or log the password hash.
     */
    public record Account(User user, String passwordHash) {}

    public User insert(User user, String passwordHash) {
        String sql =
                "INSERT INTO users " +
                "(full_name, email, phone_number, password_hash) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhoneNumber());
            statement.setString(4, passwordHash);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("User account was not inserted.");
            }

            int id;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "Could not retrieve the new user ID.");
                }
                id = keys.getInt(1);
            }

            LocalDateTime createdAt;
            String timestampSql =
                    "SELECT created_at FROM users WHERE id = ?";

            try (PreparedStatement timestampStatement =
                         connection.prepareStatement(timestampSql)) {
                timestampStatement.setInt(1, id);

                try (ResultSet rs = timestampStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Could not retrieve the creation timestamp.");
                    }

                    Timestamp timestamp = rs.getTimestamp("created_at");
                    createdAt = timestamp == null
                            ? null : timestamp.toLocalDateTime();
                }
            }

            return new User(
                    id,
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    createdAt
            );

        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw new IllegalArgumentException(
                        "An account with that email already exists.", e);
            }

            throw new IllegalStateException(
                    "Could not register the user account.", e);
        }
    }

    /**
     * Returns null when no account has the supplied email.
     */
    public Account findAccountByEmail(String email) {
        String sql =
                "SELECT id, full_name, email, phone_number, " +
                "password_hash, created_at " +
                "FROM users WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp == null
                        ? null : timestamp.toLocalDateTime();

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        createdAt
                );

                return new Account(user, rs.getString("password_hash"));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve the user account.", e);
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
}

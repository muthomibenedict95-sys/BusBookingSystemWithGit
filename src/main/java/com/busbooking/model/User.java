package com.busbooking.model;

import java.time.LocalDateTime;

/**
 * Public account details for a registered customer.
 * Password hashes are deliberately excluded from this model.
 */
public final class User {
    private final int id;
    private final String fullName;
    private final String email;
    private final String phoneNumber;
    private final LocalDateTime createdAt;

    public User(int id, String fullName, String email,
                String phoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

package com.busbooking.model;

import java.time.LocalDateTime;

/**
 * Represents one customer's reservation of one seat on a schedule.
 */
public class Booking {
    public enum Status {
        PENDING_PAYMENT,
        CONFIRMED,
        EXPIRED,
        CANCELLED
    }

    private int id;
    private int userId;
    private int scheduleId;
    private String passengerName;
    private int seatNumber;
    private LocalDateTime bookingDate;
    private Status status = Status.PENDING_PAYMENT;

    // Comes from seat_reservations, not directly from the bookings table.
    private LocalDateTime holdExpiresAt;

    public Booking() {
    }

    /**
     * Retained for compatibility with existing model tests and code.
     * New bookings must use the constructor that includes userId and status.
     */
    public Booking(int id, int scheduleId, String passengerName,
                   int seatNumber, LocalDateTime bookingDate) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
    }

    public Booking(int id, int userId, int scheduleId, String passengerName,
                   int seatNumber, LocalDateTime bookingDate, Status status,
                   LocalDateTime holdExpiresAt) {
        this.id = id;
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
        this.status = status;
        this.holdExpiresAt = holdExpiresAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }
}

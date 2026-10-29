package com.busbooking.model;

import java.time.LocalDateTime;

/**
 * Represents one passenger's reservation of one seat on a schedule.
 */
public class Booking {
    private int id;
    private int scheduleId;
    private String passengerName;
    private int seatNumber;
    private LocalDateTime bookingDate;

    public Booking() {}

    public Booking(int id, int scheduleId, String passengerName,
                   int seatNumber, LocalDateTime bookingDate) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}

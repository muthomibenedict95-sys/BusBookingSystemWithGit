package com.busbooking.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a planned service involving a bus on a specific route.
 * References a Bus object to access its capacity rather than duplicating it.
 */
public class Schedule {
    private int id;
    private Bus bus;
    private Route route;
    private LocalDate departureDate;
    private LocalTime departureTime;

    public Schedule() {}

    public Schedule(int id, Bus bus, Route route, LocalDate departureDate, LocalTime departureTime) {
        this.id = id;
        this.bus = bus;
        this.route = route;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * Convenience method to get the capacity from the associated bus.
     * @return the capacity of the bus assigned to this schedule.
     */
    public int getBusCapacity() {
        return bus != null ? bus.getCapacity() : 0;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", bus=" + (bus != null ? bus.getBusNumber() : "null") +
                ", route=" + (route != null ? route.getSource() + "-" + route.getDestination() : "null") +
                ", departureDate=" + departureDate +
                ", departureTime=" + departureTime +
                '}';
    }
}

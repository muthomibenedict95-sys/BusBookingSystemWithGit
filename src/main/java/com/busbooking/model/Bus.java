package com.busbooking.model;

/**
 * Represents a physical bus vehicle.
 * A bus has a fixed passenger capacity which it owns.
 */
public class Bus {
    private int id;
    private String busNumber;
    private int capacity;
    private String type;

    public Bus() {}

    public Bus(int id, String busNumber, int capacity, String type) {
        this.id = id;
        this.busNumber = busNumber;
        this.capacity = capacity;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "id=" + id +
                ", busNumber='" + busNumber + '\'' +
                ", capacity=" + capacity +
                ", type='" + type + '\'' +
                '}';
    }
}

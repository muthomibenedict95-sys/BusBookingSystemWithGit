package com.busbooking.model;

import java.math.BigDecimal;

/**
 * Represents a travel route between a source and a destination.
 */
public class Route {
    private int id;
    private String source;
    private String destination;
    private double distance;
    private BigDecimal basePrice;

    public Route() {}

    public Route(int id, String source, String destination, double distance, BigDecimal basePrice) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.basePrice = basePrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", distance=" + distance +
                ", basePrice=" + basePrice +
                '}';
    }
}

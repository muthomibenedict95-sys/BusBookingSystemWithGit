package com.busbooking.service;

import com.busbooking.dao.RouteDAO;
import com.busbooking.model.Route;

import java.math.BigDecimal;
import java.util.List;

public class RouteService {
    private final RouteDAO routeDAO;

    public RouteService() {
        this.routeDAO = new RouteDAO();
    }

    public void addRoute(Route route) {
        // Business logic for adding a route
        if (route.getDistance() <= 0) {
            System.err.println("Route distance must be greater than 0.");
            return;
        }
        if (route.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Route base price must be greater than 0.");
            return;
        }
        routeDAO.insert(route);
    }

    public Route getRouteById(int id) {
        return routeDAO.findById(id);
    }

    public List<Route> getAllRoutes() {
        return routeDAO.findAll();
    }

    public void updateRoute(Route route) {
        // Business logic for updating a route
        if (route.getId() <= 0) {
            System.err.println("Route ID must be valid for update.");
            return;
        }
        if (route.getDistance() <= 0) {
            System.err.println("Route distance must be greater than 0 for update.");
            return;
        }
        if (route.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Route base price must be greater than 0 for update.");
            return;
        }
        routeDAO.update(route);
    }

    public void deleteRoute(int id) {
        // Business logic for deleting a route
        if (id <= 0) {
            System.err.println("Route ID must be valid for deletion.");
            return;
        }
        // You might want to check if there are any schedules associated with this route
        routeDAO.delete(id);
    }
}

package com.busbooking.service;

import com.busbooking.dao.BusDAO;
import com.busbooking.model.Bus;

import java.util.List;

public class BusService {
    private final BusDAO busDAO;

    public BusService() {
        this.busDAO = new BusDAO();
    }

    public void addBus(Bus bus) {
        // Here you can add business logic, e.g., validation
        if (bus.getCapacity() <= 0) {
            System.err.println("Bus capacity must be greater than 0.");
            return;
        }
        busDAO.insert(bus);
    }

    public Bus getBusById(int id) {
        return busDAO.findById(id);
    }

    public List<Bus> getAllBuses() {
        return busDAO.findAll();
    }

    public void updateBus(Bus bus) {
        // Add business logic for updates
        if (bus.getId() <= 0) {
            System.err.println("Bus ID must be valid for update.");
            return;
        }
        if (bus.getCapacity() <= 0) {
            System.err.println("Bus capacity must be greater than 0 for update.");
            return;
        }
        busDAO.update(bus);
    }

    public void deleteBus(int id) {
        // Add business logic for deletion, e.g., check if bus is in active schedules
        if (id <= 0) {
            System.err.println("Bus ID must be valid for deletion.");
            return;
        }
        busDAO.delete(id);
    }
}

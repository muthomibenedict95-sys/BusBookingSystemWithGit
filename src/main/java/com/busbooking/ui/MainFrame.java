package com.busbooking.ui;

import com.busbooking.model.Bus;
import com.busbooking.service.BusService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final BusService busService;

    public MainFrame() {
        this.busService = new BusService();
        initComponents();
        // For demonstration: fetch and print all buses
        printAllBuses();
    }

    private void initComponents() {
        setTitle("Bus Booking System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Set background color using UiConstants
        getContentPane().setBackground(UiConstants.BACKGROUND_COLOR);

        // Example label
        JLabel welcomeLabel = new JLabel("Welcome to the Bus Booking System!", SwingConstants.CENTER);
        welcomeLabel.setFont(UiConstants.FONT_TITLE);
        welcomeLabel.setForeground(UiConstants.PRIMARY_COLOR);
        add(welcomeLabel, BorderLayout.CENTER);
    }

    private void printAllBuses() {
        System.out.println("\n--- Available Buses ---");
        List<Bus> buses = busService.getAllBuses();
        if (buses.isEmpty()) {
            System.out.println("No buses found.");
        } else {
            for (Bus bus : buses) {
                System.out.println("ID: " + bus.getId() + ", Number: " + bus.getBusNumber() + ", Capacity: " + bus.getCapacity() + ", Type: " + bus.getType());
            }
        }
        System.out.println("-------------------------");
    }

    public static void main(String[] args) {
        // Ensure the UI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}

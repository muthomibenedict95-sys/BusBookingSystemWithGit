package com.busbooking.ui;

import com.busbooking.model.Bus;
import com.busbooking.service.BusService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final BusService busService;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;

    public MainFrame() {
        busService = new BusService();

        tableModel = new DefaultTableModel(
                new Object[]{"Bus Number", "Capacity", "Type"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        statusLabel = new JLabel("Loading buses...");

        initComponents();
        loadBuses();
    }

    private void initComponents() {
        setTitle("Bus Booking System");
        setSize(1000, 700);
        setMinimumSize(new Dimension(
                UiConstants.MIN_WINDOW_WIDTH,
                UiConstants.MIN_WINDOW_HEIGHT
        ));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 20));
        contentPanel.setBackground(UiConstants.BACKGROUND_COLOR);
        contentPanel.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        JLabel titleLabel = new JLabel(
                "Bus Booking System",
                SwingConstants.CENTER
        );
        titleLabel.setFont(UiConstants.FONT_LARGE_TITLE);
        titleLabel.setForeground(UiConstants.PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel(
                "Available buses",
                SwingConstants.CENTER
        );
        subtitleLabel.setFont(UiConstants.FONT_REGULAR);
        subtitleLabel.setForeground(UiConstants.TEXT_COLOR);

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        JTable busTable = new JTable(tableModel);
        busTable.setFont(UiConstants.FONT_REGULAR);
        busTable.setRowHeight(35);
        busTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        busTable.setFillsViewportHeight(true);
        busTable.setAutoCreateRowSorter(true);
        busTable.getTableHeader().setFont(UiConstants.FONT_BOLD);
        busTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(busTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(UiConstants.BORDER_COLOR)
        );

        statusLabel.setFont(UiConstants.FONT_SMALL);
        statusLabel.setForeground(UiConstants.TEXT_COLOR);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }

    private void loadBuses() {
        try {
            List<Bus> buses = busService.getAllBuses();

            tableModel.setRowCount(0);

            for (Bus bus : buses) {
                tableModel.addRow(new Object[]{
                        bus.getBusNumber(),
                        bus.getCapacity(),
                        bus.getType()
                });
            }

            if (buses.isEmpty()) {
                statusLabel.setText(
                        "No buses found in the selected database."
                );
            } else {
                statusLabel.setText(
                        "Loaded " + buses.size() + " bus(es)."
                );
            }
        } catch (RuntimeException exception) {
            statusLabel.setText(
                    "Unable to load buses. Check the database connection."
            );
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}

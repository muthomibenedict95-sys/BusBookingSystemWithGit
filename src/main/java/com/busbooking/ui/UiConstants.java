package com.busbooking.ui;

import java.awt.Color;
import java.awt.Font;

public class UiConstants {

    // Colors
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219);    // Blue
    public static final Color SECONDARY_COLOR = new Color(149, 165, 166); // Gray
    public static final Color DANGER_COLOR = new Color(231, 76, 60);      // Red
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);    // Green
    public static final Color WARNING_COLOR = new Color(241, 196, 15);    // Yellow
    public static final Color INFO_COLOR = new Color(52, 152, 219);       // Blue
    public static final Color TEXT_COLOR = new Color(44, 62, 80);         // Dark Blue
    public static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // Light Gray
    public static final Color BORDER_COLOR = new Color(189, 195, 199);     // Medium Gray
    public static final Color SEAT_AVAILABLE_COLOR = new Color(153, 204, 255); // Light Blue
    public static final Color SEAT_SELECTED_COLOR = new Color(255, 204, 102);  // Orange
    public static final Color SEAT_OCCUPIED_COLOR = new Color(153, 153, 153);  // Dark Gray
    public static final Color SEAT_UNAVAILABLE_COLOR = new Color(204, 204, 204); // Lighter Gray

    // Fonts
    public static final Font FONT_LARGE_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // Spacing and Dimensions
    public static final int PADDING_SMALL = 5;
    public static final int PADDING_MEDIUM = 10;
    public static final int PADDING_LARGE = 20;
    public static final int BORDER_RADIUS = 5;
    public static final int BUTTON_HEIGHT = 30;
    public static final int FIELD_HEIGHT = 30;
    public static final int DIALOG_WIDTH = 400;
    public static final int DIALOG_HEIGHT = 200;

    // Main Window Defaults
    public static final int MIN_WINDOW_WIDTH = 1000;
    public static final int MIN_WINDOW_HEIGHT = 700;

    private UiConstants() {
        // Prevent instantiation
    }
}

package org.example;

import adminmenu.UI;
import dao.DatabaseConnection;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (var connection = DatabaseConnection.getConnection()) {
            System.out.println("Food Service Tracking System database connected!");

            UI ui = new UI();
            ui.start();

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}
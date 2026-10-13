package com.scc.ui;

import com.scc.dao.DatabaseConnection;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (var connection = DatabaseConnection.getConnection()) {
            System.out.println("Food Service Tracking System database connected!");

            adminmenu ui = new adminmenu();
            ui.start();

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}
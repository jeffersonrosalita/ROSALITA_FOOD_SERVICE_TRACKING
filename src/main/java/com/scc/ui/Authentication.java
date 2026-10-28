package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao = new UserDao();
    private final Scanner scanner = new Scanner(System.in);

    public void showAuthMenu() {
        while (true) {
            System.out.println("\n--- FOOD SERVICE TRACKING SYSTEM ---");
            System.out.println("1. Login");
            System.out.println("2. Register Account");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegistration();
                case "3" -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.loginUser(username, password);

        if (user == null) {
            System.out.println("Login failed! Invalid credentials.");
            return;
        }

        if (!user.status().equalsIgnoreCase("APPROVED")) {
            System.out.println("Login failed! Account pending approval.");
            return;
        }

        System.out.println("Login successful! Welcome, " + user.username());
        routeUser(user.roles(), user);
    }

    private void handleRegistration() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        // Validated loop that keeps showing only the role selection category on invalid input
        String role = readValidRegistrationRole();
        if (role == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        userDao.registerUser(username, password, role);
    }

    private String readValidRegistrationRole() {
        while (true) {
            System.out.print("Select Role: 1. CASHIER  2. STAFF  3. ADMIN [0 to cancel]: ");
            String roleChoice = scanner.nextLine().trim();

            if (roleChoice.equals("0")) {
                return null;
            }

            switch (roleChoice) {
                case "1" -> {
                    return "CASHIER";
                }
                case "2" -> {
                    return "STAFF";
                }
                case "3" -> {
                    return "ADMIN";
                }
                default -> System.out.println("Invalid choice! Please select a valid role option from the list.");
            }
        }
    }

    private void routeUser(String role, User currentUser) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            AdminMenu adminMenu = new AdminMenu(userDao, scanner, currentUser);
            adminMenu.displayMenu();
        } else {
            System.out.println("Routing to " + role + " portal...");
        }
    }
}
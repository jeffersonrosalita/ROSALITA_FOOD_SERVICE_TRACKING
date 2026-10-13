package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.UserDao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class adminmenu {
    private final Scanner sc = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void start() {
        while (true) {
            System.out.println("\n=== FOOD SERVICE TRACKING SYSTEM ===");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            switch (sc.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Enter username: ");
        String name = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (password.isBlank()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        System.out.println("\nSelect role:");
        System.out.println("1. ADMINISTRATOR");
        System.out.println("2. STAFF");
        System.out.println("3. CUSTOMER");
        System.out.print("Choose: ");

        String role = switch (sc.nextLine().trim()) {
            case "1" -> "ADMINISTRATOR";
            case "2" -> "STAFF";
            case "3" -> "CUSTOMER";
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid role. Select 1, 2, or 3.");
            return;
        }

        try {
            if (userDao.register(name, password, role)) {
                System.out.println("\nRegistration successful!");
                System.out.println("Username: " + name);
                System.out.println("Role: " + role);
                System.out.println("Status: PENDING");
                System.out.println("Your account requires administrator approval before you can log in.");
            } else {
                System.out.println("Registration failed.");
            }
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter username: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        try {
            Optional<User> result = userDao.login(name, password);

            if (result.isEmpty()) {
                System.out.println("Invalid username/password, or your account is still pending approval.");
                return;
            }

            User user = result.get();
            System.out.println("\nLogin successful! Welcome, " + user.name());

            if ("ADMINISTRATOR".equalsIgnoreCase(user.role())) {
                adminMenu();
            } else {
                userMenu(user);
            }

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void adminMenu() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. View & Manage Pending Applicants");
            System.out.println("2. Logout");
            System.out.print("Choose: ");

            switch (sc.nextLine().trim()) {
                case "1" -> reviewPendingApplicants();
                case "2" -> {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void reviewPendingApplicants() {
        try {
            List<User> pendingUsers = userDao.getPendingUsers();

            if (pendingUsers.isEmpty()) {
                System.out.println("\nNo pending applicants found.");
                return;
            }

            System.out.println("\n--- PENDING APPLICANTS ---");
            for (User u : pendingUsers) {
                System.out.println("ID: " + u.id() + " | Username: " + u.name() + " | Role: " + u.role());
            }

            System.out.print("\nEnter the User ID to manage (or type 0 to go back): ");
            String input = sc.nextLine().trim();
            int userId = Integer.parseInt(input);

            if (userId == 0) {
                return;
            }

            System.out.println("\nChoose action for User ID " + userId + ":");
            System.out.println("1. APPROVE");
            System.out.println("2. KEEP PENDING");
            System.out.print("Choose: ");
            String actionChoice = sc.nextLine().trim();

            String newStatus = switch (actionChoice) {
                case "1" -> "APPROVED";
                case "2" -> "PENDING";
                default -> null;
            };

            if (newStatus == null) {
                System.out.println("Invalid action selected.");
                return;
            }

            if (userDao.updateUserStatus(userId, newStatus)) {
                System.out.println("Applicant status successfully updated to: " + newStatus);
            } else {
                System.out.println("Failed to update status. Invalid User ID.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric ID.");
        }
    }

    private void userMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== FOOD SERVICE USER MENU ===");
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.print("Choose: ");

            switch (sc.nextLine().trim()) {
                case "1" -> {
                    System.out.println("\n--- ACCOUNT DETAILS ---");
                    System.out.println("ID: " + user.id());
                    System.out.println("Username: " + user.name());
                    System.out.println("Role: " + user.role());
                    System.out.println("Status: " + user.status());
                }
                case "2" -> {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
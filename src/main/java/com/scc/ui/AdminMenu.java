package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner scanner, User currentAdmin) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println(" FOOD SERVICE TRACKING SYSTEM - ADMIN");
            System.out.println(" Welcome, " + currentAdmin.username() + "!");
            System.out.println("========================================");
            System.out.println("1. View All System Users");
            System.out.println("2. Update User Role & Status");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> handleUpdateUser();
                case "3" -> {
                    System.out.println("Logging out from admin session.");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n========================================================");
        System.out.println("ALL REGISTERED SYSTEM USERS");
        System.out.println("========================================================");
        System.out.printf("%-4s | %-12s | %-10s | %-10s%n", "ID", "Username", "Role", "Status");
        System.out.println("--------------------------------------------------------");
        for (User u : users) {
            System.out.printf("%-4d | %-12s | %-10s | %-10s%n",
                    u.id() != null ? u.id() : 0,
                    u.username(),
                    u.roles(),
                    u.status());
        }
        System.out.println("========================================================");
    }

    public void handleUpdateUser() {
        int targetId = -1;
        while (true) {
            List<User> users = userDao.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No system users found.");
                return;
            }

            // Re-display the registered users table on every loop iteration
            viewAllUsers();

            targetId = readIntInput("Enter Target User ID to update (Enter 0 to cancel): ");

            if (targetId == 0) {
                System.out.println("Operation cancelled.");
                return;
            }

            // Self-Edit Security Restriction
            if (targetId == currentAdmin.id()) {
                System.out.println("[!] Error: Modifying the currently active admin account is restricted.");
                continue;
            }

            final int selectedId = targetId;
            boolean idExists = users.stream().anyMatch(u -> u.id() == selectedId);

            if (idExists) {
                break;
            } else {
                System.out.println("[!] Error: User ID " + targetId + " does not exist. Please try again.");
            }
        }

        String newRole = readValidRole();
        if (newRole == null) return;

        String newStatus = readValidStatus();
        if (newStatus == null) return;

        boolean success = userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
        if (success) {
            System.out.println("Success: User ID " + targetId + " updated to Role: " + newRole + ", Status: " + newStatus);
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid numeric input! Please enter numbers only.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (CASHIER, STAFF, ADMIN): ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("CASHIER") || role.equals("STAFF") || role.equals("ADMIN")) {
                return role;
            }
            System.out.println("[!] Invalid choice! Please choose from CASHIER, STAFF, or ADMIN.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED, PENDING): ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("[!] Invalid choice! Please choose from APPROVED or PENDING.");
        }
    }
}
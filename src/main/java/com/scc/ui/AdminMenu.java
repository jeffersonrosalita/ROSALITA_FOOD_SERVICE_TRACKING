package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.Model.ActivityLog;
import com.scc.Model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;
    private final ActivityLogDao logDao = new ActivityLogDao();

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
            System.out.println("2. Manage / Approve Pending Users");
            System.out.println("3. Update User Role & Status");
            System.out.println("4. View System Audit Logs");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> managePendingUsers();
                case "3" -> handleUpdateUser();
                case "4" -> displayAuditLogsMenu();
                case "5" -> {
                    System.out.println("Logging out from admin session.");
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n=== ALL SYSTEM USERS ===");
        System.out.printf("%-5s | %-15s | %-12s | %-10s%n", "ID", "Username", "Role", "Status");
        System.out.println("-".repeat(50));
        for (User u : users) {
            System.out.printf("%-5d | %-15s | %-12s | %-10s%n", u.id(), u.username(), u.role(), u.status());
        }
    }

    private void managePendingUsers() {
        List<User> pendingUsers = userDao.getUsersByStatus("PENDING");
        if (pendingUsers.isEmpty()) {
            System.out.println("\nNo pending user registrations found.");
            return;
        }

        System.out.println("\n=== PENDING USER APPROVALS ===");
        System.out.printf("%-5s | %-15s | %-12s%n", "ID", "Username", "Role");
        System.out.println("-".repeat(35));
        for (User u : pendingUsers) {
            System.out.printf("%-5d | %-15s | %-12s%n", u.id(), u.username(), u.role());
        }

        int targetId = readIntInput("Enter User ID to approve (or 0 to cancel): ");
        if (targetId == 0) return;

        boolean found = pendingUsers.stream().anyMatch(u -> u.id() == targetId);
        if (found) {
            boolean success = userDao.updateUserStatus(targetId, "APPROVED");
            if (success) {
                String desc = String.format("Admin [%d] approved User [%d]", currentAdmin.id(), targetId);
                logDao.logAction(currentAdmin.id(), "USER_APPROVED", desc);
                System.out.println("User account successfully approved and logged!");
            } else {
                System.out.println("Failed to update user status in the database.");
            }
        } else {
            System.out.println("Invalid User ID from the pending list.");
        }
    }

    private void handleUpdateUser() {
        viewAllUsers();
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        int targetUserId = readIntInput("Enter target User ID (or 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        List<User> users = userDao.getAllUsers();
        boolean exists = users.stream().anyMatch(u -> u.id() == targetUserId);
        if (!exists) {
            System.out.println("User ID not found.");
            return;
        }

        String newRole = readValidRole();
        if (newRole.equals("CANCEL")) return;

        String newStatus = readValidStatus();
        if (newStatus.equals("CANCEL")) return;

        boolean success = userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus);
        if (success) {
            String desc = String.format("Admin [%d] approved User [%d] or Role changed to %s",
                    currentAdmin.id(), targetUserId, newRole);
            logDao.logAction(currentAdmin.id(), "USER_UPDATED", desc);
            System.out.println("User role and status successfully updated and logged!");
        } else {
            System.out.println("Failed to update user in the database.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (ADMIN, STAFF, CUSTOMER): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0")) return "CANCEL";
            if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("CUSTOMER")) {
                return role;
            }
            System.out.println("Invalid role. Please provide a valid role.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED, PENDING): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0")) return "CANCEL";
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status. Please enter APPROVED or PENDING.");
        }
    }

    private void displayAuditLogsMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("         SYSTEM AUDIT LOG MENU          ");
            System.out.println("========================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs Safely");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> renderLogTable(logDao.getAllLogs());
                case "2" -> {
                    int userId = readIntInput("Enter Target User ID: ");
                    renderLogTable(logDao.getLogsByUserId(userId));
                }
                case "3" -> handleClearLogs();
                case "4" -> { return; }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleClearLogs() {
        System.out.print("Are you sure you want to clear all historical audit logs? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if ("Y".equals(confirm)) {
            boolean success = logDao.clearAllLogs();
            if (success) {
                System.out.println("All historical activity logs cleared successfully.");
                logDao.logAction(currentAdmin.id(), "LOGS_CLEARED", "Admin [" + currentAdmin.id() + "] cleared historical audit logs.");
            } else {
                System.out.println("Failed to clear activity logs.");
            }
        } else {
            System.out.println("Log clearing action cancelled.");
        }
    }

    private void renderLogTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }
        System.out.println("-".repeat(95));
        System.out.printf("%-5s | %-8s | %-18s | %-35s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("-".repeat(95));

        for (ActivityLog log : logs) {
            System.out.printf("%-5d | %-8d | %-18s | %-35s | %-20s%n",
                    log.id(),
                    log.userId(),
                    log.actionType(),
                    log.description(),
                    log.timestamp().toString()
            );
        }
        System.out.println("-".repeat(95));
    }
}
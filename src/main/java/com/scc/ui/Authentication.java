package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.DatabaseConnection;
import com.scc.Model.ActivityLog;
import com.scc.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("\n=== FOOD SERVICE TRACKING SYSTEM ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> login();
                case "2" -> register();
                case "3" -> {
                    System.out.println("Exiting application. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.login(username, password);
        if (user != null) {
            if ("PENDING".equals(user.status())) {
                System.out.println("Your account is pending admin approval.");
                return;
            }

            String logDesc = String.format("User %s (ID: %d) logged in.", user.username(), user.id());
            logDao.logAction(user.id(), "USER_LOGIN", logDesc);
            System.out.println("Login successful! Welcome, " + user.username());

            switch (user.role().toUpperCase()) {
                case "ADMIN" -> runAdminDashboard(user);
                case "STAFF" -> runStaffDashboard(user);
                case "CUSTOMER" -> runCustomerDashboard(user);
                default -> System.out.println("Unrecognized role. Contact system administrator.");
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void register() {
        System.out.print("Choose username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Choose role (ADMIN / STAFF / CUSTOMER): ");
        String role = scanner.nextLine().trim().toUpperCase();

        if (!role.equals("ADMIN") && !role.equals("STAFF") && !role.equals("CUSTOMER")) {
            System.out.println("Invalid role entered! Defaulting to CUSTOMER.");
            role = "CUSTOMER";
        }

        boolean success = userDao.registerUser(username, password, role);
        if (success) {
            System.out.println("Registration successful! Please wait for admin approval.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    // ==========================================
    // 1. ADMIN DASHBOARD
    // ==========================================
    private void runAdminDashboard(User currentAdmin) {
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
                case "2" -> managePendingUsers(currentAdmin);
                case "3" -> handleUpdateUser(currentAdmin);
                case "4" -> displayAuditLogsMenu(currentAdmin);
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

    private void managePendingUsers(User currentAdmin) {
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

    private void handleUpdateUser(User currentAdmin) {
        viewAllUsers();
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        int targetUserId = readIntInput("Enter target User ID (or 0 to cancel): ");
        if (targetUserId == 0) return;

        if (targetUserId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole();
        if (newRole.equals("CANCEL")) return;

        String newStatus = readValidStatus();
        if (newStatus.equals("CANCEL")) return;

        boolean success = userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus);
        if (success) {
            String desc = String.format("Admin [%d] updated User [%d] to Role: %s, Status: %s",
                    currentAdmin.id(), targetUserId, newRole, newStatus);
            logDao.logAction(currentAdmin.id(), "USER_UPDATED", desc);
            System.out.println("User successfully updated and logged!");
        } else {
            System.out.println("Failed to update user in the database.");
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (ADMIN, STAFF, CUSTOMER): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0")) return "CANCEL";
            if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("CUSTOMER")) return role;
            System.out.println("Invalid role. Please provide a valid role.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED, PENDING): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0")) return "CANCEL";
            if (status.equals("APPROVED") || status.equals("PENDING")) return status;
            System.out.println("Invalid status. Please enter APPROVED or PENDING.");
        }
    }

    private void displayAuditLogsMenu(User currentAdmin) {
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
                case "3" -> handleClearLogs(currentAdmin);
                case "4" -> { return; }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleClearLogs(User currentAdmin) {
        System.out.print("Are you sure you want to clear all historical audit logs? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if ("Y".equals(confirm)) {
            if (logDao.clearAllLogs()) {
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
                    log.id(), log.userId(), log.actionType(), log.description(), log.timestamp().toString());
        }
        System.out.println("-".repeat(95));
    }


    // ==========================================
    // 2. STAFF DASHBOARD
    // ==========================================
    private void runStaffDashboard(User currentStaff) {
        while (true) {
            System.out.println("\n========================================================");
            System.out.println("          STAFF DASHBOARD - FOOD SERVICE SYSTEM         ");
            System.out.println(" Staff ID: " + currentStaff.id() + " | Logged in as: " + currentStaff.username());
            System.out.println("========================================================");
            System.out.println("1. View Active Orders Dashboard");
            System.out.println("2. Update Order Processing Status");
            System.out.println("3. View System Summary & Stats");
            System.out.println("4. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> renderActiveOrdersDashboard();
                case "2" -> handleUpdateOrderStatus(currentStaff);
                case "3" -> renderStaffStats();
                case "4" -> {
                    System.out.println("Logging out from Staff Dashboard...");
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void renderActiveOrdersDashboard() {
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("                   ACTIVE FOOD ORDERS DASHBOARD                   ");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-8s | %-12s | %-25s | %-10s | %-10s%n", "Order ID", "Customer ID", "Details", "Amount", "Status");
        System.out.println("-".repeat(75));
        System.out.printf("%-8d | %-12d | %-25s | P%-9.2f | %-10s%n", 101, 3, "Burger & Fries Combo", 250.00, "PENDING");
        System.out.printf("%-8d | %-12d | %-25s | P%-9.2f | %-10s%n", 102, 5, "Iced Tea & Pasta", 180.00, "PREPARING");
        System.out.println("-".repeat(75));
    }

    private void renderStaffStats() {
        System.out.println("\n--- STAFF WORKFLOW SUMMARY ---");
        System.out.println("Shift Status       : ACTIVE");
        System.out.println("Assigned Duty      : Order Processing & Status Tracking");
    }

    private void handleUpdateOrderStatus(User currentStaff) {
        renderActiveOrdersDashboard();
        System.out.println("\n--- UPDATE ORDER STATUS ---");
        int orderId = readIntInput("Enter Order ID to update (or 0 to cancel): ");
        if (orderId == 0) return;

        System.out.print("Enter current status (PENDING / PREPARING / READY): ");
        String oldStatus = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter new status (PREPARING / READY / COMPLETED / CANCELLED): ");
        String newStatus = scanner.nextLine().trim().toUpperCase();

        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                String desc = String.format("Updated Order #%d status from %s to %s", orderId, oldStatus, newStatus);
                logDao.logAction(currentStaff.id(), "ORDER_STATUS_UPDATED", desc);
                System.out.println("Order #" + orderId + " updated to " + newStatus + " and logged successfully!");
            } else {
                System.out.println("Failed to update order status. Order ID not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
    }


    // ==========================================
    // 3. CUSTOMER DASHBOARD
    // ==========================================
    private void runCustomerDashboard(User currentCustomer) {
        while (true) {
            System.out.println("\n========================================================");
            System.out.println("        CUSTOMER PORTAL - FOOD SERVICE SYSTEM           ");
            System.out.println(" Customer ID: " + currentCustomer.id() + " | User: " + currentCustomer.username());
            System.out.println("========================================================");
            System.out.println("1. Browse Food Menu & Prices");
            System.out.println("2. Place New Order");
            System.out.println("3. Track My Order Status & History");
            System.out.println("4. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> renderFoodMenu();
                case "2" -> handlePlaceOrder(currentCustomer);
                case "3" -> renderCustomerOrderHistory();
                case "4" -> {
                    System.out.println("Logging out from Customer Portal...");
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void renderFoodMenu() {
        System.out.println("\n---------------------------------------------------------");
        System.out.println("                   AVAILABLE MENU ITEMS                  ");
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-5s | %-25s | %-10s%n", "Item", "Description", "Price");
        System.out.println("-".repeat(48));
        System.out.printf("%-5s | %-25s | P%-9.2f%n", "[1]", "Cheeseburger Deluxe", 120.00);
        System.out.printf("%-5s | %-25s | P%-9.2f%n", "[2]", "Chicken Rice Meal", 150.00);
        System.out.printf("%-5s | %-25s | P%-9.2f%n", "[3]", "Spaghetti Special", 110.00);
        System.out.printf("%-5s | %-25s | P%-9.2f%n", "[4]", "Iced Tea (Large)", 45.00);
        System.out.println("-".repeat(48));
    }

    private void handlePlaceOrder(User currentCustomer) {
        renderFoodMenu();
        System.out.println("\n--- PLACE NEW ORDER ---");
        System.out.print("Enter item description / details: ");
        String details = scanner.nextLine().trim();

        System.out.print("Enter total order amount (P): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered. Order cancelled.");
            return;
        }

        String sql = "INSERT INTO orders (customer_id, details, total_amount, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentCustomer.id());
            pstmt.setString(2, details);
            pstmt.setDouble(3, amount);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                String desc = String.format("Processed sale for Customer %d - Total: P%.2f", currentCustomer.id(), amount);
                logDao.logAction(currentCustomer.id(), "ORDER_CREATED", desc);
                System.out.println("Order submitted successfully! It is now pending staff preparation.");
            } else {
                System.out.println("Failed to submit order.");
            }
        } catch (SQLException e) {
            System.err.println("Error processing transaction: " + e.getMessage());
        }
    }

    private void renderCustomerOrderHistory() {
        System.out.println("\n---------------------------------------------------------");
        System.out.println("               MY ORDER STATUS & HISTORY                 ");
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-8s | %-25s | %-10s | %-10s%n", "Order ID", "Details", "Total", "Status");
        System.out.println("-".repeat(60));
        System.out.printf("%-8d | %-25s | P%-9.2f | %-10s%n", 101, "Cheeseburger Deluxe", 120.00, "PENDING");
        System.out.println("-".repeat(60));
    }

    // Utility helper for safe integer inputs
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
}
package Ui;

import dao.UserDao;
import Model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewPendingUsers();
                    break;
                case "3":
                    approveUserFlow();
                    break;
                case "4":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        printUserTable(users, "ALL SYSTEM USERS");
    }

    private void viewPendingUsers() {
        List<User> users = userDao.getPendingUsers();
        printUserTable(users, "PENDING REGISTRATIONS");
    }

    private void approveUserFlow() {
        List<User> pendingUsers = userDao.getPendingUsers();
        if (pendingUsers.isEmpty()) {
            System.out.println("\nNo pending user registrations found.");
            return;
        }

        printUserTable(pendingUsers, "PENDING REGISTRATIONS");

        System.out.print("Enter Target User ID to Approve (or enter 0 to cancel): ");
        String input = scanner.nextLine();

        int targetId;
        try {
            targetId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Cancelled.");
            return;
        }

        if (targetId == 0) {
            System.out.println("Cancelled.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(u -> u.getId() == targetId);
        if (!exists) {
            System.out.println("Error: User ID does not exist in the pending list.");
            return;
        }

        boolean success = userDao.approveUser(targetId);
        if (success) {
            System.out.println("Success: User account ID " + targetId + " status updated to 'APPROVED'.");
        } else {
            System.out.println("Failed to update status.");
        }
    }

    private void printUserTable(List<User> users, String title) {
        System.out.println("\n--- " + title + " ---");
        String format = "%-5s | %-15s | %-12s | %-10s%n";
        System.out.printf(format, "ID", "Username", "Role", "Status");
        System.out.println("-------------------------------------------------------");
        if (users.isEmpty()) {
            System.out.println("No records found.");
        } else {
            for (User u : users) {
                System.out.printf(format, u.getId(), u.getUsername(), u.getRole(), u.getStatus());
            }
        }
        System.out.println("-------------------------------------------------------");
    }
}
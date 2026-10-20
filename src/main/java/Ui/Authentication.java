package Ui;

import dao.UserDao;
import Model.User;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== FOOD SERVICE TRACKING ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegistration();
                    break;
                case "3":
                    System.out.println("Exiting application...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);
        if (user == null) {
            System.out.println("Invalid credentials.");
            return;
        }

        if (!"APPROVED".equalsIgnoreCase(user.getStatus())) {
            System.out.println("Access Denied: Account status is '" + user.getStatus() + "'. Await admin approval.");
            return;
        }

        System.out.println("Login Successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
        routeUser(user.getRole());
    }

    private void handleRegistration() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Options prompt updated: TEACHER and STUDENT removed
        System.out.print("Enter role (ADMIN, STAFF, CASHIER): ");
        String role = scanner.nextLine().toUpperCase();

        boolean success = userDao.registerUser(username, password, role);
        if (success) {
            System.out.println("Registration successful! Account status set to PENDING.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private void routeUser(String role) {
        if ("ADMIN".equals(role)) {
            AdminMenu adminMenu = new AdminMenu(userDao, scanner);
            adminMenu.displayMenu();
        } else {
            System.out.println("Routing to " + role + " dashboard...");
        }
    }
}
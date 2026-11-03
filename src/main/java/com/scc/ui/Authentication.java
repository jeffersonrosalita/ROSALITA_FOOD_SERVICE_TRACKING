package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.Model.User;

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

            if ("ADMIN".equals(user.role().toUpperCase())) {
                AdminMenu adminMenu = new AdminMenu(userDao, scanner, user);
                adminMenu.displayMenu();
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

        boolean success = userDao.registerUser(username, password, "CUSTOMER");
        if (success) {
            System.out.println("Registration successful! Please wait for admin approval.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }
}
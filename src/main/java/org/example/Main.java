
package org.example;

import Model.User;
import dao.DatabaseConnection;
import dao.UserDao;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {
        try (var connection = DatabaseConnection.getConnection()) {
            System.out.println(
                    "Food Service Tracking System database connected!"
            );
            mainMenu();
        } catch (SQLException e) {
            System.out.println(
                    "Database connection failed: " + e.getMessage()
            );
        }
    }

    private static void mainMenu() {
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

    private static void register() {
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

        String roleChoice = sc.nextLine().trim();

        String role = switch (roleChoice) {
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
                System.out.println(
                        "Your account may require administrator approval."
                );
            } else {
                System.out.println("Registration failed.");
            }
        } catch (SQLException e) {
            String message = e.getMessage();

            if (message != null
                    && message.toLowerCase().contains("unique")) {
                System.out.println("Username already exists.");
            } else {
                System.out.println(
                        "Registration failed: " + message
                );
            }
        }
    }

    private static void login() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Enter username: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        try {
            Optional<User> result = userDao.login(name, password);

            if (result.isEmpty()) {
                System.out.println(
                        "Invalid username or password, "
                                + "or your account is not approved."
                );
                return;
            }

            User user = result.get();

            System.out.println("\nLogin successful!");
            System.out.println("Welcome, " + user.name());
            System.out.println("Role: " + user.role());
            System.out.println("Status: " + user.status());

            userMenu(user);

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void userMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== FOOD SERVICE USER MENU ===");
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.println("3. Exit");
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
                case "3" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}

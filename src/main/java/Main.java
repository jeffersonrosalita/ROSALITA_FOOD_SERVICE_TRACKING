package Ui;

import dao.DatabaseInitializer;
import dao.UserDao;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize database and enable foreign keys
        DatabaseInitializer.initializeDatabase();

        // 2. Instantiate single shared instances of DAO and Scanner
        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        // 3. Delegate execution flow to Authentication start method
        Authentication auth = new Authentication(userDao, scanner);
        auth.start();
    }
}
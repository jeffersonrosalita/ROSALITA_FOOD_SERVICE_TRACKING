package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Food Service Tracking System...");

        // 1. Automatically initialize database and tables on startup
        DatabaseInitializer.initialize();

        // 2. Launch authentication & UI workflow
        Authentication auth = new Authentication();
        auth.start();
    }
}
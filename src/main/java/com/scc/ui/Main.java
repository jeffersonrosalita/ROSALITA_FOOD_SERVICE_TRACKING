package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        Authentication auth = new Authentication();
        auth.showAuthMenu();
    }
}
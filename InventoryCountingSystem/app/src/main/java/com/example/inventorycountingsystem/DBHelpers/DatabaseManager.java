package com.example.inventorycountingsystem.DBHelpers;

import android.content.Context;

public class DatabaseManager {
    private static DBHelper instance;

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }
}

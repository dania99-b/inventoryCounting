package com.example.inventorycountingsystem.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventorycountingsystem.DBHelpers.DBHelper;

public class SplashScreenActivity extends AppCompatActivity {
    // Splash screen display time in milliseconds
    private static final long SPLASH_SCREEN_DELAY = 3000; // 3 seconds
    public static DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB = new DBHelper(this);

        // ... (the rest of your SplashScreenActivity code)

        // Create a handler to delay the transition to the main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
                //System.out.println(sharedPreferences.getString());
// Check if a specific key exists in SharedPreferences
                if (sharedPreferences.contains("API_KEY")&& sharedPreferences.contains("API_SECRET")) {
                // Start the main activity
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);

                // Close this activity
                finish();
            }
                else {

                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);

                    // Close this activity
                    finish();}
            }
        }, SPLASH_SCREEN_DELAY);
    }
}

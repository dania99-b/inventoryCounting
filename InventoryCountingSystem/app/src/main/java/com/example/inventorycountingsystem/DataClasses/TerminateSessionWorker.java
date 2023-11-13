package com.example.inventorycountingsystem.DataClasses;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.inventorycountingsystem.Activities.LoginActivity;
import com.example.inventorycountingsystem.DBHelpers.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TerminateSessionWorker extends Worker {
    private DBHelper DB;
    public TerminateSessionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        DB = new DBHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Date currentDate = new Date();

        // Define the date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(currentDate);

        // Print the formatted date to the console or display it in a TextView
        System.out.println(formattedDate);
        if(DB.getLastSessionStatus().equals("Open")){
        Context applicationContext = getApplicationContext();
        SharedPreferences USER_INFO = applicationContext.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = USER_INFO.getString("USER_NAME", "");
        DB.updateFinalSessionRowStatus(userName,"Close");
        DB.printAllSession();
        SharedPreferences.Editor userInfoEditor = USER_INFO.edit();
        userInfoEditor.remove("USER_TOKEN");
        userInfoEditor.remove("USER_NAME");
        userInfoEditor.clear();
        userInfoEditor.apply();
        Intent loginIntent = new Intent(applicationContext, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        applicationContext.startActivity(loginIntent);
        Log.d(TAG, "work manager work successfully !! ");
        return Result.success();}
        return Result.failure();
}}

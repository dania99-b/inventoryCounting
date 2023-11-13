package com.example.inventorycountingsystem.Network;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
    public static Activity mCurrentActivity;
    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }
    public static void setCurrentActivity(Activity mCurrentActivity) {
        MyApplication.mCurrentActivity = mCurrentActivity;
    }
}
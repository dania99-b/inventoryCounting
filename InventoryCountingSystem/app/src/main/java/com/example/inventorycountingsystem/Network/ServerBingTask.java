package com.example.inventorycountingsystem.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class ServerBingTask {
    private final Context applicationContext;

    public ServerBingTask(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    public boolean isBingServerReachable() {
        try {
            // Try to reach the server within a specified timeout (in milliseconds).
            // You can adjust the timeout value as needed.
            if (InetAddress.getByName("erpnext.main").isReachable(5000)) {
                // The server is reachable.
                return true;
            } else {
                // The server is not reachable within the specified timeout.
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // An exception occurred, indicating that the server is not reachable.
            return false;
        }
    }

}


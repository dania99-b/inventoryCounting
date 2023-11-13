package com.example.inventorycountingsystem.Network;

import static com.example.inventorycountingsystem.Network.MyApplication.getCurrentActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.inventorycountingsystem.Activities.CountList;
import com.example.inventorycountingsystem.Activities.LoginActivity;
import com.example.inventorycountingsystem.Activities.MainActivity;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;

import javax.net.ssl.SSLException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class NetworkChangeListener extends BroadcastReceiver {
    private CountList activity; // Reference to your activity
    private String countName; // The count_name to be used in pagination
    private String countType;
    // Constructor to receive a reference to your activity and the count_name
    public NetworkChangeListener(CountList activity, String countName , String countType) {
        this.activity = activity;
        this.countName = countName;
        this.countType = countType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            // Network connection is restored, call the pagination method in your activity
            if (countType.equals("Material")) {
                try {
                    try {
                        activity.getCountItemPagination(countName,10);
                    } catch (SSLException e) {
                        e.printStackTrace();
                    }
                    activity.getCountWarehousePagination(countName,10);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (SSLException e) {
                    e.printStackTrace();
                }
            } else if (countType.equals("Asset")) {
                try {
                    activity.getCountAssetPagination(countName,10);
                    activity.getCountLocationPagination(countName,10);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (ConnectException e) {
                    // Handle the exception when there's no network connection
                    e.printStackTrace();
                    showNetworkErrorAlert(activity);
                } catch (SSLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showNetworkErrorAlert(final Activity activity) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Network Error")
                        .setMessage("There was an issue with the network connection.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
    }



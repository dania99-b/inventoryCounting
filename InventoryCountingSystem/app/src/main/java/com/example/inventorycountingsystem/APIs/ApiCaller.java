package com.example.inventorycountingsystem.APIs;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import org.chromium.net.CronetEngine;
import org.chromium.net.UrlRequest;

import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ApiCaller  implements ApiCall {

    public Pair<Integer, String> callApi(String urlWithParams, String requestMethod, Context activity, String requestBody, String auth) {
        HttpsUrl asyncTask = new HttpsUrl((ApiCall) this, requestMethod, activity,requestBody,auth);
        asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, urlWithParams);

        try {
            return asyncTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Handle interruption
            return new Pair<>(0, "Error: Task interrupted");

        } catch (ExecutionException e) {
            e.printStackTrace();
            // Handle execution exception
            return new Pair<>(0, e.getMessage());        }
    }

    @Override
    public Pair<Integer, String> processFinish(int responseCode, String response) {
        return new Pair<>(responseCode, response);

    }
}

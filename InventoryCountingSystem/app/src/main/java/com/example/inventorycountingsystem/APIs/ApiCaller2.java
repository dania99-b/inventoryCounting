package com.example.inventorycountingsystem.APIs;

import android.content.Context;
import android.os.AsyncTask;

import java.text.ParseException;

public class ApiCaller2 {
    public void callApi2(String urlWithParams, String requestMethod, Context activity, String requestBody, String auth, final ApiCall2 callback) {
        HttpsUrl2 asyncTask = new HttpsUrl2(new ApiCall2() {
            @Override
            public void onResult(int responseCode, String response) {
                // Handle the result using the callback
                callback.onResult(responseCode, response);
            }
        }, requestMethod, activity, requestBody, auth);

        asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, urlWithParams);
    }
}

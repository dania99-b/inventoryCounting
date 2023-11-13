package com.example.inventorycountingsystem.APIs;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsUrl2 extends AsyncTask<String, Integer, Pair<Integer, String>> {

    private final ApiCall2 delegate;
    private ProgressDialog progressDialog = null;
    public Context activity;
    public String requestBody;
    public  String authToken;

    String requestMethode;


    public HttpsUrl2(ApiCall2 delegate, String requestMethode, Context activity, String requestBody, String authToken) {
        this.delegate = delegate;
        this.requestMethode=requestMethode;
        this.activity = activity;
        this.requestBody=requestBody;
        this.authToken=authToken;
    }

    @Override
    protected Pair<Integer, String> doInBackground(String... urls) {

        String urlWithParams = urls[0];

        try {
            URL url = new URL(urlWithParams);
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier(new AllowAllHostnameVerifier());
            conn.setReadTimeout(9000);
            conn.setConnectTimeout(9000);
            conn.setRequestMethod(requestMethode);
            conn.setDoInput(true);
            if (authToken != null && !authToken.isEmpty()) {
                conn.setRequestProperty("Authorization", "token " + authToken);
            }

            // Set the request body if applicable
            if (requestBody != null && !requestBody.isEmpty()) {
                conn.setDoOutput(true);
                conn.getOutputStream().write(requestBody.getBytes());
            }

            conn.connect();
            int responseCode = conn.getResponseCode();
            System.out.println(responseCode);
            int startProgress = 20; // Start progress as a percentage
            int endProgress = 100;
            int progressRange = endProgress - startProgress;
            int currentProgress = calculateCurrentProgress(startProgress,endProgress); // Implement this method


            // Calculate the actual progress value within the specified range
            int progress = startProgress + (int)(progressRange * currentProgress / 100);
            publishProgress(progress);
            InputStream inputStream = (responseCode == HttpsURLConnection.HTTP_OK) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return new Pair<>(responseCode, response.toString());
        } catch (ConnectException e) {
            // Handle the connection exception, e.g., show an error message to the user
            e.printStackTrace();
            try {
                throw e;
            } catch (ConnectException ex) {
                ex.printStackTrace();
            }
            // You can return a specific error code or message for your custom handling
            return new Pair<>(0, "Connection error: " + e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            return new Pair<>(0, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if ((Activity) activity != null && !((Activity) activity).isFinishing()) {

            // ((Activity) activity).runOnUiThread(new Runnable() {


        //    progressDialog = new ProgressDialog(activity);
          //  progressDialog.setMessage("Working...");
         //   progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
          //  progressDialog.setIndeterminate(false);
           // progressDialog.setMax(100);
          //  progressDialog.setProgress(0);
           // try {
           //     progressDialog.show();
           // } catch (WindowManager.BadTokenException e) {
                // Handle the exception (e.g., Activity is no longer valid)
        //    }
        }
    }

    protected void onPostExecute(Pair<Integer, String> result) {
        Log.d("AsyncTask", "onPostExecute started");
        int responseCode = result.first;
        String responseContent = result.second;
        // Delegate the response handling to the calling activity
        delegate.onResult(responseCode, responseContent);
        //  if (progressDialog != null && progressDialog.isShowing()) {
        // progressDialog.dismiss();
        // }
        Log.d("AsyncTask", "processedResult: " + "responseCode " + responseCode + " response " + responseContent);
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values.length > 0) {
            int progress = values[0];
            progressDialog.setProgress(progress);
        }

    }

   /* @Override
    protected void onCancelled() {
        super.onCancelled();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Log.d("AsyncTask", "Task was canceled.");


    }
*/
   private int calculateCurrentProgress(long downloadedBytes, long totalBytes) {
       if (totalBytes <= 0) {
           // Avoid division by zero and return 0 if totalBytes is not known or zero.
           return 0;
       }

       // Calculate the progress as a percentage.
       int progress = (int) ((downloadedBytes * 100) / totalBytes);

       // Ensure the progress doesn't exceed 100%.
       return Math.min(100, progress);
   }
}

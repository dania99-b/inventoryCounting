package com.example.inventorycountingsystem.DataClasses;

import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.inventorycountingsystem.APIs.ApiCaller;
import com.example.inventorycountingsystem.Activities.CountList;
import com.example.inventorycountingsystem.Activities.LoginActivity;
import com.example.inventorycountingsystem.Network.ServerBingTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SyncFromDeviceWorker extends Worker {
    private Context applicationContext;

    public SyncFromDeviceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.applicationContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences SyncerTokenData = applicationContext.getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
        String api_key = SyncerTokenData.getString("SYNCER_KEY", "");
        String api_secret = SyncerTokenData.getString("SYNCER_SECRET", "");

        ServerBingTask serverBingTask = new ServerBingTask(applicationContext);
        if(!api_key.isEmpty() && !api_secret.isEmpty() && serverBingTask.isBingServerReachable()) {
    try {
        System.out.println("network yes and bing yes ");
        performCheckSync();
        performSync();
    }
    catch (JSONException e) {
        e.printStackTrace();
    }
    catch (ParseException e) {
        e.printStackTrace();
    }
    DB.printStockCountingTransactionTable();
            return Result.success();

}
        else return Result.failure(); // Worker failed to execute

    }


    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    String hex = Integer.toHexString(b & 0xFF);
                    if (hex.length() == 1)
                        hex = "0".concat(hex);
                    res1.append(hex.concat(":"));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    private void performSync() throws JSONException {
        SharedPreferences sharedPreferencessettings = applicationContext.getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = sharedPreferencessettings.getString("IP_ADDRESS", "");
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        SharedPreferences SyncerTokenData = applicationContext.getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
        String api_key= SyncerTokenData.getString("SYNCER_KEY", "");
        String api_secret=  SyncerTokenData.getString("SYNCER_SECRET", "");
        List<StockCountingTransaction> transactions = DB.getUnsyncStockCountingTransactions(0, getMacAddr());
        if(!transactions.isEmpty()){
        System.out.println("ttablllee synnccc");
        DB.printStockCountingTransactionTable();

        String url = ipAddress + "insert_material_transactions";
        JSONArray jsonArray = new JSONArray();
        JSONObject requestBody = new JSONObject();

        for (StockCountingTransaction transaction : transactions) {
            JSONObject jsonTransaction = new JSONObject();

            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            String formattedQuantity = decimalFormat.format(transaction.getQuantity());
            String site = null;
            if (transaction.getLocation_id() != null)
                site = transaction.getLocation_id();
            else if (transaction.getWarehouse_id() != null)
                site = transaction.getWarehouse_id();

            try {
                System.out.println("uuurrrlllll");
                System.out.println(url);
                jsonTransaction.put("device_transaction_id", transaction.getId());
                jsonTransaction.put("parent_stock_count", transaction.getCount_name());
                jsonTransaction.put("item_code", transaction.getItem_code());
                jsonTransaction.put("quantity", formattedQuantity);
                jsonTransaction.put("scan_date_time", transaction.getPosting_date_time());
                jsonTransaction.put("item_site", transaction.getItem_site());
                jsonTransaction.put("site", site);
                jsonTransaction.put("counter_name", transaction.getConter_name());
                jsonTransaction.put("is_corrective", transaction.getIs_corrective());
                jsonTransaction.put("stage", transaction.getStage());
                jsonTransaction.put("type", transaction.getType());
                jsonTransaction.put("sync_date_time", getCurrentTime());

                jsonArray.put(jsonTransaction);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        try {
            requestBody.put("user", userName);
            requestBody.put("device_mac", getMacAddr());
            requestBody.put("data", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Cursor cursor = DB.getAllUsers();

        if (cursor.moveToFirst()) {
            do {

                String authorizationHeader = api_key + ":" + api_secret;
                System.out.println(authorizationHeader);

                System.out.println(requestBody);

                ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        ApiCaller apiCaller = new ApiCaller();
                        System.out.println(requestBody);
                        System.out.println(String.valueOf(requestBody));
                        Pair<Integer, String> result = apiCaller.callApi(url, "POST", applicationContext, String.valueOf(requestBody), authorizationHeader);
                        JSONObject jsonObject = new JSONObject(result.second);
                        if (result.first == 200) {
                            JSONObject insert_response = jsonObject.getJSONObject("message");
                            String job_id = insert_response.getString("job_id");
                            Job job = new Job();
                            job.setId(job_id);
                            System.out.println("job id from APIIIIIIIIIIIIIIIII");
                            System.out.println(job_id);
                            System.out.println("job id from GET IDDDDDDDDDDDDDDD");
                            System.out.println(job.getId());
                            DB.insertNewJob(insert_response.getString("job_id"), "Initialize");
                            //tomorrow for check API
                            for (StockCountingTransaction transaction : transactions) {
                                if (transaction.getIs_corrective() == 1 || transaction.getIs_corrective() == 0 || transaction.getIs_corrective() == 3) {
                                    DB.updateJobId(transaction, job.getId());
                                    //   DB.updateSyncStatus(transaction,getCurrentTime()); // Update sync status here
                                    System.out.println(transaction.getSync_time());
                                    System.out.println(transaction.getJob_id());
                                    //Toast.makeText(applicationContext, "Inserted in database", Toast.LENGTH_LONG).show();
                                }
                            }

                        }
                       /* else {

                            Logs log = new Logs();
                            log.setMethod(url);
                            log.setErrorMessage(result.second);
                            log.setUsername(userName);
                            log.setParameter(requestBody.toString());
                            try {
                                log.setTime(getCurrentTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            log.setResponseCode(result.first);
                            DB.insertLog(log.getMethod(), log.getTime(), log.getErrorMessage(), log.getUsername(), log.getPage(),log.getResponseCode());

                     */
                    }

                }
            }

            while (cursor.moveToNext());
        }}
        else {
            System.out.println("There is no ne Data to sync");
    }}
    public String getCurrentTime() throws ParseException {
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("USER_NAME", "");
        long loginTimeElapsed = sharedPreferences.getLong("TIMEELAPSED", 0); // 0 is the default value
        String lastOpenDate = DB.getLastSessionDate(username);

        // Check if lastOpenDate is null before parsing
        if (lastOpenDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date serverDate = dateFormat.parse(lastOpenDate);

            long currentElapsedTime = SystemClock.elapsedRealtime();
            long timeDifferenceInMillis = currentElapsedTime - loginTimeElapsed;
            Date updatedDate = new Date(serverDate.getTime() + timeDifferenceInMillis);
            String formattedUpdatedDate = dateFormat.format(updatedDate);
            return formattedUpdatedDate;
        } else {
            // Handle the case where lastOpenDate is null
            // You can return a default value or handle the error as needed
            return "No date available";
        }
    }


    private void performCheckSync() throws JSONException, ParseException {
        SharedPreferences SettingData = applicationContext.getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        SharedPreferences SyncerTokenData = applicationContext.getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
        String api_key= SyncerTokenData.getString("SYNCER_KEY", "");
        String api_secret=  SyncerTokenData.getString("SYNCER_SECRET", "");
        String authorizationHeader = api_key + ":" + api_secret;
        DB.printJob();
        List<String> jobs_id = DB.getJobsId();
        for (int i = 0; i < jobs_id.size(); i++) {
            String jobIdBytes = jobs_id.get(i);
            String jobId = new String(jobIdBytes.getBytes(), StandardCharsets.UTF_8);
            String url =ipAddress+"check_job_status";
            String urlWithParameter = url + "?jobId=" + jobId;
            System.out.println("{{{{{{{{{{{{{{{{{{{{{job}}}}}}}}}}}}}}}}}}");
            System.out.println(urlWithParameter);
            ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameter, "POST", applicationContext, null, authorizationHeader);
                    JSONObject jsonObject = new JSONObject(result.second);
                    if (result.first == 200)
                    {
                        JSONObject insert_response = jsonObject.getJSONObject("message");
                        JSONArray failed_records_id = insert_response.getJSONArray("successed_records");
                        List<String> successedRecordsList = new ArrayList<>();

                        for (int ii = 0; ii < failed_records_id.length(); ii++) {
                            String recordId = failed_records_id.getString(ii);
                            successedRecordsList.add(recordId);
                        }

                        String status = insert_response.getString("job_status");
                        DB.updateJobStatus(jobs_id.get(i), status);
                        DB.updateSyncStatus(jobs_id.get(i), successedRecordsList);
                        Logs log = new Logs();
                        log.setMethod(url);
                        log.setStatus(result.second);
                        log.setUsername(userName);
                        log.setParameter(jobId.toString());
                        try {
                            log.setTime(getCurrentTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), log.getUsername(), log.getPage(),log.getResponseCode());

                    }
                    else{
                        Logs log = new Logs();
                        log.setMethod(url);
                        log.setErrorMessage(result.second);
                        log.setUsername(userName);
                        log.setParameter(jobId.toString());
                        try {
                            log.setTime(getCurrentTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), log.getUsername(), log.getPage(),log.getResponseCode());

                    }


                }
            }
        }
    }
}

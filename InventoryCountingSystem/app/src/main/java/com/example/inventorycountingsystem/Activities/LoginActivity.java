package com.example.inventorycountingsystem.Activities;


import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.inventorycountingsystem.APIs.ApiCall;
import com.example.inventorycountingsystem.APIs.ApiCall2;
import com.example.inventorycountingsystem.APIs.ApiCaller2;
import com.example.inventorycountingsystem.APIs.HttpsUrl;
import com.example.inventorycountingsystem.APIs.ApiCaller;
import com.example.inventorycountingsystem.DBHelpers.DBHelper;
import com.example.inventorycountingsystem.DataClasses.Logs;
import com.example.inventorycountingsystem.DataClasses.Session;
import com.example.inventorycountingsystem.DataClasses.StockCountingTransaction;
import com.example.inventorycountingsystem.DataClasses.SyncBackendWorker;
import com.example.inventorycountingsystem.DataClasses.SyncFromDeviceWorker;
import com.example.inventorycountingsystem.DataClasses.TerminateSessionWorker;
import com.example.inventorycountingsystem.DataClasses.User;
import com.example.inventorycountingsystem.Network.MyApplication;
import com.example.inventorycountingsystem.R;

import org.chromium.net.CronetEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    //UI variable
    EditText _userNameET;
    EditText _passwordET;
    Button _loginBTN;
    TextView _url;
    private boolean isLoading = false; // Flag to prevent multiple API calls at once
    int page = 0;
    int page_max = -1;
    CountList countList = new CountList();
    String pageSize = String.valueOf(1000);

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MyApplication.setCurrentActivity(LoginActivity.this);
        _userNameET = findViewById(R.id.usernameET);
        _passwordET = findViewById(R.id.passwordET);
        _loginBTN = findViewById(R.id.loginBTN);
        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        //MainActivity.ReCheckTime = SettingData.getLong("ReCheckTime",10000);

        //check if the user already logged in
        SharedPreferences USER_INFO = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String oldUserID = USER_INFO.getString("USER_NAME", "");
        String oldUserToken = USER_INFO.getString("USER_TOKEN", "");


//Automatic Sync From this Device

        String currentActivityName = getComponentName().getClassName();
        Class<?> activityClass = null;
        try {
            activityClass = Class.forName(currentActivityName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Activity currentActivity = null;
        try {
            currentActivity = (Activity) activityClass.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        _loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = _userNameET.getText().toString();
                String password = _passwordET.getText().toString();

                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Username and Password are required.", Toast.LENGTH_LONG).show();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 100, 100, 100};
                    vibrator.vibrate(pattern, -1);
                } else {
                    SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
                    String ipAddress = SettingData.getString("IP_ADDRESS", "");
                    long reCheckTime = SettingData.getLong("ReCheckTime", 0);
                    String urlStr = "https://erpnext.main/api/method/stock_count.login.login";
                    // Build the complete URL with query parameters
                    String urlWithParams = urlStr + "?usr=" + userName + "&pwd=" + password;
                    JSONObject requestBody = new JSONObject();
                    // Checking network
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
                    if (connectivityManager != null) {
                        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                            try {
                                ApiCaller2 apiCaller2 = new ApiCaller2();
                                apiCaller2.callApi2(urlWithParams, "POST", LoginActivity.this, null, null, new ApiCall2() {
                                    @Override
                                    public void onResult(int responseCode, String response) {
                                        try {
                                            if (responseCode == 200) {
                                                // Process a successful response
                                                JSONObject jsonObject = new JSONObject(response);

                                                DB.deleteAnotherDeviceStockCountingTransaction(getMacAddr());
                                                String message = jsonObject.getString("message");
                                                JSONObject messageObject = new JSONObject(message);
                                                long currentElapsedTime = SystemClock.elapsedRealtime();
                                                if (messageObject.has("user")) {
                                                    JSONObject user = messageObject.getJSONObject("user");
                                                    String user_name = user.getString("username");
                                                    String api_key = user.getString("api_key");
                                                    String api_secret = messageObject.getString("api_secret");
                                                    String open_date = messageObject.getString("server_date_time");

                                                    User user1 = new User();
                                                    user1.setUser_name(user_name);
                                                    user1.setApi_key(api_key);
                                                    user1.setApi_secret(api_secret);
                                                    Session session = new Session();
                                                    session.setCounter_name(user_name);
                                                    session.setOpen_date(open_date);
                                                    session.setStatus("Open");
                                                    session.setApi_key(api_key);
                                                    session.setApi_secret(api_secret);
                                                    DB.insertSession(session.getCounter_name(), session.getOpen_date(), session.getStatus(), session.getApi_key(), session.getApi_secret());
                                                    DB.insertOrUpdateUser(user1.getUser_name(), user1.getApi_key(), user1.getApi_secret());
                                                    // DB.printAllSession();
                                                    SharedPreferences USER_INFO = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor userInfoEditor = USER_INFO.edit();
                                                    userInfoEditor.putString("USER_NAME", user_name);
                                                    userInfoEditor.putString("API_SECRET", api_secret);
                                                    userInfoEditor.putString("API_KEY", api_key);
                                                    userInfoEditor.putString("OPEN_DATE", open_date);
                                                    userInfoEditor.putLong("TIMEELAPSED", currentElapsedTime);
                                                    userInfoEditor.apply();
                                                    //Sync From Backend After Login
                                                    System.out.println("traaannnssaction");
                                                    System.out.println(DB.getRecordCount());
                                                    //  DB.printStockCountingTransactionTable();
                                                    storeDataFromOtherDevice();
                                                    Logs log = new Logs();
                                                    log.setMethod(urlWithParams);
                                                    log.setStatus(response);
                                                    log.setUsername(userName);
                                                    log.setParameter(urlWithParams);
                                                    log.setTime(getCurrentTime());
                                                    log.setPage(page);
                                                    log.setResponseCode(responseCode);
                                                    DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), log.getUsername(), log.getPage(), log.getResponseCode());

                                                    // Save the tokens to MainActivity
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    intent.putExtra("USER_ID", user_name);
                                                    intent.putExtra("API_KEY", api_key);
                                                    intent.putExtra("API_SECRET", api_secret);
                                                    startActivity(intent);
                                                    // Continue processing the response and any other actions

                                                }
                                            } else if (responseCode == 401) {
                                                // Handle 401 Unauthorized response
                                                // ...

                                            } else if (responseCode == 500) {
                                                // Handle 500 Internal Server Error response
                                                // ...

                                            } else {
                                                // Handle other response codes or errors
                                                // ...
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Handle other exceptions here
                            }
                        }}}}});}




           private void storeDataFromOtherDevice() throws JSONException {
        if (isLoading) {
            // Don't load more data if an API call is already in progress
            return;
        }
        isLoading = true;
        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        SharedPreferences sharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        String api_secret = sharedPreferences.getString("API_SECRET", "");
        String api_key = sharedPreferences.getString("API_KEY", "");
        String baseUrl = ipAddress + "back_sync_record";
        String authorizationHeader = api_key + ":" + api_secret;
        int page = 0;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

            // Delete all existing records before syncing
          DB.deleteAnotherDeviceStockCountingTransaction(countList.getMacAddr());

            while (true) {
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    String urlWithParameters = baseUrl + "?counter_name=" + userName +
                            "&device_mac=" + getMacAddr() +
                            "&page=" + page +
                            "&page_size=" + pageSize;

                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameters, "GET", LoginActivity.this, null, authorizationHeader);

                    if (result.first == 200) {
                        try {
                            JSONObject jsonObject = new JSONObject(result.second);
                            JSONObject recordsObject = jsonObject.getJSONObject("message");
                            JSONArray recordsArray = recordsObject.getJSONArray("records");
                            int page_max = Integer.parseInt(recordsObject.getString("max_page"));

                            for (int i = 0; i < recordsArray.length(); i++) {
                                StockCountingTransaction transaction = new StockCountingTransaction();
                                JSONObject oneRecord = recordsArray.getJSONObject(i);
                                String stockCount_name = oneRecord.getString("parent_stock_count");
                                String item_code = oneRecord.getString("item_code");
                                String quantity = oneRecord.getString("quantity");
                                String scan_datetime = oneRecord.getString("scan_date_time");
                                String counter_name = oneRecord.getString("counter_name");
                                int is_corrective = oneRecord.getInt("is_corrective");
                                String stage = oneRecord.getString("stage");
                                String item_site = oneRecord.getString("item_site");
                                String place = oneRecord.getString("site");
                                String type = oneRecord.getString("type");
                                String sync_datetime = oneRecord.getString("scan_date_time");
                                String device_mac1 = oneRecord.getString("device_mac");
                                transaction.setCount_name(stockCount_name);
                                transaction.setItem_code(item_code);
                                transaction.setQuantity(Double.parseDouble(quantity));
                                transaction.setPosting_date_time(scan_datetime);
                                transaction.setConter_name(counter_name);
                                transaction.setIs_corrective(is_corrective);
                                transaction.setStage(stage);
                                transaction.setItem_site(item_site);
                                if (item_site.equals("Location")) {
                                    transaction.setLocation_id(place);
                                }
                                if (item_site.equals("Warehouse")) {
                                    transaction.setWarehouse_id(place);
                                }
                                transaction.setType(type);
                                transaction.setSync_time(sync_datetime);
                                transaction.setDevice_mac(device_mac1);

                                DB.insertStockCountingTransaction(transaction.getCount_name(), transaction.getItem_code(), transaction.getQuantity(), transaction.getPosting_date_time(), transaction.getConter_name(), transaction.getIs_corrective(), transaction.getStage(), transaction.getItem_site(), transaction.getWarehouse_id(), transaction.getLocation_id(), transaction.getType(), transaction.getSync_time(), transaction.getDevice_mac(), 0);
                            }

                            // Check if there are more pages to fetch
                            if (page < page_max) {
                                page++; // Increment page to fetch the next page
                            } else {
                                break; // Exit the loop when all pages have been fetched
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing errors
                        }
                    } else {
                        // Handle non-200 response code (e.g., error handling)
                        break; // Exit the loop on error
                    }
                } else {
                    // Handle network connectivity issues
                    break; // Exit the loop on connectivity issues
                }
            }
        }
    }


    public String getCurrentTime() throws ParseException {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuewithoutsync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingMenu:
                Intent i = new Intent(getApplicationContext(), ExternalSetting.class);
                startActivity(i);
        }
        return true;
    }
}




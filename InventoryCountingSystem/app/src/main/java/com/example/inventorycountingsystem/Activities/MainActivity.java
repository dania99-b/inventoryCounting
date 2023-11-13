package com.example.inventorycountingsystem.Activities;


import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.Volley;
import com.example.inventorycountingsystem.APIs.ApiCaller;
import com.example.inventorycountingsystem.DataClasses.CountName;
import com.example.inventorycountingsystem.DataClasses.Locations;
import com.example.inventorycountingsystem.DataClasses.Logs;
import com.example.inventorycountingsystem.DataClasses.SyncBackendWorker;
import com.example.inventorycountingsystem.DataClasses.SyncFromDeviceWorker;
import com.example.inventorycountingsystem.DataClasses.TerminateSessionWorker;
import com.example.inventorycountingsystem.DataClasses.Warehouse;
import com.example.inventorycountingsystem.Network.MyApplication;
import com.example.inventorycountingsystem.R;
import com.example.inventorycountingsystem.Network.NetworkChangeListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    //Connection status
    public static boolean weAreOnline;
    //onBackPressed
    private long pressedTime;
    private int _selectedItemId;
    //UI
    Button _transactionOutBTN;
    Button _viewUserCountsBTN;
    Button _viewUnCountedItemsBTN;
    Button _viewUnCountedForAssetBTN;
    Button _countViewBTN;
    Button _syncBTN;
    Button _logoutBTN;
    Button _fillDataBTN;
    Button _exportExcelBTN;
    Button _exportLogExcelBTN;
    Button _deleteDataBTN;
    Button _deleteOperationBTN;
    LoginActivity loginActivity = new LoginActivity();
    CountList countList=new CountList();

    // Public data
    public static String COUNT_NAME;
    public static String IP_ADDRESS;
    public static String USER_TOKEN;
    public static String USER_NAME;
    public static boolean LOCATION_IS_REQUIRED = false;
    public static String LOCATION_NAME = null;
    public static String ITEM_NATURE = null;
    public static String ITEM_NAME = null;
    // Accessing the saved data in another activity


    /**
     * connectionPattern[0]=1 => disConnected
     * connectionPattern[1]=1 => connected
     * if connected after disConnection => show dialog
     * so the dialog doesn't be shown on opening the app
     */

    public static int[] connectionPattern = {0, 0};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.setCurrentActivity(MainActivity.this);
        _viewUserCountsBTN = findViewById(R.id.viewUserCountBTN);
        _logoutBTN = findViewById(R.id.logoutBTN);
        _exportExcelBTN = findViewById(R.id.exportExcelBTN);
        _exportLogExcelBTN = findViewById(R.id.exportLogExcelBTN);
        SharedPreferences sharedPreferencessettings = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferencesuserinfo = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String user_name = sharedPreferencesuserinfo.getString("USER_NAME", "");
        String api_secret = sharedPreferencesuserinfo.getString("API_SECRET", "");
        String api_key = sharedPreferencesuserinfo.getString("API_KEY", "");
        String dns = sharedPreferencessettings.getString("IP_ADDRESS", "");
        String open_date = sharedPreferencesuserinfo.getString("OPEN_DATE", "");
        // Automatic Logout
        long sessionTerminationsecondWorkerDelay = 2;
        long BackendSncsecondWorkerDelay = 2;
        long DeviceSyncDelayMinutes = 2;
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkRequest sessionTerminationRequest = new PeriodicWorkRequest.Builder(
                TerminateSessionWorker.class,
                30, TimeUnit.MINUTES // Repeat interval
        ).setInitialDelay(sessionTerminationsecondWorkerDelay, TimeUnit.MINUTES)
                .build();

        // Automatic Sync From Device
        WorkRequest syncDeviceRequest = new PeriodicWorkRequest.Builder(
                SyncFromDeviceWorker.class, // Your Worker class
                15, TimeUnit.MINUTES
        ).setInitialDelay(DeviceSyncDelayMinutes, TimeUnit.MINUTES)// Repeat interval
         .setConstraints(constraints)
         .build();

        // Automatic Sync From Backend Database
        WorkRequest syncBackendRequest = new PeriodicWorkRequest.Builder(
                SyncBackendWorker.class,
                 18, TimeUnit.MINUTES
        ).setInitialDelay(BackendSncsecondWorkerDelay, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        //enqueue the both of workers
        WorkManager.getInstance(MainActivity.this).enqueueUniquePeriodicWork(
                "synceDevice",
                ExistingPeriodicWorkPolicy.KEEP,
                (PeriodicWorkRequest) syncDeviceRequest);
        WorkManager.getInstance(MainActivity.this).enqueueUniquePeriodicWork(
                "syncBackend",
                ExistingPeriodicWorkPolicy.KEEP,
                (PeriodicWorkRequest) syncBackendRequest);
        WorkManager.getInstance(MainActivity.this).enqueueUniquePeriodicWork(
                "terminateSession",
                ExistingPeriodicWorkPolicy.KEEP,
                (PeriodicWorkRequest) sessionTerminationRequest);

        try {
            getAllWarehouse(dns, api_key, api_secret);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            getAllLocation(dns, api_key, api_secret);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        _viewUserCountsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    callServerForUserCounts(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


        _logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                USER_TOKEN = null;
                USER_NAME = null;
                COUNT_NAME = null;
                //removing the tokens
                SharedPreferences USER_INFO = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
                SharedPreferences.Editor userInfoEditor = USER_INFO.edit();
                userInfoEditor.remove("USER_TOKEN");
                userInfoEditor.remove("USER_NAME");
                userInfoEditor.clear();
                userInfoEditor.apply();
                //update the last session status
                DB.updateFinalSessionRowStatus(user_name,"Close");

                //move to login screen
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        _exportExcelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createExcelFile();
                Toast.makeText(MainActivity.this, "Trasnactions Excel File Created", Toast.LENGTH_SHORT).show();
            }
        });
        _exportLogExcelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLogExcelFile();
                Toast.makeText(MainActivity.this, "Logs Excel File Created", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuewithoutsync, menu);
        return true;
    }

    //Dialogs functions
    private void getItemsDialog(int Offset) {

        //Check if the table is empty
        Cursor res = DB.getItemsData(Offset);
        if (res.getCount() == 0 && Offset == 0) {
            Toast.makeText(MainActivity.this, "The table is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (res.getCount() == 0 && Offset != 0) {
            Toast.makeText(MainActivity.this, "There are no more data", Toast.LENGTH_SHORT).show();
            return;
        }

        //grab the data from the table
        StringBuffer buffer = new StringBuffer();
        res.moveToFirst();
        for (int i = 0; i < res.getCount(); i++) {
            buffer.append("Name : " + res.getString(0) + "\n");
            buffer.append("Type : " + res.getString(1) + "\n");
            buffer.append("Location : " + res.getString(2) + "\n");
            buffer.append("Count name : " + res.getString(3) + "\n\n");

            res.moveToNext();
        }

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Grabbed items :");
        builder.setMessage(buffer.toString());

        // add the buttons
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                getItemsDialog(Offset + 20);
            }
        });

        builder.setNeutralButton("Previous", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                if (Offset == 0) {
                    Toast.makeText(MainActivity.this, "No previous page", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        if (res.getCount() < 20) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
        }

    }

    private void getUnCountedItemsDialog(String Count_name, int offset) {

        //Check if the table is empty
        Cursor res = DB.getUncountedItemsByCountName(Count_name, offset);
        res.moveToFirst();
        if (res.getCount() == 0 && offset == 0) {
            Toast.makeText(MainActivity.this, "There is no data, count is : " + res.getCount(), Toast.LENGTH_SHORT).show();
            res.close();
            return;
        }

        if (res.getCount() == 0 && offset != 0) {
            Toast.makeText(MainActivity.this, "There are no more data", Toast.LENGTH_SHORT).show();
            res.close();
            return;
        }

        //grab the data from the table
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < res.getCount(); i++) {
            buffer.append("Name : " + res.getString(1) + "\n");
            buffer.append("Location : " + res.getString(4) + "\n");
            buffer.append("Type : " + res.getString(2) + "\n\n");
            res.moveToNext();
        }

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Uncounted items :");
        builder.setMessage(buffer.toString());

        // add the buttons
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getUnCountedItemsDialog(Count_name, offset + 20);
            }
        });

        builder.setNeutralButton("Previous", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (offset == 0) {
                    Toast.makeText(MainActivity.this, "No previous page", Toast.LENGTH_SHORT).show();
                } else {
                    getUnCountedItemsDialog(Count_name, offset - 20);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        if (res.getCount() < 20) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
        }
        res.close();
    }

    // Method to display the dialog with clickable elements
    private void getCountnameDialog(String username, int offset) {
        Cursor res = DB.getSpecificUserStockingCounting(username);
        res.moveToFirst();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        if (res.getCount() == 0) {
            Toast.makeText(MainActivity.this, "No data available.", Toast.LENGTH_SHORT).show();
            res.close();
            return;
        }

        List<String> countNames = new ArrayList<>();
        for (int i = 0; i < res.getCount(); i++) {
            String countName = res.getString(0);
            countNames.add(countName);
            res.moveToNext();
        }

        res.close();

        View dialogView = getLayoutInflater().inflate(R.layout.countnamedialog, null);
        TextView textViewCountNames = dialogView.findViewById(R.id.textViewCountNames);
        Button buttonNext = dialogView.findViewById(R.id.buttonNext);
        Button buttonPrevious = dialogView.findViewById(R.id.buttonPrevious);

        AlertDialog dialog = builder.setView(dialogView).create();

        int pageSize = 5;
        int endIndex = Math.min(offset + pageSize, countNames.size());

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (int i = offset; i < endIndex; i++) {
            final String countName = countNames.get(i);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    try {
                        getCountInfo(countName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss(); // Close the dialog after clicking a count_name
                }
            };

            spannableStringBuilder.append(countName + "\n", clickableSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textViewCountNames.setText(spannableStringBuilder);
        textViewCountNames.setMovementMethod(LinkMovementMethod.getInstance());

        buttonPrevious.setOnClickListener(v -> {
            dialog.dismiss();
            int newOffset = Math.max(offset - pageSize, 0);
            getCountnameDialog(username, newOffset);
        });

        buttonNext.setOnClickListener(v -> {
            dialog.dismiss();
            int newOffset = offset + pageSize;
            if (newOffset < countNames.size()) {
                getCountnameDialog(username, newOffset);
            } else {
                Toast.makeText(MainActivity.this, "No more data", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


// Call the getCountnameDialog function initially with offset 0 when needed
// For example, you can call it in the button click event that triggers the dialog.


    private void getCountInfo(String countName) throws JSONException {
        try {
            SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
            String ipAddress = SettingData.getString("IP_ADDRESS", "");
            long reCheckTime = SettingData.getLong("ReCheckTime", 0);
            SharedPreferences sharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
            String userName = sharedPreferences.getString("USER_NAME", "");
            String api_secret = sharedPreferences.getString("API_SECRET", "");
            String api_key = sharedPreferences.getString("API_KEY", "");
            String url = ipAddress+"get_counting_info";
            String UrlWithParameter = url + "?count_name=" + countName;
            String authorizationHeader = api_key + ":" + api_secret;
            System.out.println(authorizationHeader);
            System.out.println("countinnnnggg infoooo");
            JSONObject requestBody = new JSONObject();
            System.out.println(countName);
            requestBody.put("count_name", countName);
            requestBody.put("page", 1);
            requestBody.put("page_size", 4);
            System.out.println(requestBody);
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(UrlWithParameter, "GET", MainActivity.this, null, authorizationHeader);
                    JSONObject jsonObject = new JSONObject(result.second);
                    if (result.first == 200) {
                        // Prepare the authorization header
                        JSONObject stock_countArray = jsonObject.getJSONObject("message");
                        Intent intent = new Intent(MainActivity.this, CountList.class);
                        intent.putExtra("response", stock_countArray.toString());
                        intent.putExtra("count_name", countName);
                        startActivity(intent);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //export to excel
    private void createExcelFile() {
        Workbook wb = new HSSFWorkbook();
        Cell cell = null;

        // Create sheet
        Sheet sheet = wb.createSheet("Demo Sheet name");

        // Create headers in the first row
        Row headerRow = sheet.createRow(0);
        cell = headerRow.createCell(0);
        cell.setCellValue("ID");
        cell = headerRow.createCell(1);
        cell.setCellValue("Count Name");
        cell = headerRow.createCell(2);
        cell.setCellValue("Item Code");
        cell = headerRow.createCell(3);
        cell.setCellValue("Counter Name");
        cell = headerRow.createCell(4);
        cell.setCellValue("Quantity");
        cell = headerRow.createCell(5);
        cell.setCellValue("Posting Date");
        cell = headerRow.createCell(6);
        cell.setCellValue("Is Corrective");
        cell = headerRow.createCell(7);
        cell.setCellValue("Stage");
        cell = headerRow.createCell(8);
        cell.setCellValue("Warehouse");
        cell = headerRow.createCell(9);
        cell.setCellValue("Location");
        cell = headerRow.createCell(10);
        cell.setCellValue("Movement Type");
        cell = headerRow.createCell(11);
        cell.setCellValue("Type");
        cell = headerRow.createCell(12);
        cell.setCellValue("Is Sync");

        // Set column widths
        sheet.setColumnWidth(0, 20 * 200);
        sheet.setColumnWidth(1, 30 * 200);
        sheet.setColumnWidth(2, 30 * 200);
        sheet.setColumnWidth(3, 30 * 200);
        sheet.setColumnWidth(4, 30 * 200);
        sheet.setColumnWidth(5, 30 * 200);
        sheet.setColumnWidth(6, 30 * 200);
        sheet.setColumnWidth(7, 30 * 200);
        sheet.setColumnWidth(8, 30 * 200);
        sheet.setColumnWidth(9, 30 * 200);
        sheet.setColumnWidth(10, 30 * 200);
        sheet.setColumnWidth(11, 30 * 200);
        sheet.setColumnWidth(12, 30 * 200);

        //getting the data from buffer to the cells
        Cursor res = DB.getStock_Counting_TransactionData();
        int rowNum = 1; // Start from the second row (index 1)
        while (res.moveToNext()) {
            Row dataRow = sheet.createRow(rowNum);
            cell = dataRow.createCell(0);
            cell.setCellValue(res.getString(0)); // Assuming "ID" is the 1st column (index 0)
            cell = dataRow.createCell(1);
            cell.setCellValue(res.getString(1)); // Assuming "Count Name" is the 2nd column (index 1)
            cell = dataRow.createCell(2);
            cell.setCellValue(res.getString(2)); // Assuming "Item Code" is the 3rd column (index 2)
            cell = dataRow.createCell(3);
            cell.setCellValue(res.getString(6)); // Assuming "Counter Name" is the 4th column (index 3)
            cell = dataRow.createCell(4);
            cell.setCellValue(res.getString(3));
            cell = dataRow.createCell(5);
            cell.setCellValue(res.getString(4));
            // Assuming "Posting Date" is the 6th column (index 5)
            cell = dataRow.createCell(6);
            cell.setCellValue(res.getString(7)); // Assuming "Is Corrective" is the 7th column (index 6)
            cell = dataRow.createCell(7);
            cell.setCellValue(res.getString(9)); // Assuming "Stage" is the 8th column (index 7)
            cell = dataRow.createCell(8);
            cell.setCellValue(res.getString(5)); // Assuming "Warehouse" is the 9th column (index 8)
            cell = dataRow.createCell(9);
            cell.setCellValue(res.getString(11));
            cell = dataRow.createCell(10);
            cell.setCellValue(res.getString(8));
            cell = dataRow.createCell(11);
            cell.setCellValue(res.getString(10));
            cell = dataRow.createCell(12);
            cell.setCellValue(res.getString(12));// Assuming "Location" is the 10th column (index 9)

            rowNum++;
        }

        // Save the Excel file
        try {
            // Get the app's internal files directory
            File saveDirectory = new File(getFilesDir(), "CountingSystem");
            // Create the directory if it doesn't exist
            if (!saveDirectory.exists()) {
                saveDirectory.mkdirs();
            }

            // Create the file
            File file = new File(saveDirectory, "Test.xls");

            FileOutputStream outputStream = new FileOutputStream(file);
            wb.write(outputStream);
            outputStream.close();

            Toast.makeText(getApplicationContext(), "Excel Created in " + file.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Not OK", Toast.LENGTH_LONG).show();
        }
    }


    private void createLogExcelFile() {
        Workbook wb = new HSSFWorkbook();
        Cell cell = null;

        // Create sheet
        Sheet sheet = wb.createSheet("Log Sheet");

        // Create headers in the first row
        Row headerRow = sheet.createRow(0);
        cell = headerRow.createCell(0);
        cell.setCellValue("ID");
        cell = headerRow.createCell(1);
        cell.setCellValue("Method");
        cell = headerRow.createCell(2);
        cell.setCellValue("Time");
        cell = headerRow.createCell(3);
        cell.setCellValue("Status");
        cell = headerRow.createCell(4);
        cell.setCellValue("Username");
        cell = headerRow.createCell(5);
        cell.setCellValue("Page");
        cell = headerRow.createCell(6);
        cell.setCellValue("Response Code");



        // Set column widths
        sheet.setColumnWidth(0, 20 * 200);
        sheet.setColumnWidth(1, 30 * 200);
        sheet.setColumnWidth(2, 30 * 200);
        sheet.setColumnWidth(3, 30 * 200);
        sheet.setColumnWidth(4, 30 * 200);
        sheet.setColumnWidth(5, 30 * 200);
        sheet.setColumnWidth(6, 30 * 200);


        //getting the data from buffer to the cells
        Cursor res = DB.getAllLog();
        int rowNum = 1; // Start from the second row (index 1)
        while (res.moveToNext()) {
            Row dataRow = sheet.createRow(rowNum);
            cell = dataRow.createCell(0);
            cell.setCellValue(res.getString(0)); // Assuming "ID" is the 1st column (index 0)
            cell = dataRow.createCell(1);
            cell.setCellValue(res.getString(1)); // Assuming "Count Name" is the 2nd column (index 1)
            cell = dataRow.createCell(2);
            cell.setCellValue(res.getString(2)); // Assuming "Item Code" is the 3rd column (index 2)
            cell = dataRow.createCell(3);
            cell.setCellValue(res.getString(3)); // Assuming "Counter Name" is the 4th column (index 3)
            cell = dataRow.createCell(4);
            cell.setCellValue(res.getString(4));
            cell = dataRow.createCell(5);
            cell.setCellValue(res.getString(5));
            // Assuming "Posting Date" is the 6th column (index 5)
            cell = dataRow.createCell(6);
            cell.setCellValue(res.getString(6)); // Assuming "Is Corrective" is the 7th column (index 6)


            rowNum++;
        }

        // Save the Excel file
        try {
            // Get the app's internal files directory
            File saveDirectory = new File(getFilesDir(), "CountingSystem");
            // Create the directory if it doesn't exist
            if (!saveDirectory.exists()) {
                saveDirectory.mkdirs();
            }

            // Create the file
            File file = new File(saveDirectory, "Logs.xls");

            FileOutputStream outputStream = new FileOutputStream(file);
            wb.write(outputStream);
            outputStream.close();

            Toast.makeText(getApplicationContext(), "Log Excel Created in " + file.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Not OK", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finishAffinity();
        } else {
            Toast.makeText(getBaseContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    //two overrides to get the setting menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingMenu:
                Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(i);
        }
        return true;
    }

    public void callServerForUserCounts(int offset) throws JSONException {
        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        SharedPreferences sharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        String api_secret = sharedPreferences.getString("API_SECRET", "");
        String api_key = sharedPreferences.getString("API_KEY", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        try {

            String url = ipAddress+"get_user_countings";
            String user = userName;
            // Build the complete URL with query parameters
            String UrlWithParameter = url + "?user=" + user;
            System.out.println(UrlWithParameter);
            // Prepare the authorization header
            String authorizationHeader = api_key + ":" + api_secret;
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(UrlWithParameter, "GET", MainActivity.this, null, authorizationHeader);
                    JSONObject jsonObject = new JSONObject(result.second);
                    if (result.first == 200) {
                        JSONArray stock_countArray = jsonObject.getJSONArray("message");
                        List<String> countNames = new ArrayList<>();

                        for (int i = 0; i < stock_countArray.length(); i++) {
                            JSONObject countObject = stock_countArray.getJSONObject(i);
                            CountName count = new CountName();
                            if (countObject.has("name") && !countObject.isNull("name")) {
                                count.setName(countObject.getString("name"));
                                System.out.println("Count name: " + count.getName());
                                count.setType(countObject.getString("type"));
                                count.setManual_item_check(countObject.getInt("manual_item_check"));
                                count.setManual_site_check(countObject.getInt("manual_site_check"));
                                // Insert the count name into the OFFLINE database
                                DB.insertCountName(count.getName(), count.getType(), count.getManual_item_check(), count.getManual_site_check());
                                DB.insertUserStockingCounting(userName, count.getName());
                                countNames.add(count.getName());
                            }
                        }
                        View dialogView = getLayoutInflater().inflate(R.layout.countnamedialog, null);
                        TextView textViewCountNames = dialogView.findViewById(R.id.textViewCountNames);
                        Button buttonNext = dialogView.findViewById(R.id.buttonNext);
                        Button buttonPrevious = dialogView.findViewById(R.id.buttonPrevious);
                        AlertDialog dialog = builder.setView(dialogView).create();

                        int pageSize = 5;
                        int endIndex = Math.min(offset + pageSize, countNames.size());

                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                        for (int i = offset; i < endIndex; i++) {
                            final String countName = countNames.get(i);

                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    try {
                                        getCountInfo(countName);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss(); // Close the dialog after clicking a count_name
                                }
                            };

                            spannableStringBuilder.append(countName + "\n", clickableSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        textViewCountNames.setText(spannableStringBuilder);
                        textViewCountNames.setMovementMethod(LinkMovementMethod.getInstance());

                        buttonPrevious.setOnClickListener(v -> {
                            dialog.dismiss();
                            int newOffset = Math.max(offset - pageSize, 0);
                            getCountnameDialog(userName, newOffset);
                        });

                        buttonNext.setOnClickListener(v -> {
                            dialog.dismiss();
                            int newOffset = offset + pageSize;
                            if (newOffset < countNames.size()) {
                                getCountnameDialog(userName, newOffset);
                            } else {
                                Toast.makeText(MainActivity.this, "No more data", Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (!MainActivity.this.isDestroyed()) {
                            dialog.show();
                        }

                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getAllWarehouse(String dns, String api_key, String api_secret) throws JSONException, ParseException {
        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        String url = ipAddress+"get_all_warehouse";
        JSONObject requestBody = new JSONObject();

        // Create the header with the existing Authorization token
        String authorizationHeader = api_key + ":" + api_secret;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                ApiCaller apiCaller = new ApiCaller();
                Pair<Integer, String> result = apiCaller.callApi(url, "GET", MainActivity.this, null, authorizationHeader);

                if (result.first == 200) {
                    JSONObject jsonObject = new JSONObject(result.second);
                    JSONArray warehouseArray = jsonObject.getJSONArray("message");
                    for (int i = 0; i < warehouseArray.length(); i++) {
                        JSONObject locationObject = warehouseArray.getJSONObject(i);
                        String warehouse_name = locationObject.getString("name");
                        String barcode = locationObject.getString("barcode");
                        Warehouse warehouse = new Warehouse();
                        warehouse.setName(warehouse_name);
                        warehouse.setBarcode(barcode);
                        DB.insertwarehouse(warehouse.getName(), warehouse.getBarcode());
                        Logs log = new Logs();
                        log.setMethod(url);
                        log.setStatus(result.second);
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), null, 0,log.getResponseCode());

                    }
                } else {
                    Logs log = new Logs();
                    log.setMethod(url);
                    log.setErrorMessage(result.second);
                    log.setTime(getCurrentTime());
                    log.setResponseCode(result.first);
                    DB.insertLog(log.getMethod(), log.getTime(), log.getErrorMessage(), null, 0,log.getResponseCode());

                }
            }
        }
    }

    private void getAllLocation(String dns, String api_key, String api_secret) throws JSONException, ParseException {
        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        String url = ipAddress+"get_all_location";
        JSONObject requestBody = new JSONObject();

        // Create the header with the existing Authorization token
        String authorizationHeader = api_key + ":" + api_secret;
        System.out.println(authorizationHeader);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                ApiCaller apiCaller = new ApiCaller();
                Pair<Integer, String> result = apiCaller.callApi(url, "GET", MainActivity.this, null, authorizationHeader);

                if (result.first == 200) {
                    JSONObject jsonObject = new JSONObject(result.second);
                    JSONArray locationArray = jsonObject.getJSONArray("message");
                    for (int i = 0; i < locationArray.length(); i++) {
                        JSONObject locationObject = locationArray.getJSONObject(i);
                        String location_name = locationObject.getString("name");
                        String barcode = locationObject.getString("barcode_location");
                        Locations locations = new Locations();
                        locations.setLocation_name(location_name);
                        locations.setBarcode(barcode);
                        DB.insertLocationsData(locations.getLocation_name(), locations.getBarcode());
                        Logs log = new Logs();
                        log.setMethod(url);
                        log.setStatus(result.second);
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), null, 0,log.getResponseCode());

                    }
                }
                if (result.first == 417) {
                    Logs log = new Logs();
                    log.setMethod(url);
                    log.setErrorMessage(result.second);
                    log.setTime(loginActivity.getCurrentTime());
                    log.setResponseCode(result.first);
                    DB.insertLog(log.getMethod(), log.getTime(), log.getErrorMessage(), null, 0,log.getResponseCode());
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
}

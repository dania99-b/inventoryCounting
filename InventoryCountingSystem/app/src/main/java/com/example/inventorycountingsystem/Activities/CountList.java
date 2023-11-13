package com.example.inventorycountingsystem.Activities;


import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorycountingsystem.APIs.ApiCaller;

import com.example.inventorycountingsystem.DataClasses.Items;
import com.example.inventorycountingsystem.DataClasses.Job;
import com.example.inventorycountingsystem.DataClasses.Locations;
import com.example.inventorycountingsystem.DataClasses.StockCountingTransaction;
import com.example.inventorycountingsystem.DataClasses.Warehouse;
import com.example.inventorycountingsystem.Network.MyApplication;
import com.example.inventorycountingsystem.Network.NetworkChangeListener;
import com.example.inventorycountingsystem.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.inventorycountingsystem.DataClasses.Logs;

import javax.net.ssl.SSLException;

public class CountList extends AppCompatActivity {
    TextView _count_nameTV;
    TextView _countTypeTV;
    TextView _item_numberTV;
    TextView WarehouseTitle;
    TextView ItemTitle;
    TextView LocationTitle;
    Button movementBTN;
    Button transactionBTN;
    Button _CancelBTN;
    RecyclerView recyclerViewItems;
    RecyclerView recyclerViewWarehouses;
    RecyclerView recyclerViewLocations;
    RecyclerView recyclerViewDepartments;
    private boolean isLoading = false; // Flag to prevent multiple API calls at once
    private boolean isLoading1 = false; // Flag to prevent multiple API calls at once
    private boolean isLoading11 = false; // Flag to prevent multiple API calls at once
    private ItemAdapter adapter;
    private ItemAdapter warehouseAdapter;
    private ItemAdapter departmentsAdapter;
    private ItemAdapter locationAdapter;
    private SharedPreferences PagesharedPreferences;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_list);

        getSupportActionBar().setTitle("Active Stock Count");

        LinearLayout mainLayout = findViewById(R.id.myMainLayoutLL);
        Context context = this;
        MyApplication.setCurrentActivity(this);

        String count_name = getIntent().getStringExtra("count_name");
        PagesharedPreferences = getSharedPreferences("PaginationData", Context.MODE_PRIVATE);
        _count_nameTV = findViewById(R.id.countNameTV);
        // _start_dateTV = findViewById(R.id.startDateTV);
        _countTypeTV = findViewById(R.id.countTypeTV);
        recyclerViewWarehouses = findViewById(R.id.recyclerViewWarehouses);
        recyclerViewLocations = findViewById(R.id.recyclerViewLocations);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewDepartments = findViewById(R.id.recyclerViewDeparts);
        WarehouseTitle = findViewById(R.id.WarehouseTitle);
        ItemTitle = findViewById(R.id.ItemTitle);
        LocationTitle = findViewById(R.id.LocationTitle);
        String count_type = DB.getCountType(count_name);
        adapter = new ItemAdapter(null,"item_name");
        //Initialize cursor null
        warehouseAdapter = new ItemAdapter(null,"warehouse_name");
        departmentsAdapter=new ItemAdapter(null,"department_name");
        locationAdapter=new ItemAdapter(null,"location_name");
        recyclerViewItems.setHasFixedSize(true);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(CountList.this));
        recyclerViewItems.setAdapter(adapter);


        NetworkChangeListener networkChangeReceiver = new NetworkChangeListener(this, count_name,count_type); // Pass the reference to your activity and the count_name
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);


        _CancelBTN=findViewById(R.id.cancelButton);
        if (count_type.equals("Material")) {
            try {
                getCountItemPagination(count_name,1000);
                getCountWarehousePagination(count_name,1000);
                Cursor initialWarehouseCursor = DB.getWarehouse_stock_countingData(count_name);
                warehouseAdapter.updateCursor(initialWarehouseCursor);
                System.out.println("itemmm stocck countt tableee");
                DB.printItemStockCountingTable();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        } else if (count_type.equals("Asset")) {
            try {
                getCountAssetPagination(count_name,100);
                getCountLocationPagination(count_name,100);
                Cursor initialLocationCursor = DB.getLocation_stock_countingData(count_name, 0);
                locationAdapter.updateCursor(initialLocationCursor);

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (SSLException e) {
                e.printStackTrace();
            } catch (ConnectException e) {
                e.printStackTrace();
            }
        }
        try {
            getCountDepartmentsPagination(count_name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ///initilize Cursor for Item adapter
        Cursor initialItemCursor = DB.getItems_stock_countingData(count_name);
        adapter.updateCursor(initialItemCursor);


    //    Cursor initialDepartmentCursor = DB.getLocation_stock_countingData(count_name, 0);
       // locationAdapter.updateCursor(initialWarehouseCursor);


        DB.printItems();
        _CancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerViewItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0)
                    {
                        // Scroll reached the bottom, load more data here
                        Log.d("ScrollListener", "Reached the bottom, loading more data ITEMS...");
                        int offset = adapter.getItemCount(); // Calculate the offset based on the current data
                        Cursor newCursor = DB.getItems_stock_countingData(count_name);

                        // Check if the new cursor is not null
                        if (newCursor != null)
                        {
                            // Move the cursor to the first position
                            if (newCursor.moveToFirst()) {
                                do {
                                    // Process each row in the cursor and extract data
                                    String item = newCursor.getString(newCursor.getColumnIndex("item_name")); // Use the specified column name
                                    adapter.addItem(item);
                                } while (newCursor.moveToNext());
                            }
                            // Close the new cursor
                            newCursor.close();
                        }
                    }
                }
            }
            });


        if (count_type.equals("Asset"))
        {
            WarehouseTitle.setVisibility(View.GONE);
            recyclerViewWarehouses.setVisibility(View.GONE);
        }
        else {
            recyclerViewWarehouses.setHasFixedSize(true);
            recyclerViewWarehouses.setLayoutManager(new LinearLayoutManager(CountList.this));
            recyclerViewWarehouses.setAdapter(warehouseAdapter);
            recyclerViewWarehouses.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView1, int dx1, int dy1) {
                    super.onScrolled(recyclerView1, dx1, dy1);

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView1.getLayoutManager();
                    int visibleWarehouseCount = layoutManager.getChildCount();
                    int totalWarehouseCount = layoutManager.getItemCount();
                    int firstVisibleWarehousePosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading1) {
                        if ((visibleWarehouseCount + firstVisibleWarehousePosition) >= totalWarehouseCount
                                && firstVisibleWarehousePosition >= 0) {
                            Log.d("ScrollListener", "Reached the bottom, loading more data WAREHOUSE...");
                            int offset = warehouseAdapter.getItemCount(); // Calculate the offset based on the current data
                            Cursor newCursor = DB.getWarehouse_stock_countingData(count_name);

                            // Check if the new cursor is not null
                            if (newCursor != null) {
                                // Move the cursor to the first position
                                if (newCursor.moveToFirst()) {
                                    do {
                                        // Process each row in the cursor and extract data
                                        String item = newCursor.getString(newCursor.getColumnIndex("warehouse_name"));
                                        warehouseAdapter.addItem(item);
                                    } while (newCursor.moveToNext());
                                }

                                // Close the new cursor
                                newCursor.close();
                            }
                        }
                    }
                }
            });
        }
        if (count_type.equals("Material")) {
            LocationTitle.setVisibility(View.GONE);
            recyclerViewLocations.setVisibility(View.GONE);
        } else {
            recyclerViewLocations.setHasFixedSize(true);
            recyclerViewLocations.setLayoutManager(new LinearLayoutManager(CountList.this));
            recyclerViewLocations.setAdapter(locationAdapter);
            recyclerViewLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView1, int dx1, int dy1) {
             super.onScrolled(recyclerView1, dx1, dy1);

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView1.getLayoutManager();
                    int visibleWarehouseCount = layoutManager.getChildCount();
                    int totalWarehouseCount = layoutManager.getItemCount();
                    int firstVisibleWarehousePosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading1) {
                        if ((visibleWarehouseCount + firstVisibleWarehousePosition) >= totalWarehouseCount
                                && firstVisibleWarehousePosition >= 0) {
                            Log.d("ScrollListener", "Reached the bottom, loading more data LOCATION...");
                            int offset = locationAdapter.getItemCount(); // Calculate the offset based on the current data
                            Cursor newCursor = DB.getLocation_stock_countingData(count_name, offset);

                            // Check if the new cursor is not null
                            if (newCursor != null) {
                                // Move the cursor to the first position
                                if (newCursor.moveToFirst()) {
                                    do {
                                        // Process each row in the cursor and extract data
                                        String item = newCursor.getString(newCursor.getColumnIndex("location_name"));
                                        locationAdapter.addItem(item);
                                    } while (newCursor.moveToNext());
                                }

                                // Close the new cursor
                                newCursor.close();
                            }
                        }
                    }
                }
            });
        }
        recyclerViewDepartments.setHasFixedSize(true);
        recyclerViewDepartments.setLayoutManager(new LinearLayoutManager(CountList.this));
        recyclerViewDepartments.setAdapter(departmentsAdapter);
        recyclerViewDepartments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView1, int dx1, int dy1) {
                super.onScrolled(recyclerView1, dx1, dy1);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView1.getLayoutManager();
                int visibleWarehouseCount = layoutManager.getChildCount();
                int totalWarehouseCount = layoutManager.getItemCount();
                int firstVisibleWarehousePosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading1) {
                    if ((visibleWarehouseCount + firstVisibleWarehousePosition) >= totalWarehouseCount
                            && firstVisibleWarehousePosition >= 0) {
                        // Scroll reached the bottom, load more data here
                        Log.d("ScrollListener", "Reached the bottom, loading more data...");
                        try {
                            getCountDepartmentsPagination(count_name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        try {

            _count_nameTV.setText(count_name);
            _countTypeTV.setText(count_type);

        } catch (Exception e) {
            e.printStackTrace();
        }

        transactionBTN = findViewById(R.id.transactionBTN);
        transactionBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CountList.this, DoTransaction.class);
                intent.putExtra("countName", count_name);
                intent.putExtra("type", count_type);
                startActivity(intent);
            }
        });

        movementBTN = findViewById(R.id.movementBTN);
        if (count_type.equals("Asset")) {

            movementBTN.setVisibility(View.GONE);

        }
        movementBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(CountList.this, Movement.class);
                intent2.putExtra("countName", count_name);
                intent2.putExtra("type", count_type);
                startActivity(intent2);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuewithoutsetting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                try {
                    performCheckSync();
                    performSync();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.check:
                DB.printStockCountingTransactionTable();

        }
        return super.onOptionsItemSelected(item);
    }
    public void getCountItemPagination(String countName, int pageSize) throws ParseException, SSLException {
        if (isLoading) {
            // Don't load more data if an API call is already in progress
            return;
        }



        isLoading = true;
        // Retrieve the last saved page from SharedPreferences
        int currentPage = PagesharedPreferences.getInt("LastItemPage", 0);
        // Set a flag to indicate if you should start from the beginning
        boolean shouldStartFromBeginning = true;
        boolean hasMoreData = true; // Flag to track if there's more data

        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        SharedPreferences UsersharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = UsersharedPreferences.getString("USER_NAME", "");
        String api_secret = UsersharedPreferences.getString("API_SECRET", "");
        String api_key = UsersharedPreferences.getString("API_KEY", "");
        String baseUrl = ipAddress + "get_counting_materials";
        System.out.println(baseUrl);
        String authorizationHeader = api_key + ":" + api_secret;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);

        while (hasMoreData) {
            String urlWithParameters = baseUrl + "?page=" + currentPage + "&page_size=" + pageSize + "&count_name=" + countName;

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameters, "GET", CountList.this, null, authorizationHeader);
                    if (result.first == 200) {
                        // Parse the JSON response and add items to your database
                        try {
                            JSONObject jsonObject = new JSONObject(result.second);
                            JSONObject stock_countArray = jsonObject.getJSONObject("message");
                            JSONArray stock_itemArray = stock_countArray.getJSONArray("items");

                            for (int i = 0; i < stock_itemArray.length(); i++) {
                                Items items = new Items();
                                JSONObject locationObject = stock_itemArray.getJSONObject(i);
                                String item_name = locationObject.getString("item_name");
                                String item_code = locationObject.getString("item_code");
                                String barcode = locationObject.getString("barcode");
                                items.setItem_code(item_code);
                                items.setItem_name(item_name);
                                items.setType("Material");
                                items.setBarcode(barcode);
                                DB.insertItemData(item_code, item_name, barcode, "Material");
                                DB.insertItemStockCounting(countName, item_code, item_name, barcode, "Material");
                            }

                            int page_max = stock_countArray.getInt("max_page");

                            if (currentPage < page_max) {
                                // Increment the page number for the next request
                                currentPage++;
                                shouldStartFromBeginning = false;
                            } else {
                                // No more data, exit the loop
                                hasMoreData = false;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Logs and error handling code here
                    } else {
                        // Handle API error (e.g., show a message to the user)
                        // Logs and error handling code here
                        Toast.makeText(CountList.this, "API Error: " + result.first, Toast.LENGTH_SHORT).show();
                        break; // Exit the loop in case of an API error
                    }
                }
            }

            // Check if you should start from the beginning
            if (shouldStartFromBeginning) {
                currentPage = 0;
            }

            // Save the last page in SharedPreferences
            SharedPreferences.Editor editor = PagesharedPreferences.edit();
            editor.putInt("LastItemPage", currentPage);
            editor.apply();



        }

        isLoading = false;
    }


    public void getCountAssetPagination(String countName, int pageSize) throws ParseException, SSLException, ConnectException {
        if (isLoading) {
            // Don't load more data if an API call is already in progress
            return;
        }
        isLoading = true;
        // Retrieve the last saved page from SharedPreferences for the asset pagination
        int currentPage = PagesharedPreferences.getInt("LastAssetPage", 0);

        // Set a flag to indicate if you should start from the beginning
        boolean shouldStartFromBeginning = true;

        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        SharedPreferences UsersharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = UsersharedPreferences.getString("USER_NAME", "");
        String api_secret = UsersharedPreferences.getString("API_SECRET", "");
        String api_key = UsersharedPreferences.getString("API_KEY", "");
        String baseUrl = ipAddress + "get_counting_assets";
        System.out.println(baseUrl);
        String authorizationHeader = api_key + ":" + api_secret;
        boolean hasMoreData = true; // Flag to track if there's more data

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);

        while (hasMoreData) {
            String urlWithParameters = baseUrl + "?page=" + currentPage + "&page_size=" + pageSize + "&count_name=" + countName;

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameters, "GET", CountList.this, null, authorizationHeader);
                    if (result.first == 200) {
                        // Parse the JSON response and add items to your database
                        try {
                            JSONObject jsonObject = new JSONObject(result.second);
                            JSONObject stock_countArray = jsonObject.getJSONObject("message");
                            JSONArray stock_itemArray = stock_countArray.getJSONArray("assets_list");

                            for (int i = 0; i < stock_itemArray.length(); i++) {
                                JSONObject locationObject = stock_itemArray.getJSONObject(i);
                                String item_name = locationObject.getString("item_name");
                                String item_code = locationObject.getString("item_code");
                                String barcode = locationObject.getString("barcode");

                                DB.insertItemData(item_code, item_name, barcode, "Asset");
                                DB.insertItemStockCounting(countName, item_code, item_name, barcode, "Asset");
                            }

                            int page_max = stock_countArray.getInt("max_page");

                            if (page_max > currentPage) {
                                // Increment the page number for the next request
                                currentPage++;
                                shouldStartFromBeginning = false;
                            } else {
                                // No more data, exit the loop
                                hasMoreData = false;
                            }
                        }

                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Logs log = new Logs();
                        log.setMethod(baseUrl);
                        log.setStatus(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters);
                        log.setTime(getCurrentTime());
                        log.setPage(currentPage);
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), log.getUsername(), log.getPage(), log.getResponseCode());
                        // Logs and error handling code here
                    } else {
                        Logs log = new Logs();
                        log.setMethod(baseUrl);
                        log.setErrorMessage(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters);
                        log.setTime(getCurrentTime());
                        log.setPage(currentPage);
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), log.getUsername(), log.getPage(), log.getResponseCode());

                        // Handle API error (e.g., show a message to the user)
                        // Logs and error handling code here
                        Toast.makeText(CountList.this, "API Error: " + result.first, Toast.LENGTH_SHORT).show();
                        break; // Exit the loop in case of an API error
                    }
                }
            }

            // Check if you should start from the beginning
            if (shouldStartFromBeginning) {
                currentPage = 0;
            }

            // Save the last page in SharedPreferences for the asset pagination
            SharedPreferences.Editor editor = PagesharedPreferences.edit();
            editor.putInt("LastAssetPage", currentPage);
            editor.apply();


        }

        isLoading = false;
    }



    public void getCountWarehousePagination(String countName, int pageSize) throws ParseException, SSLException {
        if (isLoading1) {
            // Don't load more data if an API call is already in progress
            return;
        }
        isLoading1 = true;

        // Retrieve the last saved page from SharedPreferences for the warehouse pagination
        int currentPage = PagesharedPreferences.getInt("LastWarehousePage", 0);

        // Set a flag to indicate if you should start from the beginning
        boolean shouldStartFromBeginning = true;

        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        SharedPreferences UsersharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = UsersharedPreferences.getString("USER_NAME", "");
        String api_secret = UsersharedPreferences.getString("API_SECRET", "");
        String api_key = UsersharedPreferences.getString("API_KEY", "");
        System.out.println("user info");
        System.out.println(api_secret);
        System.out.println(api_key);
        String baseUrl1 = ipAddress + "get_counting_warehouses";
        System.out.println(baseUrl1);
        String authorizationHeader = api_key + ":" + api_secret;
        System.out.println(authorizationHeader);
        boolean hasMoreData = true; // Flag to track if there's more data

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);

        while (hasMoreData) {
            String urlWithParameters1 = baseUrl1 + "?page=" + currentPage + "&page_size=" + pageSize + "&count_name=" + countName;

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameters1, "GET", CountList.this, null, authorizationHeader);
                    if (result.first == 200) {
                        // Parse the JSON response and add items to your database
                        try {
                            JSONObject jsonObject = new JSONObject(result.second);
                            JSONObject stock_countArrayWarehouse = jsonObject.getJSONObject("message");
                            JSONArray stock_WarehouseArray = stock_countArrayWarehouse.getJSONArray("warehouse");

                            for (int i = 0; i < stock_WarehouseArray.length(); i++) {
                                JSONObject warehouseObject = stock_WarehouseArray.getJSONObject(i);
                                String warehouse_name = warehouseObject.getString("material_warehouse");
                                DB.insertcount_warehouse(countName, warehouse_name);
                            }

                            int page_max = stock_countArrayWarehouse.getInt("max_page");

                            if (page_max > currentPage) {
                                // Increment the page number for the next request
                                currentPage++;
                                shouldStartFromBeginning = false;
                            } else {
                                // No more data, exit the loop
                                hasMoreData = false;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Logs log = new Logs();
                        log.setMethod(baseUrl1);
                        log.setStatus(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters1);
                        log.setTime(getCurrentTime());
                    } else {
                        Logs log = new Logs();
                        log.setMethod(baseUrl1);
                        log.setStatus(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters1);
                        log.setTime(getCurrentTime());
                        // Handle API error (e.g., show a message to the user)
                        // Logs and error handling code here
                        Toast.makeText(CountList.this, "API Error: " + result.first, Toast.LENGTH_SHORT).show();
                        break; // Exit the loop in case of an API error
                    }
                }
            }

            // Check if you should start from the beginning
            if (shouldStartFromBeginning) {
                currentPage = 0;
            }

            // Save the last page in SharedPreferences for the warehouse pagination
            SharedPreferences.Editor editor = PagesharedPreferences.edit();
            editor.putInt("LastWarehousePage", currentPage);
            editor.apply();
        }

        isLoading1 = false;
    }


    public void getCountLocationPagination(String countName, int pageSize) throws ParseException, SSLException {
        if (isLoading1) {
            // Don't load more data if an API call is already in progress
            return;
        }
        isLoading1 = true;
        // Retrieve the last saved page from SharedPreferences for the location pagination
        int currentPage = PagesharedPreferences.getInt("LastLocationPage", 0);

        // Set a flag to indicate if you should start from the beginning
        boolean shouldStartFromBeginning = true;

        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        SharedPreferences UsersharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = UsersharedPreferences.getString("USER_NAME", "");
        String api_secret = UsersharedPreferences.getString("API_SECRET", "");
        String api_key = UsersharedPreferences.getString("API_KEY", "");
        String baseUrl2 = ipAddress + "get_counting_locations";
        System.out.println(baseUrl2);
        String authorizationHeader = api_key + ":" + api_secret;
        boolean hasMoreData = true; // Flag to track if there's more data

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);

        while (hasMoreData) {
            String urlWithParameters2 = baseUrl2 + "?page=" + currentPage + "&page_size=" + pageSize + "&count_name=" + countName;

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameters2, "GET", CountList.this, null, authorizationHeader);
                    if (result.first == 200) {
                        // Parse the JSON response and add items to the adapter
                        try {
                            JSONObject jsonObject = new JSONObject(result.second);
                            JSONObject stock_countArrayWarehouse = jsonObject.getJSONObject("message");
                            JSONArray stock_LocationArray = stock_countArrayWarehouse.getJSONArray("locations");

                            for (int i = 0; i < stock_LocationArray.length(); i++) {
                                JSONObject LocationeObject = stock_LocationArray.getJSONObject(i);
                                String location_name = LocationeObject.getString("location");
                                DB.insertcount_location(countName, location_name);
                                locationAdapter.addItem(location_name); // Add the item to your list
                            }

                            int page_max = stock_countArrayWarehouse.getInt("max_page");

                            if (page_max > currentPage) {
                                // Increment the page number for the next request
                                currentPage++;
                                shouldStartFromBeginning = false;
                            } else {
                                // No more data, exit the loop
                                hasMoreData = false;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Logs log = new Logs();
                        log.setMethod(baseUrl2);
                        log.setStatus(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters2);
                        log.setTime(getCurrentTime());
                    } else {
                        Logs log = new Logs();
                        log.setMethod(baseUrl2);
                        log.setStatus(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters2);
                        log.setTime(getCurrentTime());
                        // Handle API error (e.g., show a message to the user)
                        // Logs and error handling code here
                        Toast.makeText(CountList.this, "API Error: " + result.first, Toast.LENGTH_SHORT).show();
                        break; // Exit the loop in case of an API error
                    }
                }
            }

            // Check if you should start from the beginning
            if (shouldStartFromBeginning) {
                currentPage = 0;
            }

            // Save the last page in SharedPreferences for the location pagination
            SharedPreferences.Editor editor = PagesharedPreferences.edit();
            editor.putInt("LastLocationPage", currentPage);
            editor.apply();
        }

        isLoading1 = false;
    }


    private void getCountDepartmentsPagination(String countName) throws JSONException {
        if (isLoading11) {
            // Don't load more data if an API call is already in progress
            return;
        }
        isLoading11 = true;
        // Retrieve the last saved page from SharedPreferences for the department pagination
        int currentPage = PagesharedPreferences.getInt("LastDepartmentPage", 0);

        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        long reCheckTime = SettingData.getLong("ReCheckTime", 0);
        SharedPreferences sharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        SharedPreferences SyncerTokenData = getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
        String api_key= SyncerTokenData.getString("SYNCER_KEY", "");
        String api_secret=  SyncerTokenData.getString("SYNCER_SECRET", "");
        String authorizationHeader = api_key + ":" + api_secret;
        String baseUrl1 = ipAddress + "get_counting_departments";
        String pageSize1 = "3"; // Set your desired page size
        boolean hasMoreData = true; // Flag to track if there's more data

        while (hasMoreData) {
            String urlWithParameters11 = baseUrl1 + "?page=" + currentPage + "&page_size=" + pageSize1 + "&count_name=" + countName;

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameters11, "GET", CountList.this, null, authorizationHeader);
                    if (result.first == 200) {
                        try {
                            JSONObject DeptsjsonObject = new JSONObject(result.second);
                            JSONObject stock_countArrayDepartment = DeptsjsonObject.getJSONObject("message");
                            JSONArray stock_DepartmentsArray = stock_countArrayDepartment.getJSONArray("departments");
                            for (int i = 0; i < stock_DepartmentsArray.length(); i++) {
                                JSONObject departmentObject = stock_DepartmentsArray.getJSONObject(i);
                                String department_name = departmentObject.getString("deparment_name");
                                departmentsAdapter.addItem(department_name); // Add the item to your list
                            }

                            int page_max = stock_countArrayDepartment.getInt("max_page");

                            if (page_max > currentPage) {
                                // Increment the page number for the next request
                                currentPage++;
                            } else {
                                // No more data, exit the loop
                                hasMoreData = false;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Logs log = new Logs();
                        log.setMethod(baseUrl1);
                        log.setStatus(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters11);

                        try {
                            log.setTime(getCurrentTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        log.setPage(currentPage);
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), log.getUsername(), log.getPage(), log.getResponseCode());
                    } else {
                        Logs log = new Logs();
                        log.setMethod(baseUrl1);
                        log.setErrorMessage(result.second);
                        log.setUsername(userName);
                        log.setParameter(urlWithParameters11);
                        try {
                            log.setTime(getCurrentTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        log.setPage(currentPage);
                        log.setResponseCode(result.first);
                        DB.insertLog(log.getMethod(), log.getTime(), log.getErrorMessage(), log.getUsername(), log.getPage(), log.getResponseCode());

                        // Handle API error (e.g., show a message to the user)
                        Toast.makeText(CountList.this, "API Error: " + result.first, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        // Notify the adapter that the data has changed
        // adapter.notifyDataSetChanged();
        isLoading11 = false;
    }



    private void performSync() throws JSONException, ParseException {
        SharedPreferences sharedPreferencessettings = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = sharedPreferencessettings.getString("IP_ADDRESS", "");
        SharedPreferences sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        String api_key = sharedPreferences.getString("API_KEY", "");
        String api_secret = sharedPreferences.getString("API_SECRET", "");
        String count_name2 = getIntent().getStringExtra("count_name");
        List<StockCountingTransaction> transactions = DB.getUnsyncStockCountingTransactions(0, getMacAddr());

        DB.printStockCountingTransactionTable();

        String url = ipAddress+"insert_material_transactions";
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
                System.out.println( getCurrentTime());
                System.out.println("syynnnccc");
                jsonArray.put(jsonTransaction);
            } catch (JSONException e) {
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
             //   SharedPreferences SyncerTokenData = getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
             //   String api_key= SyncerTokenData.getString("SYNCER_KEY", "");
             //   String api_secret=  SyncerTokenData.getString("SYNCER_SECRET", "");
                String authorizationHeader = api_key + ":" + api_secret;
                System.out.println(authorizationHeader);
                System.out.println(api_key);

                System.out.println(requestBody);

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        ApiCaller apiCaller = new ApiCaller();
                        System.out.println(requestBody);
                        System.out.println(String.valueOf(requestBody));
                        Pair<Integer, String> result = apiCaller.callApi(url, "POST", CountList.this, String.valueOf(requestBody), authorizationHeader);
                        JSONObject jsonObject = new JSONObject(result.second);
                        if (result.first == 200) {
                            JSONObject insert_response = jsonObject.getJSONObject("message");
                            String job_id = insert_response.getString("job_id");
                            Job job = new Job();
                            job.setId(job_id);
                            job.setStatus("Initialize");
                            System.out.println("job id from GET IDDDDDDDDDDDDDDD");
                            System.out.println(job.getId());
                            DB.insertNewJob(job.getId(), job.getStatus());
                            //tomorrow for check API
                            for (StockCountingTransaction transaction : transactions) {
                                if (transaction.getIs_corrective() == 1 || transaction.getIs_corrective() == 0 || transaction.getIs_corrective() == 3) {
                                    DB.updateJobId(transaction, job.getId());
                                      DB.updateSyncTime(transaction,getCurrentTime());
                                      DB.updateRetriesNumber(transaction,1);// Update sync status here
                                    System.out.println(transaction.getSync_time());
                                    Toast.makeText(CountList.this, "Inserted in database", Toast.LENGTH_LONG).show();
                                }
                            }
                            Logs log = new Logs();
                            log.setMethod(url);
                            log.setStatus(result.second);
                            log.setUsername(userName);
                            log.setParameter(requestBody.toString());
                            try {
                                log.setTime(getCurrentTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            log.setResponseCode(result.first);
                            DB.insertLog(log.getMethod(), log.getTime(), log.getStatus(), log.getUsername(), log.getPage(),log.getResponseCode());

                        }
                        else {

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

                        }

                }}
            }
            while (cursor.moveToNext());
        }
        DB.printJob();

    }

    private void performCheckSync() throws JSONException, ParseException {
        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
          String ipAddress = SettingData.getString("IP_ADDRESS", "");
        SharedPreferences sharedPreferences = this.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        String count_name2 = getIntent().getStringExtra("count_name");
        String api_secret = sharedPreferences.getString("API_SECRET", "");
        String api_key = sharedPreferences.getString("API_KEY", "");
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
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameter, "POST", CountList.this, null, authorizationHeader);
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
                        DB.printJob();
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

        DB.printStockCountingTransactionTable();
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

    public String getCurrentTime() throws ParseException {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("USER_NAME", "");
        long loginTimeElapsed = sharedPreferences.getLong("TIMEELAPSED", 0); // 0 is the default value
        String lastOpenDate = DB.getLastSessionDate(username);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date serverDate = dateFormat.parse(lastOpenDate);

        long currentElapsedTime = SystemClock.elapsedRealtime();
        long timeDifferenceInMillis = currentElapsedTime - loginTimeElapsed;
        Date updatedDate = new Date(serverDate.getTime() + timeDifferenceInMillis);
        String formattedUpdatedDate = dateFormat.format(updatedDate);
        return formattedUpdatedDate;
    }


}


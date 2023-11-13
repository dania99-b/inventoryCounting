package com.example.inventorycountingsystem.DataClasses;

import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.inventorycountingsystem.APIs.ApiCaller;
import com.example.inventorycountingsystem.Activities.CountList;
import com.example.inventorycountingsystem.Activities.LoginActivity;
import com.example.inventorycountingsystem.DBHelpers.DBHelper;
import com.example.inventorycountingsystem.Network.ServerBingTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.sql.SQLOutput;
import java.util.Collections;
import java.util.List;


public class SyncBackendWorker extends Worker {
    private String pageSize = String.valueOf(1000);
    private Context applicationContext;
    public SyncBackendWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.applicationContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        System.out.println("in workkkerrrr");
        SharedPreferences SyncerTokenData = applicationContext.getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
        String api_key = SyncerTokenData.getString("SYNCER_KEY", "");
        String api_secret = SyncerTokenData.getString("SYNCER_SECRET", "");

        ServerBingTask serverBingTask = new ServerBingTask(applicationContext);
        if(!api_key.isEmpty() && !api_secret.isEmpty() && serverBingTask.isBingServerReachable()) {

            try {
                storeDataFromOtherDevice(); // Pass the application context here        } catch (JSONException e) {
            } catch (JSONException e) {
                e.printStackTrace();
            }

        DB.printStockCountingTransactionTable();
            return Result.success();}
        else return Result.failure();
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






    private void storeDataFromOtherDevice() throws JSONException {
        SharedPreferences SettingData = applicationContext.getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String ipAddress = SettingData.getString("IP_ADDRESS", "");
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        SharedPreferences SyncerTokenData = applicationContext.getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
        String api_key= SyncerTokenData.getString("SYNCER_KEY", "");
        String api_secret=  SyncerTokenData.getString("SYNCER_SECRET", "");
        String baseUrl = ipAddress + "back_sync_record";
        String authorizationHeader = api_key + ":" + api_secret;
        System.out.println(authorizationHeader);
        // Delete all existing records before syncing
        DB.deleteAnotherDeviceStockCountingTransaction(getMacAddr());

        boolean shouldContinue = true;
        int page = 0;

        while (shouldContinue) {
            ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(LoginActivity.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    String urlWithParameters = baseUrl +"?counter_name=" +userName+ "&device_mac=" + getMacAddr() + "&page=" + page + "&page_size=" + pageSize;

                    ApiCaller apiCaller = new ApiCaller();
                    Pair<Integer, String> result = apiCaller.callApi(urlWithParameters, "GET", applicationContext, null, authorizationHeader);

                    if (result.first == 200) {
                        try {
                            JSONObject jsonObject = new JSONObject(result.second);
                            JSONObject recordsObject = jsonObject.getJSONObject("message");
                            JSONArray recordsArray = recordsObject.getJSONArray("records");
                            int pageMax = Integer.parseInt(recordsObject.getString("max_page"));

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
                                DB.insertStockCountingTransaction(transaction.getCount_name(), transaction.getItem_code(), transaction.getQuantity(), transaction.getPosting_date_time(), transaction.getConter_name(), transaction.getIs_corrective(), transaction.getStage(), transaction.getItem_site(), transaction.getWarehouse_id(), transaction.getLocation_id(), transaction.getType(), transaction.getSync_time(), transaction.getDevice_mac(),0);
                                System.out.println("ppppppppppppppppppppppppppppppp");
                             //   DB.printStockCountingTransactionTable();
                            }

                            if (page < pageMax) {
                                page++;
                            } else {
                                shouldContinue = false; // Exit the loop when all pages are processed
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing errors
                            shouldContinue = false; // Exit the loop on error
                        }
                    } else {
                        // Handle non-200 response code (e.g., error handling)
                        shouldContinue = false; // Exit the loop on error
                    }
                } else {
                    // Handle network connectivity issues
                    shouldContinue = false; // Exit the loop on connectivity issues
                }
            }
        }
    }}

    // Utility method to get the device's MAC address



package com.example.inventorycountingsystem.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.inventorycountingsystem.DataClasses.Job;
import com.example.inventorycountingsystem.DataClasses.Locations;
import com.example.inventorycountingsystem.DataClasses.StockCountingTransaction;
import com.example.inventorycountingsystem.DataClasses.Warehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "CountingSystemData", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        //Create User Table
        DB.execSQL("create Table Users (user_name TEXT PRIMARY KEY,api_key TEXT,api_secret TEXT)");
        //Create Stock Counting Table
        DB.execSQL("create Table Stock_Counting (counting_name TEXT PRIMARY KEY,type TEXT,location TEXT,start_date TEXT,department TEXT,warehouse TEXT,number_item INTEGER)");

        //Create user Stock Counting Table
        DB.execSQL("CREATE TABLE User_Stocking_Counting (id INTEGER PRIMARY KEY AUTOINCREMENT, user_name TEXT, counting_name TEXT, FOREIGN KEY(user_name) REFERENCES Users(user_name), FOREIGN KEY(counting_name) REFERENCES Stock_Counting(counting_name))");

        //Create Countname Table
        DB.execSQL("CREATE TABLE Count_Name (count_name TEXT PRIMARY KEY , type TEXT , manual_item_check INTEGER , manual_site_check INTEGER ) ");

        //Location Table
        DB.execSQL("create Table Locations (name TEXT PRIMARY KEY, barcode TEXT)");
        //count location table
        DB.execSQL("create Table count_locations (id INTEGER PRIMARY KEY AUTOINCREMENT,stock_counting_name TEXT , location_name TEXT)");
        // count warehouse table
        DB.execSQL("create Table count_warehouse (id INTEGER PRIMARY KEY AUTOINCREMENT,stock_counting_name TEXT , warehouse_name TEXT)");

        DB.execSQL("create Table Counts (name TEXT PRIMARY KEY, location_req INTEGER, start_date TEXT, " +
                "total_items INTEGER, counted_items INTEGER)");
         //Item Table
        DB.execSQL("CREATE TABLE Items (item_code TEXT PRIMARY KEY, item_name TEXT ,barcode TEXT , type TEXT CHECK (type IN ('Asset','Material')))");

        //Warehouse Table
        DB.execSQL("CREATE TABLE Warehouses (name TEXT PRIMARY KEY , barcode TEXT)");
        DB.execSQL("CREATE TABLE Departments (name TEXT PRIMARY KEY)");

        //Item Stock Count table
        DB.execSQL("CREATE TABLE Item_Stocking_Counting (id INTEGER PRIMARY KEY AUTOINCREMENT, stock_counting_name TEXT, item_code TEXT,item_name TEXT ,barcode TEXT , type TEXT CHECK (type IN ('Asset','Material')))");
        //Operation Table
        DB.execSQL("create Table Stock_Counting_Transaction(id INTEGER PRIMARY KEY AUTOINCREMENT,count_name TEXT,item_code TEXT, quantity DOUBLE, posting_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,warehouse TEXT  , conter_name TEXT, is_corrective INTEGER , type TEXT CHECK (type IN ('Count','Issuing', 'Receiving')),item_site TEXT,stage TEXT CHECK (stage IN ('Before', 'After', '') OR stage IS NULL), location TEXT  ,is_sync INTEGER  DEFAULT 0 ,sync_date_time DEFAULT CURRENT_TIMESTAMP,batch_number TEXT,job_id Text,device_mac TEXT, retriesNumber INTEGER ,FOREIGN KEY(warehouse) REFERENCES Warehouses(warehouse),FOREIGN KEY(item_code) REFERENCES Items(id),FOREIGN KEY(job_id) REFERENCES Job(id))");
        DB.execSQL("create Table Session(id INTEGER PRIMARY KEY AUTOINCREMENT,  counter_name TEXT,open_date DATETIME DEFAULT CURRENT_TIMESTAMP, status TEXT CHECK (status IN ('Open', 'Close')),api_key Text , api_secret Text)");
        DB.execSQL("create Table Log(id INTEGER PRIMARY KEY AUTOINCREMENT,  method TEXT,time DATETIME DEFAULT CURRENT_TIMESTAMP, status TEXT , username TEXT ,page INTEGER , responseCode INTEGER)");
        DB.execSQL("create Table Job(id INTEGER PRIMARY KEY AUTOINCREMENT,job_id  TEXT, status TEXT )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int ii) {

    }


    //////////////////////////////////////
    /////////// User Functions ///////////
    //////////////////////////////////////
    public Boolean insertLog(String method, String time, String status,String username ,int page ,int responseCode) {
        SQLiteDatabase DB = this.getWritableDatabase();
        if (method == null || time == null || status == null||username==null) {
            return false; // Return false as we cannot insert with null values
        }
        // Check if the combination of item_code, item_name, and barcode already exists in the database


        ContentValues contentValues = new ContentValues();
        contentValues.put("method", method);
        contentValues.put("time", time);
        contentValues.put("status", status);
        contentValues.put("username", username);
        contentValues.put("page", page);
        contentValues.put("responseCode", responseCode);

        long result = DB.insert("Log", null, contentValues);
        return -1 != result;
    }

    public int getRecordCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Stock_Counting_Transaction", null);

        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();

        return count;
    }


public Cursor getAllLog() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Log", null);
        return cursor;
    }

    public void printAllLog(){

        Cursor cursor = getAllLog();

        if (cursor.moveToFirst()) {
            System.out.println("////////////////////////////// HHHEREEE IS THEE Sessions ");
            do {

                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String method=cursor.getString(cursor.getColumnIndex("method"));
                String time=cursor.getString(cursor.getColumnIndex("time"));
                String status = cursor.getString(cursor.getColumnIndex("status"));
                String username = cursor.getString(cursor.getColumnIndex("username"));
                String page = cursor.getString(cursor.getColumnIndex("page"));
                String responseCode = cursor.getString(cursor.getColumnIndex("responseCode"));




                // ... Retrieve other column values as needed

                // Print the values to the console
                System.out.println("ID: " + id);
                System.out.println("method: " + method);
                System.out.println("time: " + time);
                System.out.println("status: " + status);
                System.out.println("username: " + username);
                System.out.println("page: " + page);
                System.out.println("responseCode: " + responseCode);



            } while (cursor.moveToNext());
        }
        System.out.println("////////////////////////////// HHHEREEE IS THEE Log DATABASE ENDDDDDDD ");
        cursor.close();

    }
    public void insertOrUpdateUser(String userName, String apiKey, String apiSecret) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name", userName);
        values.put("api_key", apiKey);
        values.put("api_secret", apiSecret);

        // Check if the user already exists
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE user_name=?", new String[]{userName});
        if (cursor.moveToFirst()) {
            // User exists, perform update
            db.update("Users", values, "user_name=?", new String[]{userName});
        } else {
            // User doesn't exist, perform insert
            db.insert("Users", null, values);
        }

        cursor.close();
        db.close();
    }



    public Cursor getAllUsers() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Users", null);
        return cursor;
    }
    public Cursor getAllSession() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Session", null);
        return cursor;
    }


    public void printAllSession(){

        Cursor cursor = getAllSession();

        if (cursor.moveToFirst()) {
            do {
                System.out.println("////////////////////////////// HHHEREEE IS THEE Sessions ");
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String counter_name=cursor.getString(cursor.getColumnIndex("counter_name"));
                String open_date=cursor.getString(cursor.getColumnIndex("open_date"));
                String status = cursor.getString(cursor.getColumnIndex("status"));
                String api_key = cursor.getString(cursor.getColumnIndex("api_key"));
                String api_secret = cursor.getString(cursor.getColumnIndex("api_secret"));


                // ... Retrieve other column values as needed

                // Print the values to the console
                System.out.println("ID: " + id);
                System.out.println("counter_name: " + counter_name);
                System.out.println("open_date: " + open_date);
                System.out.println("status: " + status);
                System.out.println("api_key: " + api_key);
                System.out.println("api_secret: " + api_secret);
                System.out.println("////////////////////////////// HHHEREEE IS THEE Sessions DATABASE ENDDDDDDD ");
            } while (cursor.moveToNext());
        }

        cursor.close();

    }

    public String getLastSessionDate(String counter_name) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery(
                "SELECT open_date FROM Session WHERE counter_name=? ORDER BY open_date DESC LIMIT 1",
                new String[]{counter_name}
        );

        String lastSessionDate = null;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("open_date");
            lastSessionDate = cursor.getString(columnIndex);
        }

        cursor.close();
        return lastSessionDate;
    }



    public String getLastSessionStatus() {
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase() or getWritableDatabase() as appropriate

        Cursor cursor = db.rawQuery("SELECT status FROM Session ORDER BY ROWID DESC LIMIT 1", null);

        String lastSessionStatus = null;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("status");
            lastSessionStatus = cursor.getString(columnIndex);
        }

        cursor.close();
        return lastSessionStatus;
    }



    /// Count Name Table

    public boolean insertCountName(String countName ,String type , int manual_item_check , int manual_site_check ) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the count name already exists in the table
        Cursor cursor = db.rawQuery("SELECT count_name FROM Count_Name WHERE count_name = ?", new String[]{countName});
        if (cursor.moveToFirst()) {
            // Count name already exists, handle the duplicate value here
            // For example, display an error message
            cursor.close();
            return false;
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put("count_name", countName);
        contentValues.put("type", type);
        contentValues.put("manual_item_check", manual_item_check);
        contentValues.put("manual_site_check", manual_site_check);

        long result = db.insert("Count_Name", null, contentValues);
        return result != -1;
    }

    public int getManualItemCheck(String count_name) {
        int manualBarcodeCheck = -1; // Default value in case the query doesn't return a valid result
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT manual_item_check FROM Count_Name WHERE count_name = ?", new String[]{count_name});

        if (cursor.moveToFirst()) {
            manualBarcodeCheck = cursor.getInt(0); // Assuming the manual_barcode_check column is an integer
        }

        cursor.close();
        DB.close();

        return manualBarcodeCheck;
    }

    public int getManualSiteCheck(String count_name) {
        int manualWarehouseCheck = -1; // Default value in case the query doesn't return a valid result
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT manual_site_check FROM Count_Name WHERE count_name = ?", new String[]{count_name});

        if (cursor.moveToFirst()) {
            manualWarehouseCheck = cursor.getInt(0); // Assuming the manual_barcode_check column is an integer
        }

        cursor.close();
        DB.close();

        return manualWarehouseCheck;
    }




    public String getCountType(String count_name) {
        String type =""; // Default value in case the query doesn't return a valid result
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT type FROM Count_Name WHERE count_name = ?", new String[]{count_name});

        if (cursor.moveToFirst()) {
            type = cursor.getString(0); // Assuming the manual_barcode_check column is an integer
        }

        cursor.close();
        DB.close();

        return type;
    }








    public Cursor getAllCountName() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM Count_Name", null);
        return cursor;
    }
    public boolean insertCountDetails(String countName,String type,String start_date,int number_item) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the count name already exists in the table
        Cursor cursor = db.rawQuery("SELECT counting_name FROM Stock_Counting WHERE counting_name = ?", new String[]{countName});
        if (cursor.moveToFirst()) {
            // Count name already exists, handle the duplicate value here
            // For example, display an error message
            cursor.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("counting_name", countName);
        values.put("type", type);
        values.put("start_date", start_date);
        values.put("number_item", number_item);

        long result = db.insert("Stock_Counting", null, values);
        return result != -1;
    }

    public Cursor getAllCountDetails() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM Stock_Counting", null);
        return cursor;
    }

    //////////////////////////////////////
    /////////// Item Functions ///////////
    //////////////////////////////////////


    public Boolean insertItemData(String item_code, String item_name, String barcode,String type) {
        SQLiteDatabase DB = this.getWritableDatabase();
        if (item_code == null || item_name == null || barcode == null) {
            return false; // Return false as we cannot insert with null values
        }
        // Check if the combination of item_code, item_name, and barcode already exists in the database
        if (isDuplicateCombinationItemOnly(DB, item_code, item_name, barcode)) {
            return false; // Combination already exists, return false
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("item_code", item_code);
        contentValues.put("item_name", item_name);
        contentValues.put("barcode", barcode);
        contentValues.put("type", type);

        long result = DB.insert("Items", null, contentValues);
        return -1 != result;
    }

    public Boolean insertDepartment(String name) {
        SQLiteDatabase DB = this.getWritableDatabase();
        if (name == null ) {
            return false; // Return false as we cannot insert with null values
        }
        // Check if the combination of item_code, item_name, and barcode already exists in the database
        if (isDuplicateCombinationdepartments(DB,name)) {
            return false; // Combination already exists, return false
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);


        long result = DB.insert("Departments", null, contentValues);
        return -1 != result;
    }




    public Boolean insertNewJob(String id, String status) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        try {
            contentValues.put("job_id", id                                                                                                                      );
            contentValues.put("status", status);

            long result = DB.insert("Job", null, contentValues);

            if (result != -1) {

                return true;
            } else {

                return false;
            }
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        } finally {

            DB.close();
        }
    }





    public String getItemCodeByBarcode(String barcode) {
        SQLiteDatabase DB = this.getReadableDatabase();
        String[] columns = { "item_code" };
        String selection = "barcode = ?";
        String[] selectionArgs = { barcode };

        Cursor cursor = DB.query("Items", columns, selection, selectionArgs, null, null, null);
        String itemCode = null;
        if (cursor != null && cursor.moveToFirst()) {
            itemCode = cursor.getString(cursor.getColumnIndex("item_code"));
            cursor.close();
        }
        return itemCode;
    }


    // Helper method to check if the combination of item code, item name, and barcode already exists in the database
    private boolean isDuplicateCombinationItemOnly(SQLiteDatabase DB, String item_code, String item_name, String barcode) {
        String[] columns = { "item_code", "item_name", "barcode" };
        String selection = "item_code = ? AND item_name = ? AND barcode = ?";
        String[] selectionArgs = { item_code, item_name, barcode };

        Cursor cursor = DB.query("Items", columns, selection, selectionArgs, null, null, null);
        boolean isDuplicate = cursor.moveToFirst();
        cursor.close();
        return isDuplicate;
    }


    //So i can use .getCount()
    public Cursor getAllItemsData () {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Items",null);
        return cursor;
    }

    public Cursor getItemsData (int Offset) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Items LIMIT 20 OFFSET " + Offset, null);
        return cursor;
    }
    // insert counting item
    public boolean insertItemStockCounting(String stock_counting_name, String item_code , String item_name , String barcode , String type)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
      //  if (stock_counting_name == null || item_code == null) {
        //    return false; // Return false as we cannot insert with null values
      //  }
        // Check if the combination of stock_counting_name and item_code already exists in the database
        if (isDuplicateCombinationItemStockCounting(DB, stock_counting_name, item_code)) {
            return false; // Combination already exists, return false
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("item_code", item_code);
        contentValues.put("item_name", item_name);
        contentValues.put("barcode", barcode);
        contentValues.put("type", type);
        contentValues.put("stock_counting_name", stock_counting_name);

        long result = DB.insert("Item_Stocking_Counting", null, contentValues);
        return -1 != result;
    }

    public boolean insertSession(String counter_name, String open_date,String status,String api_key,String api_secret) {
        SQLiteDatabase DB = this.getWritableDatabase();
        //  if (stock_counting_name == null || item_code == null) {
        //    return false; // Return false as we cannot insert with null values
        //  }
        // Check if the combination of stock_counting_name and item_code already exists in the database

        ContentValues contentValues = new ContentValues();
        contentValues.put("counter_name", counter_name);
        contentValues.put("open_date", open_date);
        contentValues.put("status", status);
        contentValues.put("api_key", api_key);
        contentValues.put("api_secret", api_secret);

        long result = DB.insert("Session", null, contentValues);
        return -1 != result;
    }




    // Helper method to check if the combination of stock counting name and item code already exists in the database
    private boolean isDuplicateCombinationItemStockCounting(SQLiteDatabase DB, String stock_counting_name, String item_code) {
        String[] columns = { "stock_counting_name", "item_code" };
        String selection = "item_code = ?";
        String[] selectionArgs = { item_code };

        Cursor cursor = DB.query("Item_Stocking_Counting", columns, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            String existingStockCountingName = cursor.getString(cursor.getColumnIndex("stock_counting_name"));

            if (existingStockCountingName.equals(stock_counting_name)) {
                // Found a duplicate with the same combination, don't store it
                cursor.close();
                return true;
            }
        }

        cursor.close();
        return false;
    }


    public long insertStockCountingTransaction(String countName, String itemCode, double quantity, String postingDateTime,
                                               String counterName, int isCorrective, String stage, String itemSite,
                                               String warehouse, String location, String type, String syncTime, String device_mac, int retryNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("count_name", countName);
        contentValues.put("item_code", itemCode);
        contentValues.put("quantity", quantity);
        contentValues.put("posting_date_time", postingDateTime);
        contentValues.put("conter_name", counterName);
        contentValues.put("warehouse", warehouse); // Check that this warehouse value exists in the Warehouses table
        contentValues.put("is_corrective", isCorrective);
        contentValues.put("stage", stage); // Make sure it's 'Before', 'After', or null
        contentValues.put("item_site", itemSite); // Make sure it's 'Warehouse' or 'Location'
        contentValues.put("location", location);
        contentValues.put("type", type); // Make sure it's 'Count', 'Issuing', or 'Receiving'
        contentValues.put("sync_date_time", syncTime); // Ensure syncTime is in the right format
        contentValues.put("device_mac", device_mac); // Ensure syncTime is in the right format
        contentValues.put("retriesNumber", retryNumber); // Ensure syncTime is in the right format

        long id = db.insert("Stock_Counting_Transaction", null, contentValues);

        Log.d("DATABASE", "Inserted row with id: " + id);
        // No need to close the database here, let the caller handle it.

        return id;
    }

    public void deleteAnotherDeviceStockCountingTransaction(String device_mac){
        try {
            SQLiteDatabase DB = this.getWritableDatabase();
            String whereClause = "device_mac" + " != ?";
            String[] whereArgs = {device_mac};
            DB.delete("Stock_Counting_Transaction", whereClause, whereArgs);
        } catch (Exception e) {
            Log.e("Delete Error", "Error deleting rows: " + e.getMessage());
    }}

    public long insertUserStockingCounting(String user_name, String countName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the user and count name combination already exists
        String query = "SELECT COUNT(*) FROM User_Stocking_Counting WHERE user_name = ? AND counting_name = ?";
        String[] selectionArgs = {user_name, countName};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        // If the combination already exists, return -1 indicating no insertion
        if (count > 0) {
            return -1;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("user_name", user_name);
        contentValues.put("counting_name", countName);

        long id = db.insert("User_Stocking_Counting", null, contentValues);

        db.close();
        Log.d("DATABASE", "inserted row with id: " + id);
        return id;
    }
    public Cursor getSpecificUserStockingCounting(String user_name) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select counting_name from User_Stocking_Counting WHERE user_name = ?", new String[]{user_name});
        return cursor;
    }
public  void updateJobStatus(String job_id,String status){
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put("status", status); // Set the new value for the "status" column

    // Update the row based on the "id" value
    String whereClause = "id = ?";
    String[] whereArgs = {job_id};
    int rowsAffected = db.update("Job", values, whereClause, whereArgs);

    db.close();

}

    public  void updateFinalSessionRowStatus(String counter_name,String status){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("status", status); // Set the new value for the "status" column

        String query = "SELECT MAX(id) FROM Session WHERE counter_name = ?";
        String[] selectionArgs = {counter_name};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            int lastRowId = cursor.getInt(0); // Get the ID of the last row

            // Update the row based on the "id" value (lastRowId)
            String whereClause = "id = ?";
            String[] whereArgs = {String.valueOf(lastRowId)};
            int rowsAffected = db.update("Session", values, whereClause, whereArgs);
        }

        cursor.close();
        db.close();
    }

    public void updateSyncStatus(String job_id, List<String> sucess_transaction_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("is_sync", 1); // Set the new value for the is_sync column

        // Construct the WHERE clause to update all rows except those with IDs in the 'failed_transaction_id' list
        StringBuilder whereClause = new StringBuilder("job_id = ? AND id IN (");
        String[] whereArgs = new String[1 + sucess_transaction_id.size()];
        whereArgs[0] = job_id;

        for (int i = 0; i < sucess_transaction_id.size(); i++) {
            if (i > 0) {
                whereClause.append(", ");
            }
            whereClause.append("?");
            whereArgs[i + 1] = sucess_transaction_id.get(i);
        }
        whereClause.append(")");

        int rowsAffected = db.update("Stock_Counting_Transaction", values, whereClause.toString(), whereArgs);
        System.out.println(rowsAffected);

        db.close();

    }





    public void updateSyncTime( StockCountingTransaction transaction, String sync_time) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("sync_date_time", sync_time); // Replace 'sync_time' with your desired value

        // Construct the WHERE clause to update all rows except those with IDs in the 'failed_transaction_id' list

        // Update the row based on the "id" value
        String whereClause = "count_name = ?";
        String[] whereArgs = {transaction.getCount_name()};
        int rowsAffected = db.update("Stock_Counting_Transaction", values, whereClause, whereArgs);

        db.close();}

    public void updateRetriesNumber(StockCountingTransaction transaction, int trynumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Fetch the current value of retriesNumber
        String[] columnsToFetch = {"retriesNumber"};
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(transaction.getId())};
        Cursor cursor = db.query("Stock_Counting_Transaction", columnsToFetch, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {
            // Retrieve the current value of retriesNumber
            int currentRetries = cursor.getInt(cursor.getColumnIndex("retriesNumber"));

            // Calculate the new value by adding trynumber
            int newRetries = currentRetries + trynumber;

            // Construct the ContentValues to update the retriesNumber
            ContentValues values = new ContentValues();
            values.put("retriesNumber", newRetries);

            // Update the row based on the "count_name" value
            int rowsAffected = db.update("Stock_Counting_Transaction", values, whereClause, whereArgs);
        }

        cursor.close();
        db.close();
    }


    public void updateJobId(StockCountingTransaction transaction, String job_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("job_id", job_id); // Set the new value for the is_sync column
        // Update the row based on the ID value
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(transaction.getId())};
        int rowsAffected = db.update("Stock_Counting_Transaction", values, whereClause, whereArgs);

        db.close();
    }

    public Cursor getStockCountingTransaction() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM Stock_Counting_Transaction", null);
        return cursor;
    }

    public Cursor getjobs(int offset) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Job LIMIT 100 OFFSET " + offset, null);
        return cursor;
    }

    public  void printJob(){
    Cursor cursor = getjobs(0);

        if (cursor.moveToFirst()) {
        do {
            System.out.println("////////////////////////////// HHHEREEE IS THEE DATABASE ");
            String id = cursor.getString(cursor.getColumnIndex("job_id"));
            String status = cursor.getString(cursor.getColumnIndex("status"));

            // ... Retrieve other column values as needed

            // Print the values to the console
            System.out.println("ID: " + id);
            System.out.println("status: " + status);
            System.out.println("////////////////////////////// HHHEREEE IS THEE DATABASE ENDDDDDDD ");
        } while (cursor.moveToNext());
    }

        cursor.close();
}



    public List<StockCountingTransaction> getUnsyncStockCountingMovements(int offset) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Stock_Counting_Transaction WHERE is_sync = 0 AND is_corrective=3 LIMIT 20 OFFSET " + offset, null);

        List<StockCountingTransaction> transactions = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String itemCode = cursor.getString(cursor.getColumnIndex("item_code"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                String posting_date = cursor.getString(cursor.getColumnIndex("posting_date_time"));
                String warehouse = cursor.getString(cursor.getColumnIndex("warehouse"));
                String counter_name = cursor.getString(cursor.getColumnIndex("conter_name"));
                int is_corrective = cursor.getInt(cursor.getColumnIndex("is_corrective"));
                String item_site = cursor.getString(cursor.getColumnIndex("item_site"));

                String stage = cursor.getString(cursor.getColumnIndex("stage"));

                String location = cursor.getString(cursor.getColumnIndex("location"));
                // Retrieve other column values as needed
                StockCountingTransaction transaction = new StockCountingTransaction();
                transaction.setId(id);
                transaction.setItem_code(itemCode);
                transaction.setQuantity(quantity);
                transaction.setPosting_date_time(posting_date);
                transaction.setWarehouse_id(warehouse);
                transaction.setConter_name(counter_name);
                transaction.setIs_corrective(is_corrective);
                transaction.setItem_site(item_site);
                transaction.setStage(stage);
                transaction.setLocation_id(location);
                // Set other properties of the transaction object as needed

                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return transactions;
    }

    public List<String> getJobsId() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, job_id FROM Job WHERE status = 'Initialize'", null);
        List<String> jobs_id = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("job_id"));
                jobs_id.add(id);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return jobs_id;
    }


    public List<StockCountingTransaction> getUnsyncStockCountingTransactions(int offset,String MacAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = { MacAddress }; // Prepare the selection argument
        Cursor cursor = db.rawQuery("SELECT * FROM Stock_Counting_Transaction WHERE is_sync = 0 AND (is_corrective=0 OR is_corrective=1 OR is_corrective=3) AND device_mac = ? AND retriesNumber<5 LIMIT 20 OFFSET " + offset, selectionArgs);
        List<StockCountingTransaction> transactions = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String count_name = cursor.getString(cursor.getColumnIndex("count_name"));
                String itemCode = cursor.getString(cursor.getColumnIndex("item_code"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                String posting_date = cursor.getString(cursor.getColumnIndex("posting_date_time"));
                String warehouse = cursor.getString(cursor.getColumnIndex("warehouse"));
                String counter_name = cursor.getString(cursor.getColumnIndex("conter_name"));
                int is_corrective = cursor.getInt(cursor.getColumnIndex("is_corrective"));
                String item_site = cursor.getString(cursor.getColumnIndex("item_site"));
                String stage = cursor.getString(cursor.getColumnIndex("stage"));
                String location = cursor.getString(cursor.getColumnIndex("location"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String sync_time = cursor.getString(cursor.getColumnIndex("sync_date_time"));
                // Retrieve other column values as needed
                StockCountingTransaction transaction = new StockCountingTransaction();
                transaction.setId(id);
                transaction.setCount_name(count_name);
                transaction.setItem_code(itemCode);
                transaction.setQuantity(quantity);
                transaction.setPosting_date_time(posting_date);
                transaction.setWarehouse_id(warehouse);
                transaction.setConter_name(counter_name);
                transaction.setIs_corrective(is_corrective);
                transaction.setItem_site(item_site);
                transaction.setStage(stage);
                transaction.setLocation_id(location);
                transaction.setType(type);
                transaction.setSync_time(sync_time);
                System.out.println(transaction.getSync_time());
                // Set other properties of the transaction object as needed
                transactions.add(transaction);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        return transactions;
    }

    private boolean isValidBarcode(String barcode) {
        // Define a regular expression pattern for valid barcodes (e.g., numeric only).
        String pattern = "^[0-9]+$";

        // Check if the input matches the pattern.
        return barcode.matches(pattern);
    }

    public boolean isItemCodeExists(String barcode, String count_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        if(isValidBarcode(barcode)){
        // Step 1: Retrieve the item code from the "Items" table using the provided barcode
        Cursor itemCursor = db.rawQuery("SELECT item_code FROM Items WHERE barcode = ?", new String[]{barcode});
        if (itemCursor.moveToFirst()) {
            String itemCode = itemCursor.getString(itemCursor.getColumnIndex("item_code"));
            itemCursor.close();

            // Step 2: Check if the item code exists in the "Item_Stocking_Counting" table for the specified stock counting name
            Cursor countingCursor = db.rawQuery("SELECT * FROM Item_Stocking_Counting WHERE item_code = ? AND stock_counting_name = ?", new String[]{itemCode, count_name});
            boolean exists = countingCursor.moveToFirst();
            countingCursor.close();

            return exists;
        } else {
            // Barcode not found in the "Items" table
            itemCursor.close();
            return false;
        } }
        else return false;
    }


    public boolean isItemCodeExistsinTransaction(String barcode, String count_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Stock_Counting_Transaction WHERE item_code = ? AND count_name = ?", new String[]{barcode, count_name});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    public boolean iswarehouseCodeExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Warehouses WHERE  name = ?", new String[]{ name});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    public boolean islocationCodeExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Locations WHERE  name = ?", new String[]{ name});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }
    public Cursor getItems_stock_countingallData(int offset){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Item_Stocking_Counting", null);
        return cursor;

    }
    public Cursor getItems_stock_countingData(String countName) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM Item_Stocking_Counting WHERE stock_counting_name = ?",
                new String[]{countName});
        return cursor;
    }

    public Cursor getLocation_stock_countingAllData( int offset) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from count_locations LIMIT 20 OFFSET " + offset, null);
        return cursor;
    }

    public Cursor getLocation_stock_countingData(String count_name, int offset) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM count_locations WHERE stock_counting_name = ? LIMIT 20 OFFSET ?",
                new String[]{count_name, String.valueOf(offset)});
        return cursor;
    }

    public Cursor getWarehouse_stock_countingData(String count_name) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM count_warehouse WHERE stock_counting_name = ?",
                new String[]{count_name});
        return cursor;
    }

    public Cursor getItems() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM Item_Stocking_Counting", null);
        System.out.println("Number of items in cursor: " + cursor.getCount());
        return cursor;
    }
    public Cursor getWarehouse(int offset) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = { "name", "barcode"}; // Include the barcode column in the query
        Cursor cursor = db.query("Warehouses", columns, null, null, null, null, null, "20 OFFSET " + offset);
        return cursor;
    }

    public List<String> getWarehouseBarcodes() {
        List<String> barcodes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase for read-only operations

        String[] columns = { "barcode" };
        String selection = null; // You can add a selection if needed
        String[] selectionArgs = null; // You can add selection arguments if needed
        String orderBy = "name ASC"; // Adjust the ordering as needed

        Cursor cursor = db.query("Warehouses", columns, selection, selectionArgs, null, null, orderBy );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                barcodes.add(barcode);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return barcodes;
    }


    public List<String> getLocationBarcodes() {
        List<String> barcodes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase for read-only operations

        String[] columns = { "barcode" };
        String selection = null; // You can add a selection if needed
        String[] selectionArgs = null; // You can add selection arguments if needed
        String orderBy = "name ASC"; // Adjust the ordering as needed

        Cursor cursor = db.query("Locations", columns, selection, selectionArgs, null, null, orderBy );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                barcodes.add(barcode);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return barcodes;
    }


    public List<String> getItemBarcodesStockCount(String stockCountingName) {
        List<String> barcodeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columnsToReturn = {"barcode"};
        String selection = "stock_counting_name = ?";
        String[] selectionArgs = {stockCountingName};

        Cursor cursor = db.query("Item_Stocking_Counting", columnsToReturn, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                barcodeList.add(barcode);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return barcodeList;
    }


    public void put_in_hashmap(HashMap hashMap){
            Cursor cursor = getWarehouse(0);
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String barcode = cursor.getString(cursor.getColumnIndex("barcode"));

                    hashMap.put(barcode, name);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

    public void put_location_in_hashmap(HashMap hashMap){
        Cursor cursor = getLocation(0);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));

                // Populate the mapping with barcode as the key and warehouse name as the value
                hashMap.put(barcode, name);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    //insert counting with his location table
public boolean insertcount_location(String stock_counting_name, String location_name) {
    SQLiteDatabase DB = this.getWritableDatabase();

    // Check if the combination of stock_counting_name and location_name already exists in the database
    if (isDuplicateCombinationlocation_count(DB, stock_counting_name, location_name)) {
        return false; // Combination already exists, return false
    }

    ContentValues contentValues = new ContentValues();
    contentValues.put("stock_counting_name", stock_counting_name);
    contentValues.put("location_name", location_name);

    long result = DB.insert("count_locations", null, contentValues);
    return -1 != result;
}

    // Helper method to check if the combination of stock counting name and location already exists in the database
    private boolean isDuplicateCombinationLocation(SQLiteDatabase DB, String location_name, String barcode) {
        String[] columns = {"name"};
        String selection = "name = ? OR barcode = ?";
        String[] selectionArgs = {location_name, barcode};

        Cursor cursor = DB.query("Locations", columns, selection, selectionArgs, null, null, null);
        boolean isDuplicate = cursor.moveToFirst();
        cursor.close();
        return isDuplicate;
    }
    public boolean insertwarehouse(String warehousename, String barcode) {
        SQLiteDatabase DB = this.getWritableDatabase();

        // Check if the combination of stock_counting_name and warehouse_name already exists in the database
        if (isDuplicateCombinationwarehouse(DB, warehousename, barcode)) {
            return false; // Combination already exists, return false
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", warehousename);
        contentValues.put("barcode", barcode);

        long result = DB.insert("Warehouses", null, contentValues);
        return -1 != result;}

    //insert counting with his warehouse table
    public boolean insertcount_warehouse(String stock_counting_name, String warehouse_name) {
        SQLiteDatabase DB = this.getWritableDatabase();

        // Check if the combination of stock_counting_name and warehouse_name already exists in the database
        if (isDuplicateCombination(DB, stock_counting_name, warehouse_name)) {
            return false; // Combination already exists, return false
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("stock_counting_name", stock_counting_name);
        contentValues.put("warehouse_name", warehouse_name);

        long result = DB.insert("count_warehouse", null, contentValues);
        return -1 != result;
    }

    // Helper method to check if the combination of stock counting name and warehouse already exists in the database
    private boolean isDuplicateCombination(SQLiteDatabase DB, String stock_counting_name, String warehouse_name) {
        String[] columns = { "stock_counting_name", "warehouse_name" };
        String selection = "stock_counting_name = ? AND warehouse_name = ?";
        String[] selectionArgs = { stock_counting_name, warehouse_name };

        Cursor cursor = DB.query("count_warehouse", columns, selection, selectionArgs, null, null, null);
        boolean isDuplicate = cursor.moveToFirst();
        cursor.close();
        return isDuplicate;
    }
    private boolean isDuplicateCombinationlocation_count(SQLiteDatabase DB, String stock_counting_name, String location_name) {
        String[] columns = { "stock_counting_name", "location_name" };
        String selection = "stock_counting_name = ? AND location_name = ?";
        String[] selectionArgs = { stock_counting_name, location_name };

        Cursor cursor = DB.query("count_locations", columns, selection, selectionArgs, null, null, null);
        boolean isDuplicate = cursor.moveToFirst();
        cursor.close();
        return isDuplicate;
    }

    private boolean isDuplicateCombinationdepartments(SQLiteDatabase DB, String department_name) {
        String[] columns = { "name" };
        String selection = "name = ?";
        String[] selectionArgs = { department_name };

        Cursor cursor = DB.query("Departments", columns, selection, selectionArgs, null, null, null);
        boolean isDuplicate = cursor.moveToFirst();
        cursor.close();
        return isDuplicate;
    }


    private boolean isDuplicateCombinationwarehouse(SQLiteDatabase DB, String warehouse_name, String barcode) {
        String[] columns = { "name", "barcode" };
        String selection = "name = ? AND barcode = ?";
        String[] selectionArgs = { warehouse_name, barcode };

        Cursor cursor = DB.query("Warehouses", columns, selection, selectionArgs, null, null, null);
        boolean isDuplicate = cursor.moveToFirst();
        cursor.close();
        return isDuplicate;
    }


    private boolean isDuplicateCombinationlocation(SQLiteDatabase DB, String location_name, String barcode) {
        String[] columns = { "name", "barcode" };
        String selection = "name = ? AND barcode = ?";
        String[] selectionArgs = { location_name, barcode };

        Cursor cursor = DB.query("Locations", columns, selection, selectionArgs, null, null, null);
        boolean isDuplicate = cursor.moveToFirst();
        cursor.close();
        return isDuplicate;
    }



/////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Three functions to get Items data and three others to get there numbers
     * 1.1- Items
     * 1.2- Items.count
     *
     * 2.1- Materials
     * 2.2- Materials.count
     *
     * 3.1- Assets
     * 3.2- Assets.count
     */



        public String getlocationNameByBarcode(String barcode){

             SQLiteDatabase DB = this.getReadableDatabase();
             String[] columns = { "name" };
             String selection = "barcode = ?";
             String[] selectionArgs = { barcode };

             Cursor cursor = DB.query("Locations", columns, selection, selectionArgs, null, null, null);
             String name = null;
             if (cursor != null && cursor.moveToFirst()) {
           name = cursor.getString(cursor.getColumnIndex("name"));
            cursor.close();

                 }
             return name;
                 }


    public Boolean insertLocationsData(String name, String barcode) {
        SQLiteDatabase DB = this.getWritableDatabase();
        if (isDuplicateCombinationLocation(DB, name, barcode)) {

            return false;
        } else {
            // Location does not exist, insert a new record
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name);
            contentValues.put("barcode", barcode);

            long result = DB.insert("Locations", null, contentValues);
            return result != -1;
        }
    }



    public Cursor getLocation(int offset) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM Locations LIMIT 20 OFFSET " + offset, null);
        System.out.println("Number of items in cursor: " + cursor.getCount());
        return cursor;
    }


    public Cursor getLocationNameByBarcode(String barcode){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select name from Locations WHERE barcode = ?",new String[] {barcode});
        return cursor;
    }


    /**
     * Three types of Operations :
     * 1- Counting Materials
     * 2- Counting Assets
     * 3- Transactions
     */


    public Cursor getStock_Counting_TransactionData () {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Stock_Counting_Transaction", null);
        return cursor;
    }

    public Cursor getUnCountedItems (int count_id,int Offset) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * " +
                "FROM Items " +
                "LEFT JOIN Operations ON Items.barcode = Operations.barcode " +
                "WHERE Items.count_id = "+ count_id +" AND Operations.barcode IS NULL " +
                "LIMIT 20 OFFSET "+ Offset,null);
        return cursor;
    }

    /**
     * Three functions to get uncounted data and three others to get there numbers
     * 1.1- Uncounted Items
     * 1.2- Uncounted Items.count
     *
     * 2.1- Uncounted Materials
     * 2.2- Uncounted Materials.count
     *
     * 3.1- Uncounted Assets
     * 3.2- Uncounted Assets.count
     */

// 1.1
    public Cursor getUncountedItemsByCountName(String countName, int Offset) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT i.name, i.location " +
                "FROM Items i " +
                "WHERE i.count_name = ? " +
                "AND i.name NOT IN (SELECT o.item_name " +
                "FROM Operations o " +
                "WHERE o.count_name = ?) " +
                "LIMIT 20 " +
                "OFFSET ?", new String[]{countName, countName, String.valueOf(Offset)});
        return cursor;
    }

    public void deleteStockCountTransactionData() {
        SQLiteDatabase DB = this.getWritableDatabase();
        try {
            DB.execSQL("DELETE FROM Stock_Counting_Transaction");
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        DB.close();
    }}

    public void deleteAllData() {
        SQLiteDatabase DB = this.getWritableDatabase();
        try {
        DB.execSQL("DELETE FROM Users");
        DB.execSQL("DELETE FROM Stock_Counting");
        DB.execSQL("DELETE FROM User_Stocking_Counting");
        DB.execSQL("DELETE FROM Count_Name");
        DB.execSQL("DELETE FROM count_locations");
        DB.execSQL("DELETE FROM count_warehouse");
        DB.execSQL("DELETE FROM Counts");
        DB.execSQL("DELETE FROM Items");
        DB.execSQL("DELETE FROM Locations");
        DB.execSQL("DELETE FROM Warehouses");
        DB.execSQL("DELETE FROM Departments");
        DB.execSQL("DELETE FROM Item_Stocking_Counting");
        DB.execSQL("DELETE FROM Stock_Counting_Transaction");
        DB.execSQL("DELETE FROM Session");
        DB.execSQL("DELETE FROM Log");
        DB.execSQL("DELETE FROM Job");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DB.close();
        }

    }


    //to reset the database
    public void resetDB(){
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.execSQL("DROP TABLE IF EXISTS Items");
        DB.execSQL("DROP TABLE IF EXISTS Locations");
        DB.execSQL("DROP TABLE IF EXISTS Counts");
        DB.execSQL("DROP TABLE IF EXISTS Operations");

        DB.execSQL("CREATE TABLE Items (id INTEGER,name TEXT,type TEXT,barcode TEXT,location TEXT,count_id INTEGER)");
        DB.execSQL("create Table Locations (id INTEGER primary key AUTOINCREMENT,name TEXT,barcode TEXT)");
        DB.execSQL("create Table Counts (id INTEGER,name TEXT,location_req INTEGER)");

        DB.execSQL("create Table Operations (id INTEGER primary key AUTOINCREMENT, user_token TEXT,user_name TEXT,count_id INTEGER,location_id, " +
                "barcode TEXT,quantity INTEGER,direction TEXT, type TEXT,nature TEXT,counting_status TEXT,record_status TEXT,synced INTEGER , "+
                "Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,CONSTRAINT fk_OnGoingCounts FOREIGN KEY (count_id) REFERENCES Counts(id))");

    }

    public void printStockCountingTransactionTable() {
        Cursor cursor = getStockCountingTransaction();

        if (cursor.moveToFirst()) {
            do {
                System.out.println("////////////////////////////// HHHEREEE IS THEE DATABASE ");
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String count_name = cursor.getString(cursor.getColumnIndex("count_name"));
                String itemCode = cursor.getString(cursor.getColumnIndex("item_code"));
                float quantity = cursor.getFloat(cursor.getColumnIndex("quantity"));
                String postingDate = cursor.getString(cursor.getColumnIndex("posting_date_time"));
                String conter_name = cursor.getString(cursor.getColumnIndex("conter_name"));
                String sync = cursor.getString(cursor.getColumnIndex("is_sync"));
                String is_corrective = cursor.getString(cursor.getColumnIndex("is_corrective"));
                String stage = cursor.getString(cursor.getColumnIndex("stage"));
                String item_site = cursor.getString(cursor.getColumnIndex("item_site"));
                String warehouse = cursor.getString(cursor.getColumnIndex("warehouse"));
                String location = cursor.getString(cursor.getColumnIndex("location"));
                String sync_time = cursor.getString(cursor.getColumnIndex("sync_date_time"));
                String device_mac = cursor.getString(cursor.getColumnIndex("device_mac"));
                String retries_number = cursor.getString(cursor.getColumnIndex("retriesNumber"));

                // ... Retrieve other column values as needed

                // Print the values to the console
                System.out.println("ID: " + id);
              //  System.out.println("count_name: " + count_name);
                  System.out.println("Item Code: " + itemCode);
                  System.out.println("counter name: " + conter_name);
                  System.out.println("Quantity: " + quantity);
              //  System.out.println("Posting Date: " + postingDate);
              //  System.out.println("is_sync: " + sync);
                  System.out.println("is_corrective: " + is_corrective);
              //  System.out.println("stage: " + stage);
              //  System.out.println("item site: " + item_site);
              //  System.out.println("warehouse: " + warehouse);
              //  System.out.println("location: " + location);
               // System.out.println("sync time: " + sync_time);
              //  System.out.println("device mac: " + device_mac);
                System.out.println("retries number: " + retries_number);


                // ... Print other column values as needed
                //System.out.println("////////////////////////////// HHHEREEE IS THEE DATABASE ENDDDDDDD ");
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    public void printItemStockCountingTable() {
        Cursor cursor = getItems_stock_countingallData(100);

        if (cursor.moveToFirst()) {
            do {
                System.out.println("start");
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String countname  = cursor.getString(cursor.getColumnIndex("stock_counting_name"));
                String itemCode = cursor.getString(cursor.getColumnIndex("item_code"));

                // ... Retrieve other column values as needed

                // Print the values to the console
                System.out.println("ID: " + id);
                System.out.println("Item Code: " + itemCode);
                System.out.println("count name: " + countname);

            } while (cursor.moveToNext());
        }
        System.out.println("databasedd ");
        cursor.close();
    }

    public void printlocationStockCountingTable() {
        Cursor cursor = getLocation_stock_countingAllData(0);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String countname  = cursor.getString(cursor.getColumnIndex("stock_counting_name"));
                String locatin_name = cursor.getString(cursor.getColumnIndex("location_name"));

                // ... Retrieve other column values as needed

                // Print the values to the console
                System.out.println("ID: " + id);
                System.out.println("Location_name: " + locatin_name);
                System.out.println("count name: " + countname);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    public void printItems() {
        Cursor cursor = getItems();
        if (cursor.moveToFirst()) {
            do {
                System.out.println("////////////////////////////// HHHEREEE IS THEE DATABASE of ItemStockCounting ");
                String id  = cursor.getString(cursor.getColumnIndex("id"));
                String item_code  = cursor.getString(cursor.getColumnIndex("item_code"));
                String item_name = cursor.getString(cursor.getColumnIndex("item_name"));
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String count_name = cursor.getString(cursor.getColumnIndex("stock_counting_name"));

                // ... Retrieve other column values as needed

                // Print the values to the console
                System.out.println("id: " + id);
                System.out.println("item_code: " + item_code);
                System.out.println("item_name: " + item_name);
                System.out.println("barcode: " + barcode);
                System.out.println("type: " + type);
                System.out.println("count name: " + count_name);

            } while (cursor.moveToNext());
        }
        System.out.println("////////////////////////////// the database end ");
        cursor.close();
    }

}
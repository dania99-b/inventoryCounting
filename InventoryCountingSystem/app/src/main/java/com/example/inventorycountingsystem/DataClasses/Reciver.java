package com.example.inventorycountingsystem.DataClasses;

import static androidx.core.content.ContextCompat.registerReceiver;

import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import android.annotation.SuppressLint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Vibrator;
import android.util.Log;
import android.widget.EditText;

import com.example.inventorycountingsystem.Activities.CountList;
import com.example.inventorycountingsystem.Activities.DoTransaction;
import com.example.inventorycountingsystem.DBHelpers.DBHelper;
import com.example.inventorycountingsystem.R;

import java.nio.charset.StandardCharsets;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Reciver extends BroadcastReceiver {
    String countName;
    private EditText targetEditText; // The EditText to set the scanned data
    private EditText targetEditText1;
    private Context context;



    public Reciver(EditText targetEditText,EditText targetEditText1 , Context context ) {
        this.targetEditText = targetEditText;
        this.targetEditText1=targetEditText1;
        this.context=context;
    }
    public void setCountName(String countName) {
        this.countName = countName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String code = intent.getStringExtra("data");
        List<String> warehouseBarcodes = DB.getWarehouseBarcodes();
        List<String> itemBarcodes = DB.getItemBarcodesStockCount(countName);
        List<String> locationBarcodes = DB.getLocationBarcodes();
        System.out.println(itemBarcodes.toString());
        System.out.println(warehouseBarcodes.toString());
        System.out.println(locationBarcodes.toString());

        if (checkBarcodeInList(code, warehouseBarcodes)) {
            Log.d("Match", "Scanned code matches a warehouse code.");
            targetEditText.setText(code);
        }
        else if (checkBarcodeInList(code, locationBarcodes)) {
            Log.d("Match", "Scanned code matches a location code.");
            targetEditText.setText(code);
        }

        else if (checkBarcodeInList(code, itemBarcodes)) {
            Log.d("Match", "Scanned code matches an item barcode.");
            targetEditText1.setText(code);
        } else {
        Log.d("NoMatch", "Scanned code does not match any barcode.");
        Log.d("ScannedCode", "Scanned code: " + code);
        Log.d("ItemBarcodes", "Item barcodes: " + itemBarcodes.toString());
        // Vibrator code remains for feedback
        Vibrator vibrator2 = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator2 != null && vibrator2.hasVibrator()) {
            vibrator2.vibrate(200); // Vibrate for 200 milliseconds
        }
    }

}


    private boolean checkBarcodeInList(String code, List<String> barcodeList) {
        for (String barcode : barcodeList) {
            if (code.equals(barcode)) {
                return true;
            }
        }
        return false;
    }

}


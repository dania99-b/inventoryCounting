package com.example.inventorycountingsystem.Activities;

import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventorycountingsystem.R;

public class SettingActivity extends AppCompatActivity {

    //UI
    EditText _ipConfig;
    EditText _reCheckTime;
    Button _saveBTN;
    Button _deleteAllDataBTN;
    Button _deleteTransactionDataBTN;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        _ipConfig = findViewById(R.id.ipSettingET);
        _reCheckTime = findViewById(R.id.reCheckTimeET);
        _saveBTN = findViewById(R.id.saveSettingBTN);
        _deleteAllDataBTN= findViewById(R.id.deleteAllDataBTN);
        _deleteTransactionDataBTN = findViewById(R.id.deleteOperationDataBTN);


        String oldip = "https://erpnext.main/api/method/stock_count.stock_count.doctype.stock_count_transaction.stock_count_transaction.";


        //filling the Setting Data fields from the shared preferences
        SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        SharedPreferences.Editor SettingDataEditor = SettingData.edit();
        SettingDataEditor.putString("IP_ADDRESS", oldip);
        //SettingDataEditor.putLong("ReCheckTime",tmp2);
        SettingDataEditor.apply();



        _ipConfig.setText(oldip);


        _saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = _ipConfig.getText().toString().replaceAll("\\s", "");
                String reCheck = _reCheckTime.getText().toString();

                if(!(TextUtils.isEmpty(ipAddress)) && !(TextUtils.isEmpty(reCheck))) {

                    long tmp = Long.valueOf(reCheck);
                    long tmp2=tmp * 1000;

                    Toast.makeText(SettingActivity.this, "time :"+tmp2, Toast.LENGTH_SHORT).show();

                    //saving the ip to the shared preferences
                    SharedPreferences SettingData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor SettingDataEditor = SettingData.edit();
                    SettingDataEditor.putString("IP_ADDRESS", ipAddress);
                    SettingDataEditor.putLong("ReCheckTime", tmp2);
                    SettingDataEditor.apply();

                    //saving the ip to the MainActivity public data
                  //  MainActivity.IP_ADDRESS = ipAddress;
                 //   MainActivity.ReCheckTime = Long.valueOf(tmp2);

                    //back to login screen or to main screen
                    //check if user is logged in
                    if(MainActivity.USER_NAME==null || MainActivity.USER_TOKEN == null)
                    {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    else
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                else
                    Toast.makeText(SettingActivity.this, "You should fill all the fields", Toast.LENGTH_SHORT).show();
            }
        });
        _deleteAllDataBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.deleteAllData();
                Toast.makeText(SettingActivity.this, "All Data Deleted", Toast.LENGTH_SHORT).show();

            }
        });

        _deleteTransactionDataBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.deleteStockCountTransactionData();
                Toast.makeText(SettingActivity.this, "Transaction Data Deleted", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
package com.example.inventorycountingsystem.Activities;

import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventorycountingsystem.DataClasses.Reciver;
import com.example.inventorycountingsystem.DataClasses.StockCountingTransaction;
import com.example.inventorycountingsystem.Network.MyApplication;
import com.example.inventorycountingsystem.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class Movement extends AppCompatActivity {
    EditText _itemCodeET;
    EditText _quantityET;
    AutoCompleteTextView _locationManualET;
    Spinner _stageSP;
    Button _submitBTN;
    Button _cancelBTN;
    Spinner _movementTypeSP;
    String location;
    String warehouse ;
    String previousBarcode = "";
    CountList countList =new CountList();
   boolean isQuantityValid = true;
    private HashMap<String, String> barcodeToNameMapping = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onResume() {
        super.onResume();
        _itemCodeET.requestFocus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement);

        getSupportActionBar().setTitle("Active Stock Count");
        SharedPreferences sharedPreferencessettings = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String dns = sharedPreferencessettings.getString("IP_ADDRESS", "");
        MyApplication.setCurrentActivity(this);
        String countName = getIntent().getStringExtra("countName");
        String type = getIntent().getStringExtra("type");
        SharedPreferences sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("USER_NAME", "");
        //// take url config
        _submitBTN=findViewById(R.id.submitButton);
        _cancelBTN=findViewById(R.id.cancelButton1);
        _stageSP=findViewById(R.id.spinner22);
        _itemCodeET = findViewById(R.id.editText11);
        _locationManualET = findViewById(R.id.Autocompletewarehouse222);

        int BarcodeCheck= DB.getManualItemCheck(countName);
        int warehouseCheck= DB.getManualSiteCheck(countName);

        if(BarcodeCheck==0) {
            _itemCodeET.setKeyListener(null);
            _itemCodeET.setInputType(InputType.TYPE_NULL);
            _itemCodeET.setFocusable(false);
            _itemCodeET.setFocusableInTouchMode(false);

        }
        if(warehouseCheck==0){
            _locationManualET.setKeyListener(null);
            _locationManualET.setInputType(InputType.TYPE_NULL);
            _locationManualET.setFocusable(false);
            _locationManualET.setFocusableInTouchMode(false);

        }
        _itemCodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentBarcode = charSequence.toString();

                //  for clear the field if the previous code is different from the new one and update the field
                if (!currentBarcode.equals(previousBarcode)) {
                    _itemCodeET.getText().clear();
                    previousBarcode = currentBarcode;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        _quantityET = findViewById(R.id.editText22);
        if (type.equals("Asset")) {
            warehouse = null;
        }
        _movementTypeSP=findViewById(R.id.spinner44);

        if (type.equals("Material")) {
            location = null;
        }

        _cancelBTN.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }}));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.add("Before");
        adapter.add("After");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter1.add("Issuing");
        adapter1.add("Receiving");

// Set the adapter for the Spinner
        _stageSP.setAdapter(adapter);
        _movementTypeSP.setAdapter(adapter1);
        DB.put_in_hashmap(barcodeToNameMapping );
        DB.put_location_in_hashmap(barcodeToNameMapping);

        _locationManualET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String barcodeScanned = s.toString().trim();

                if (!barcodeScanned.isEmpty() && barcodeToNameMapping.containsKey(barcodeScanned)) {
                    String warehouseName = barcodeToNameMapping.get(barcodeScanned);
                    _locationManualET.setText(warehouseName);
                    _locationManualET.setSelection(warehouseName.length()); // Move cursor to the end
                }
            }
        });

        _submitBTN.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String itemCode = _itemCodeET.getText().toString();
                _quantityET = findViewById(R.id.editText22);
                String warehouse1=_locationManualET.getText().toString();
                if (!DB.isItemCodeExists(itemCode, countName)) {

                    Toast.makeText(Movement.this, "Item Code with this Stock Counting does not exist.", Toast.LENGTH_LONG).show();
                    Vibrator vibrator2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator2 != null && vibrator2.hasVibrator()) {
                        vibrator2.vibrate(200); // Vibrate for 200 milliseconds
                    }}
                else if (!DB.iswarehouseCodeExists(warehouse1)) {

                    Toast.makeText(Movement.this, "Warehouse does not exist.", Toast.LENGTH_LONG).show();
                    Vibrator vibrator2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator2 != null && vibrator2.hasVibrator()) {
                        vibrator2.vibrate(200); // Vibrate for 200 milliseconds
                    }}
                else{

                _quantityET.setInputType(InputType.TYPE_CLASS_NUMBER);
                _quantityET.setFilters(new InputFilter[]{new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String pattern = "[0-9]+(\\.[0-9]*)?";
                        if (!Pattern.matches(pattern, source)) {
                            return "";
                        }
                        return null;
                    }
                }});

                String quantityStr = _quantityET.getText().toString();
                String type="Issuing";
                Spinner spinner = findViewById(R.id.spinner22);
                String stage = spinner.getSelectedItem().toString();

                if (TextUtils.isEmpty(itemCode)) {
                    Toast.makeText(Movement.this, "All Fields Is Required.", Toast.LENGTH_LONG).show();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 100, 100, 100};
                    vibrator.vibrate(pattern, -1);
                } else{
                    String item_code=DB.getItemCodeByBarcode(itemCode);
                    StockCountingTransaction stockCountingTransaction=new StockCountingTransaction();


                    if (quantityStr.isEmpty()) {
                        Toast.makeText(Movement.this, "All Fields Is Required.", Toast.LENGTH_LONG).show();
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        long[] pattern = {0, 100, 100, 100};
                        vibrator.vibrate(pattern, -1);
                    } else {
                        try {
                            float quantity = Float.parseFloat(quantityStr);
                            stockCountingTransaction.setQuantity(quantity);
                            isQuantityValid = true;
                            // Use the numeric value
                            // Do something with the quantity variable
                        } catch (NumberFormatException e) {
                            // Handle invalid input
                            // Display an error message or perform any other necessary action
                            Toast.makeText(getApplicationContext(), "Invalid input!", Toast.LENGTH_SHORT).show();
                            isQuantityValid = false;

                            // Vibrate the device
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null && vibrator.hasVibrator()) {
                                vibrator.vibrate(200); // Vibrate for 200 milliseconds
                            }

                        }
                    }

                    String warehouse_name = warehouse1;
                    stockCountingTransaction.setCount_name(countName);
                    stockCountingTransaction.setItem_code(item_code);
                    try {
                        stockCountingTransaction.setPosting_date_time(getCurrentTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    stockCountingTransaction.setWarehouse_id(warehouse_name);
                    stockCountingTransaction.setConter_name(username);
                    stockCountingTransaction.setIs_corrective(3);
                    stockCountingTransaction.setStage(stage);
                    stockCountingTransaction.setLocation_id(location);
                    stockCountingTransaction.setType(type);
                    stockCountingTransaction.setItem_site("Warehouse");

                    long generatedId =   DB.insertStockCountingTransaction(stockCountingTransaction.getCount_name(),stockCountingTransaction.getItem_code(),stockCountingTransaction.getQuantity(),stockCountingTransaction.getPosting_date_time(),stockCountingTransaction.getConter_name()
                            ,stockCountingTransaction.getIs_corrective(),stockCountingTransaction.getStage(),stockCountingTransaction.getItem_site(),stockCountingTransaction.getWarehouse_id(),stockCountingTransaction.getLocation_id(),stockCountingTransaction.getType(),null,countList.getMacAddr(),0);
                    stockCountingTransaction.setId((int) generatedId);
                    System.out.println("inserted suuucceessfulllyyyyyfffgfgfgfgfggf");
                    System.out.println(stockCountingTransaction.getCount_name());
                    DB.printStockCountingTransactionTable();
                  }}}}));

        BroadcastReceiver LocationItembroadcastReceiver = new Reciver(_locationManualET,_itemCodeET,Movement.this);
        // Register the receivers using intent filters
        ((Reciver) LocationItembroadcastReceiver).setCountName(countName);
        registerReceiver(LocationItembroadcastReceiver, createIntentFilter("dania.taiba"));
    }

    // Helper method to create an IntentFilter
    private IntentFilter createIntentFilter(String action) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        return intentFilter;
    }



    public String getCurrentTime() throws ParseException {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("USER_NAME", "");
        long loginTimeElapsed = sharedPreferences.getLong("TIMEELAPSED", 0); // 0 is the default value
        String lastOpenDate = DB.getLastSessionDate(username);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date serverDate = dateFormat.parse(lastOpenDate);

        long currentElapsedTime = SystemClock.elapsedRealtime();
        long timeDifferenceInMillis = currentElapsedTime -loginTimeElapsed;

        Date updatedDate = new Date(serverDate.getTime() + timeDifferenceInMillis);

        String formattedUpdatedDate = dateFormat.format(updatedDate);

        return formattedUpdatedDate;
    }



}

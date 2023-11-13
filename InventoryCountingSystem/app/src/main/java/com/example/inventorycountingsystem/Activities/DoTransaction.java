package com.example.inventorycountingsystem.Activities;


import static android.text.format.DateUtils.formatElapsedTime;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import static com.example.inventorycountingsystem.Activities.SplashScreenActivity.DB;

import com.example.inventorycountingsystem.DataClasses.Reciver;
import com.example.inventorycountingsystem.DataClasses.StockCountingTransaction;

import com.example.inventorycountingsystem.Network.MyApplication;
import com.example.inventorycountingsystem.R;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class DoTransaction extends AppCompatActivity  {
    EditText _itemCodeET;
    EditText _quantityET;
    Button _submitBTN;
    Button _cancelBTN;
    AutoCompleteTextView _locationManualET;
    String location;
    String stage;
    String previousBarcode = "";
    private HashMap<String, String> barcodeToNameMapping = new HashMap<>();
    CountList countList =new CountList();

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onResume() {
        super.onResume();
        _itemCodeET.requestFocus();
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_transaction);
        SharedPreferences sharedPreferencessettings = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        String dns = sharedPreferencessettings.getString("IP_ADDRESS", "");
        SharedPreferences sharedPreferences = getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("USER_NAME", "");
        getSupportActionBar().setTitle("Active Stock Count");
        MyApplication.setCurrentActivity(this);

        String countName = getIntent().getStringExtra("countName");
        String type = getIntent().getStringExtra("type");

        _itemCodeET = findViewById(R.id.editText1);
        _quantityET = findViewById(R.id.editText2);
        _submitBTN = findViewById(R.id.submitButton);
        _cancelBTN = findViewById(R.id.cancelButton);
        _locationManualET = findViewById(R.id.Autocompletewarehouse22);
        //for checking manual barcode configuration
        int BarcodeCheck= DB.getManualItemCheck(countName);
        int warehouseCheck=DB.getManualSiteCheck(countName);

        if(BarcodeCheck==0){
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

        _cancelBTN.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }}));


            _locationManualET.addTextChangedListener(new TextWatcher() {
               @Override
               public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                 }

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
                 }

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

        _itemCodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentBarcode = charSequence.toString();

                // Check if the scanned barcode is different from the previous one
                if (!currentBarcode.equals(previousBarcode)) {
                    // Update the previous barcode
                    previousBarcode = currentBarcode;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (type.equals("Asset"))
        {
            _quantityET.setText("1");
            _quantityET.setVisibility(View.GONE);
        }

        DB.put_in_hashmap(barcodeToNameMapping);
        DB.put_location_in_hashmap(barcodeToNameMapping);




// Create an ArrayAdapter with the two values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.add("Before");
        adapter.add("After");

        _submitBTN.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String itemCode = _itemCodeET.getText().toString();
                String warehouse1 = _locationManualET.getText().toString();
                if (!DB.isItemCodeExists(itemCode, countName)) {

                    Toast.makeText(DoTransaction.this, "Item Code with this Stock Counting does not exist.", Toast.LENGTH_LONG).show();
                    Vibrator vibrator2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator2 != null && vibrator2.hasVibrator()) {
                        vibrator2.vibrate(200); // Vibrate for 200 milliseconds
                    }
                } else if (type.equals("Asset") && !DB.islocationCodeExists(warehouse1)) {

                    Toast.makeText(DoTransaction.this, "Location does not exist.", Toast.LENGTH_LONG).show();
                    Vibrator vibrator2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator2 != null && vibrator2.hasVibrator()) {
                        vibrator2.vibrate(200); // Vibrate for 200 milliseconds
                    }
                } else if (type.equals("Material") && !DB.iswarehouseCodeExists(warehouse1)) {

                    Toast.makeText(DoTransaction.this, "Warehouse does not exist.", Toast.LENGTH_LONG).show();
                    Vibrator vibrator2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator2 != null && vibrator2.hasVibrator()) {
                        vibrator2.vibrate(200); // Vibrate for 200 milliseconds
                    }
                } else {
                    String item_code = DB.getItemCodeByBarcode(itemCode);

                    _quantityET.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); // Set input type in XML if possible
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

                    if (type.equals("Material")) {

                        String stage = null;
                        location = null;
                    }

                    String counterName = username;
                    if (TextUtils.isEmpty(itemCode)) {
                        Toast.makeText(DoTransaction.this, "All Fields Is Required.", Toast.LENGTH_LONG).show();
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        long[] pattern = {0, 100, 100, 100};
                        vibrator.vibrate(pattern, -1);
                    } else {

                        StockCountingTransaction stockCountingTransaction = new StockCountingTransaction();

                        if (quantityStr.isEmpty()) {

                        } else {
                            try {
                                float quantity = Float.parseFloat(quantityStr);
                                stockCountingTransaction.setQuantity(quantity);
                            } catch (NumberFormatException e) {
                                // Handle invalid input
                                // Display an error message or perform any other necessary action
                                Toast.makeText(getApplicationContext(), "Invalid input!", Toast.LENGTH_SHORT).show();

                                // Vibrate the device
                                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                if (vibrator != null && vibrator.hasVibrator()) {
                                    vibrator.vibrate(200); // Vibrate for 200 milliseconds
                                }

                            }
                        }
                        if (type.equals("Material")) {
                            String warehouse_name = warehouse1;
                            stockCountingTransaction.setCount_name(countName);
                            stockCountingTransaction.setItem_code(item_code);
                            try {
                                stockCountingTransaction.setPosting_date_time(getCurrentTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            stockCountingTransaction.setWarehouse_id(warehouse_name);
                            System.out.println(stockCountingTransaction.getWarehouse_id());
                            stockCountingTransaction.setConter_name(counterName);
                            stockCountingTransaction.setType("Count");
                            stockCountingTransaction.setStage(stage);
                            stockCountingTransaction.setLocation_id(location);
                            stockCountingTransaction.setItem_site("Warehouse");// Make sure the location is not null
                        } else if (type.equals("Asset")) {
                            String warehouse_name = warehouse1;
                            stockCountingTransaction.setCount_name(countName);
                            stockCountingTransaction.setItem_code(item_code);
                            try {
                                stockCountingTransaction.setPosting_date_time(getCurrentTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            stockCountingTransaction.setLocation_id(warehouse_name);
                            stockCountingTransaction.setConter_name(counterName);
                            stockCountingTransaction.setType("Count");
                            stockCountingTransaction.setStage(stage);
                            stockCountingTransaction.setItem_site("Location");
                        }

                            if (DB.isItemCodeExistsinTransaction(item_code, countName)) {
                                showConfirmationDialog(stockCountingTransaction);
                            } else {

                                stockCountingTransaction.setIs_corrective(0);
                                ;
                                long generatedId = DB.insertStockCountingTransaction(stockCountingTransaction.getCount_name(), stockCountingTransaction.getItem_code(), stockCountingTransaction.getQuantity(), stockCountingTransaction.getPosting_date_time(), stockCountingTransaction.getConter_name()
                                        , stockCountingTransaction.getIs_corrective(), stockCountingTransaction.getStage(), stockCountingTransaction.getItem_site(),stockCountingTransaction.getWarehouse_id(), stockCountingTransaction.getLocation_id(), stockCountingTransaction.getType(),null,countList.getMacAddr(),0);
                                stockCountingTransaction.setId((int) generatedId);
                            }

                            DB.printStockCountingTransactionTable();
                        }
//clear item code and quantity after every submit
                        _itemCodeET.setText("");
                        _quantityET.setText("");
                    }
                }


        }));

        BroadcastReceiver LocationbroadcastReceiver = new Reciver(_locationManualET,_itemCodeET,DoTransaction.this);
        ((Reciver) LocationbroadcastReceiver).setCountName(countName);
        registerReceiver(LocationbroadcastReceiver, createIntentFilter("dania.taiba"));
    }

    // Helper method to create an IntentFilter
    private IntentFilter createIntentFilter(String action) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        return intentFilter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menuewithoutsync,menu);
        return true;
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


    private void showConfirmationDialog(final StockCountingTransaction stockCountingTransaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This item has had previous transactions. Do you want to proceed with a corrective transaction for it?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stockCountingTransaction.setIs_corrective(1);
                        // for Insert the transaction in my offline database
                        long generatedId = DB.insertStockCountingTransaction(stockCountingTransaction.getCount_name(), stockCountingTransaction.getItem_code(),
                                stockCountingTransaction.getQuantity(),
                                stockCountingTransaction.getPosting_date_time(),
                                stockCountingTransaction.getConter_name(),
                                stockCountingTransaction.getIs_corrective(),
                                stockCountingTransaction.getStage(),
                                stockCountingTransaction.getItem_site(),
                                stockCountingTransaction.getWarehouse_id(),
                                stockCountingTransaction.getLocation_id(),
                                stockCountingTransaction.getType(),null,countList.getMacAddr(),0);
                                stockCountingTransaction.setId((int) generatedId
                        );
                        System.out.println("Successfully inserted!");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
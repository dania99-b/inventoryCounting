package com.example.inventorycountingsystem.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventorycountingsystem.R;

public class ExternalSetting extends AppCompatActivity {
    EditText _syncerKey;
    EditText _syncerSecret;
    Button _saveBTN;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_setting);
        _syncerKey = findViewById(R.id.syncerkey);
        _syncerSecret = findViewById(R.id.syncersecret);
        _saveBTN = findViewById(R.id.saveSettingBTN);
        _saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String syncerKey = _syncerKey.getText().toString();
                String syncerSecret = _syncerSecret.getText().toString();


                SharedPreferences SyncerTokenData = getSharedPreferences("SyncerTokenData", Context.MODE_PRIVATE);
                SharedPreferences.Editor SettingDataEditor = SyncerTokenData.edit();
                SettingDataEditor.putString("SYNCER_KEY", syncerKey);
                SettingDataEditor.putString("SYNCER_SECRET", syncerSecret);
                SettingDataEditor.apply();
                System.out.println("syncer info");
                System.out.println(syncerKey);
                System.out.println(syncerSecret);
                System.out.println(SyncerTokenData.getString("SYNCER_KEY", ""));
                System.out.println(SyncerTokenData.getString("SYNCER_SECRET", ""));
                if (_syncerKey != null || _syncerSecret != null) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                } else {
                    Toast.makeText(ExternalSetting.this, "You should fill all the fields", Toast.LENGTH_SHORT).show();
                    Vibrator vibrator2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator2 != null && vibrator2.hasVibrator()) {
                        vibrator2.vibrate(200); // Vibrate for 200 milliseconds
                    }

                }
            }});
    }
}

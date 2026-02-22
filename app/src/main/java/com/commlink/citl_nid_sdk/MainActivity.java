package com.commlink.citl_nid_sdk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import citl_nid_sdk.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(citl_nid_sdk.R.layout.activity_main);

        Button startButton = findViewById(citl_nid_sdk.R.id.startVerificationButton);
        startButton.setOnClickListener(v -> {
            NIDEnterpriseSDK.startVerification(this, "test_license_key", new NIDCallback() {
                @Override
                public void onSuccess(boolean match, float score, NIDInfo nidInfo) {
                    runOnUiThread(()-> Toast.makeText(MainActivity.this,
                            "Match: " + match + ", Score: " + score,
                            Toast.LENGTH_LONG).show());
                }

                @Override
                public void onError(NIDError error) {
                    runOnUiThread(()-> Toast.makeText(MainActivity.this,
                            "Error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show());

                }
            });
        });
    }
}


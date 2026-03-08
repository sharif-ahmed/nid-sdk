package com.commlink.nid_sdk_demo.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.commlink.citl_nid_sdk.NIDEnterpriseSDK;
import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.model.NIDInfo;
import com.commlink.citl_nid_sdk.utils.BitmapHolder;
import com.commlink.nid_sdk_demo.R;
import com.commlink.nid_sdk_demo.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide default action bar to show custom header
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.startVerificationButton.setOnClickListener(v -> {
            // startVerification();
            String clientId = "276893700486-actcrpoiu5vsjq792ond2f7dqg8fe5kg.apps.ecipher.co";
            String clientSecret = "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";
            tokenRequest(clientId, clientSecret);
        });

        binding.btnReset.setOnClickListener(v -> {
            resetUI();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("MainActivity", "onResume");
    }

    private void startVerification(String apiKey) {
        NIDEnterpriseSDK.startVerification(this, apiKey, new NIDCallback() {
            @Override
            public void onSuccess(NIDInfo nidInfo) {
                // android.util.Log.d("MainActivity", "onSuccess: match=" + match + ", score=" +
                // score);
                runOnUiThread(() -> showResult(true, 5.0f, nidInfo));
            }

            @Override
            public void onError(NIDError error) {
                android.util.Log.e("MainActivity", "onError: " + error.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showResult(boolean match1, float score, NIDInfo info) {
        try {
            binding.cardStart.setVisibility(View.GONE);
            binding.layoutResult.setVisibility(View.VISIBLE);
            //int match = Integer.parseInt(info.getFaceMatchDetail().getFaceMatchCode());
            // Status
            if (info != null && info.isFaceMatched()) {
                binding.imgResultStatus.setImageResource(R.drawable.ic_check_circle);
                binding.imgResultStatus.setColorFilter(ContextCompat.getColor(this, R.color.kyc_primary));
                binding.tvResultStatus.setText("Verified Successfully");
                binding.tvResultStatus.setTextColor(ContextCompat.getColor(this, R.color.kyc_primary));
            } else {
                binding.imgResultStatus.setImageResource(android.R.drawable.ic_delete);
                binding.imgResultStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                binding.tvResultStatus.setText("Verification Failed");
                binding.tvResultStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }

            // Details
            //binding.tvResultScore.setText(String.valueOf(info.getFaceMatchDetail().getFaceMatchScore()));
            binding.tvResultName.setText(info.getName());
            binding.tvResultNid.setText(info.getNidNumber());
            binding.tvResultDob.setText(info.getDateOfBirth());

            // Selfie (Get from BitmapHolder if still available)
            /*if (BitmapHolder.getSelfieBitmap() != null) {
                binding.imgResultSelfie.setImageBitmap(BitmapHolder.getSelfieBitmap());
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetUI() {
        binding.layoutResult.setVisibility(View.GONE);
        binding.cardStart.setVisibility(View.VISIBLE);
        BitmapHolder.clear();
    }

    private void tokenRequest(
            String clientId,
            String clientSecret) {
        // Show progress UI or dialog
        MaterialAlertDialogBuilder progressDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Token")
                .setMessage("Getting Api Key...")
                .setCancelable(false);
        androidx.appcompat.app.AlertDialog dialog = progressDialog.show();

        // EcRequest request = new EcRequest(nidNumber, dob, fullName, fullOcrData);
        TokenRequest request = new TokenRequest(
                clientId,
                clientSecret);

        TokenApiClient.getTokenService(this).getApiToken(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                dialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    TokenResponse tokenResponse = response.body();
                    showSuccessDialog(tokenResponse.getApiKey());
                } else {
                    showErrorDialog(NIDError.E102, "Token API Timeout or connection error.");
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                dialog.dismiss();
                showErrorDialog(NIDError.E102, "Token API Error:  " + t.getMessage());
            }
        });
    }

    private void showSuccessDialog(String apiKey) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.kyc_success_title)
                .setMessage("Token Api Successfully Called Done")
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    dialog.cancel();
                    startVerification(apiKey);
                    // finish();
                })
                .show();
    }

    private void showErrorDialog(String errorCode, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Verification Failed")
                .setMessage("Error Code: " + errorCode + "\n" + message)
                .setPositiveButton("OK", null)
                .show();
    }
}

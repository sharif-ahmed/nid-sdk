package citl_nid_sdk;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import citl_nid_sdk.databinding.ActivityMainBinding;

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
            startVerification();
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

    private void startVerification() {
        NIDEnterpriseSDK.startVerification(this, "test_license_key", new NIDCallback() {
            @Override
            public void onSuccess(boolean match, float score, NIDInfo nidInfo) {
                android.util.Log.d("MainActivity", "onSuccess: match=" + match + ", score=" + score);
                runOnUiThread(() -> showResult(match, score, nidInfo));
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

    private void showResult(boolean match, float score, NIDInfo info) {
        binding.cardStart.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);

        // Status
        if (match) {
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
        binding.tvResultScore.setText(String.format("%.3f", score));
        binding.tvResultName.setText(info.getName());
        binding.tvResultNid.setText(info.getNidNumber());

        // Selfie (Get from BitmapHolder if still available)
        if (BitmapHolder.getSelfieBitmap() != null) {
            binding.imgResultSelfie.setImageBitmap(BitmapHolder.getSelfieBitmap());
        }
    }

    private void resetUI() {
        binding.layoutResult.setVisibility(View.GONE);
        binding.cardStart.setVisibility(View.VISIBLE);
        BitmapHolder.clear();
    }
}


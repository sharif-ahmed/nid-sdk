package com.commlink.citl_nid_sdk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.commlink.citl_nid_sdk.R;
import com.commlink.citl_nid_sdk.databinding.ActivityVerificationBinding;
import com.commlink.citl_nid_sdk.db.NidDatabase;
import com.commlink.citl_nid_sdk.utils.BitmapHolder;
import com.commlink.citl_nid_sdk.utils.CallbackHolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class VerificationStepActivity extends AppCompatActivity {

    private ActivityVerificationBinding binding;
    private boolean isNidInfoCompleted = false;
    private boolean isSelfieCompleted = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("finish", false)) {
            finish();
            return;
        }
        checkStepStatus();
    }

    private void checkStepStatus() {
        executor.execute(() -> {
            NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
            int count = db.nidInfoDao().getAll().size();
            isNidInfoCompleted = count > 0;
            isSelfieCompleted = BitmapHolder.getSelfieBitmap() != null;

            runOnUiThread(this::updateCardStates);
        });
    }

    private void updateCardStates() {
        // Step 2: Selfie
        if (isNidInfoCompleted) {
            binding.cardSelfie.setAlpha(1.0f);
            binding.cardSelfie.setEnabled(true);
        } else {
            binding.cardSelfie.setAlpha(0.4f);
            binding.cardSelfie.setEnabled(true); // keep enabled to show toast

            binding.cardVerify.setAlpha(0.4f);
            binding.cardVerify.setEnabled(true); // keep enabled to show toast
        }

        // Step 3: Verify
        if (isSelfieCompleted) {
            binding.cardVerify.setAlpha(1.0f);
            binding.cardVerify.setEnabled(true);
        } else {
            binding.cardVerify.setAlpha(0.4f);
            binding.cardVerify.setEnabled(true); // keep enabled to show toast
        }
    }

    private void setupClickListeners() {
        binding.cardNidInfo.setOnClickListener(v -> {
            Intent intent = new Intent(VerificationStepActivity.this, NidInfoActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        binding.cardSelfie.setOnClickListener(v -> {
            if (!isNidInfoCompleted) {
                Toast.makeText(this,
                        "Please complete Step 1 (Submit NID Information) first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            SelfieActivity.start(this);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        binding.cardVerify.setOnClickListener(v -> {
            if (!isSelfieCompleted) {
                Toast.makeText(this,
                        "Please complete Step 2 (Take Selfie) first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            VerificationSummaryActivity.start(this);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (isFinishing()) {
            com.commlink.citl_nid_sdk.core.NIDCallback cb = CallbackHolder.getInstance().getCallback();
            if (cb != null) {
                cb.onError(new com.commlink.citl_nid_sdk.model.NIDError(
                        com.commlink.citl_nid_sdk.model.NIDError.Code.USER_CANCELLED, 
                        "Verification cancelled or closed by user"));
            }
            CallbackHolder.getInstance().clear();
        }
        binding = null;
    }
}

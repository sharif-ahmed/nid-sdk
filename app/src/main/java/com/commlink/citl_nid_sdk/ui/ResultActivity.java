package com.commlink.citl_nid_sdk.ui;
import citl_nid_sdk.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.db.NidDatabase;
import com.commlink.citl_nid_sdk.model.NIDInfo;
import com.commlink.citl_nid_sdk.utils.BitmapHolder;
import com.commlink.citl_nid_sdk.utils.CallbackHolder;

import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    private static final String EXTRA_MATCH = "match";
    private static final String EXTRA_SCORE = "score";
    private static final String EXTRA_NID_NUMBER = "nid";
    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_DOB = "dob";
    private static final String EXTRA_NID_INFO = "nid_info";

    private citl_nid_sdk.databinding.ActivityResultBinding binding;

    public static void start(Context context, NIDInfo info) {
        Intent intent = new Intent(context, ResultActivity.class);
        //intent.putExtra(EXTRA_MATCH, match);
        //intent.putExtra(EXTRA_SCORE, score);
        intent.putExtra(EXTRA_NID_NUMBER, info.getNidNumber());
        intent.putExtra(EXTRA_NAME, info.getName());
        intent.putExtra(EXTRA_DOB, info.getDateOfBirth());
        intent.putExtra(EXTRA_NID_INFO, info);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = citl_nid_sdk.databinding.ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean match = getIntent().getBooleanExtra(EXTRA_MATCH, false);
        float score = getIntent().getFloatExtra(EXTRA_SCORE, 0f);
        String nid = getIntent().getStringExtra(EXTRA_NID_NUMBER);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String dob = getIntent().getStringExtra(EXTRA_DOB);
        NIDInfo nidInfo = (NIDInfo) getIntent().getSerializableExtra(EXTRA_NID_INFO);

        populateUI(nidInfo);

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Toast.makeText(getApplicationContext(),
                                "Back disabled during verification",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateUI(NIDInfo nidInfo) {
        // Icon and Colors
        int match = Integer.parseInt(nidInfo.getFaceMatchDetail().getFaceMatchCode());
        if (match == 1) {
            binding.imgStatusIcon.setImageResource(R.drawable.ic_check_circle);
            binding.imgStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.kyc_primary));
            binding.statusText.setText(R.string.nid_result_match);
            binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.kyc_primary));
            binding.statusDescText.setText(R.string.kyc_result_match_desc);
        } else {
            binding.imgStatusIcon.setImageResource(android.R.drawable.ic_delete);
            binding.imgStatusIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            binding.statusText.setText(R.string.nid_result_not_match);
            binding.statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            binding.statusDescText.setText(R.string.kyc_result_no_match_desc);
        }

        // Selfie
        if (BitmapHolder.getSelfieBitmap() != null) {
            binding.imgResultSelfie.setImageBitmap(BitmapHolder.getSelfieBitmap());
        }

        // Details
        //binding.tvResultScore.setText(String.format("%.3f", score));
        binding.tvResultCode.setText(String.valueOf(nidInfo.getFaceMatchDetail().getFaceMatchCode()));
        binding.tvResultName.setText(nidInfo.getName());
        binding.tvResultNid.setText(nidInfo.getNidNumber());
        binding.tvResultDob.setText(nidInfo.getDateOfBirth());
        binding.tvResultScore.setText(String.valueOf(nidInfo.getFaceMatchDetail().getFaceMatchScore()));

        binding.doneButton.setOnClickListener(v -> {
            //NIDInfo info = new NIDInfo(nid, name, dob);
            NIDCallback cb = CallbackHolder.getInstance().getCallback();
            if (cb != null) {
                cb.onSuccess(nidInfo);
            }
            // Clear high-level callback
            CallbackHolder.getInstance().clear();
            BitmapHolder.clear();
            Executors.newSingleThreadExecutor().execute(() -> {
                NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
                db.nidInfoDao().deleteAll();
            });
            // Return to MainActivity by finishing all SDK activities in the task
            Intent intent = new Intent(this, VerificationStepActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("finish", true);
            startActivity(intent);
            finish();
        });
    }
}


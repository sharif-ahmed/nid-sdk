package citl_nid_sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.internal.BaseImplementation;

import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    private static final String EXTRA_MATCH = "match";
    private static final String EXTRA_SCORE = "score";
    private static final String EXTRA_NID_NUMBER = "nid";
    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_DOB = "dob";

    private citl_nid_sdk.databinding.ActivityResultBinding binding;

    public static void start(Context context, boolean match, float score, NIDInfo info) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_MATCH, match);
        intent.putExtra(EXTRA_SCORE, score);
        intent.putExtra(EXTRA_NID_NUMBER, info.getNidNumber());
        intent.putExtra(EXTRA_NAME, info.getName());
        intent.putExtra(EXTRA_DOB, info.getDateOfBirth());
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

        populateUI(match, score, nid, name, dob);
    }

    private void populateUI(boolean match, float score, String nid, String name, String dob) {
        // Icon and Colors
        if (match) {
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
        binding.tvResultScore.setText(String.format("%.3f", score));
        binding.tvResultName.setText(name);
        binding.tvResultNid.setText(nid);
        binding.tvResultDob.setText(dob);

        binding.doneButton.setOnClickListener(v -> {
            NIDInfo info = new NIDInfo(nid, name, dob);
            NIDCallback cb = CallbackHolder.getInstance().getCallback();
            if (cb != null) {
                cb.onSuccess(match, score, info);
            }
            // Clear high-level callback
            CallbackHolder.getInstance().clear();
            BitmapHolder.clear();
            Executors.newSingleThreadExecutor().execute(() -> {
                NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
                db.nidInfoDao().deleteAll();
            });
            // Return to MainActivity by finishing all SDK activities in the task
            Intent intent = new Intent(this, VerificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("finish", true);
            startActivity(intent);
            finish();
        });
    }
}


package citl_nid_sdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import citl_nid_sdk.databinding.ActivityVerificationSummaryBinding;

public class VerificationSummaryActivity extends AppCompatActivity {

    private ActivityVerificationSummaryBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void start(Context context) {
        context.startActivity(new Intent(context, VerificationSummaryActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        loadData();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnConfirmVerify.setOnClickListener(v -> {
            ProcessingActivity.start(this);
            finish();
        });
    }

    private void loadData() {
        // Load Selfie
        if (BitmapHolder.getSelfieBitmap() != null) {
            binding.imgSelfie.setImageBitmap(BitmapHolder.getSelfieBitmap());
        }

        // Load NID Info from DB
        executor.execute(() -> {
            NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
            List<NidInfoEntity> entities = db.nidInfoDao().getAll();
            if (!entities.isEmpty()) {
                NidInfoEntity entity = entities.get(entities.size() - 1); // Get latest

                runOnUiThread(() -> {
                    binding.tvFullName.setText(entity.getFullName());
                    binding.tvNidNumber.setText(entity.getNidNumber());
                    binding.tvDob.setText(entity.getDateOfBirth());

                    if (entity.getFrontImagePath() != null) {
                        binding.imgNidFront.setImageBitmap(BitmapFactory.decodeFile(entity.getFrontImagePath()));
                        BitmapHolder.setNidBitmap(BitmapFactory.decodeFile(entity.getFrontImagePath()));
                    }
                    if (entity.getBackImagePath() != null) {
                        binding.imgNidBack.setImageBitmap(BitmapFactory.decodeFile(entity.getBackImagePath()));
                    }
                });
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        binding = null;
    }
}

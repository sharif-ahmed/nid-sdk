package citl_nid_sdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import citl_nid_sdk.databinding.ActivityVerificationSummaryBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationSummaryActivity extends AppCompatActivity {

    private ActivityVerificationSummaryBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private NidInfoEntity currentEntity;

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
            /*if (currentEntity != null) {
                performFaceMatch(currentEntity);
            }*/
            ProcessingActivity.start(this);
        });
    }

    private void performFaceMatch(NidInfoEntity entity) {
        Bitmap selfie = BitmapHolder.getSelfieBitmap();
        if (selfie == null) {
            showErrorDialog(NIDError.E103, "Selfie not found. Please restart the flow.");
            return;
        }

        // Show progress UI or dialog
        MaterialAlertDialogBuilder progressDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Final Verification")
                .setMessage("Performing face matching...")
                .setCancelable(false);
        androidx.appcompat.app.AlertDialog dialog = progressDialog.show();

        String selfieBase64 = BitmapUtils.toBase64(selfie);
        FaceMatchRequest request = new FaceMatchRequest(
                entity.getNidNumber(),
                entity.getDateOfBirth(),
                entity.getFullName(),
                selfieBase64
        );

        ApiClient.getService(this).matchFace(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SdkResponse> call, Response<SdkResponse> response) {
                dialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    SdkResponse sdkResponse = response.body();
                    relayResponse(sdkResponse);
                } else {
                    showErrorDialog(NIDError.E105, "Face Match API Timeout or error.");
                }
            }

            @Override
            public void onFailure(Call<SdkResponse> call, Throwable t) {
                dialog.dismiss();
                showErrorDialog(NIDError.E105, "Face Match API failure: " + t.getMessage());
            }
        });
    }

    private void relayResponse(SdkResponse response) {
        NIDCallback cb = CallbackHolder.getInstance().getCallback();
        if (cb != null) {
            // Check success and pass back
            boolean match = response.faceMatch != null ? response.faceMatch : false;
            float score = 0.0f; // Score is not explicitly in SdkResponse but we can add it if needed
            
            // Relaying the rich info via the existing callback for now
            // or mapping to the User's preferred format if we update the interface
            NIDInfo info = new NIDInfo(response.ocrData != null ? response.ocrData.nidNumber : "", 
                                     response.ocrData != null ? response.ocrData.nameEnglish : "", 
                                     response.ocrData != null ? response.ocrData.dateOfBirth : "");
            
            if (response.ocrData != null) {
                info.setNameBangla(response.ocrData.nameBangla);
                info.setFatherName(response.ocrData.fatherName);
                info.setMotherName(response.ocrData.motherName);
                info.setAddressBangla(response.ocrData.address);
            }

            cb.onSuccess(match, score, info);
        }

        // Show Result screen or finish
        if ("SUCCESS".equalsIgnoreCase(response.status)) {
            NIDInfo finalInfo = new NIDInfo(response.ocrData.nidNumber, response.ocrData.nameEnglish, response.ocrData.dateOfBirth);
            ResultActivity.start(this, response.faceMatch != null ? response.faceMatch : false, 0.0f, finalInfo);
            finish();
        } else {
            showErrorDialog(response.errorCode != null ? response.errorCode : NIDError.E500, response.message);
        }
    }

    private void showErrorDialog(String errorCode, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Verification Error")
                .setMessage("Error Code: " + errorCode + "\n" + message)
                .setPositiveButton("OK", null)
                .show();
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
                currentEntity = entity;

                runOnUiThread(() -> {
                    binding.tvFullName.setText(entity.getFullName());
                    binding.tvNameBangla.setText(entity.getNameBangla());
                    binding.tvNidNumber.setText(entity.getNidNumber());
                    binding.tvFatherName.setText(entity.getFatherName());
                    binding.tvFatherNameBangla.setText(entity.getFatherNameBangla());
                    binding.tvMotherName.setText(entity.getMotherName());
                    binding.tvMotherNameBangla.setText(entity.getMotherNameBangla());
                    binding.tvAddressBangla.setText(entity.getAddressBangla());
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

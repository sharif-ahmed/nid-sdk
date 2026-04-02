package com.commlink.citl_nid_sdk.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.commlink.citl_nid_sdk.R;
import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.databinding.ActivityVerificationSummaryBinding;
import com.commlink.citl_nid_sdk.db.NidDatabase;
import com.commlink.citl_nid_sdk.model.EcValidation;
import com.commlink.citl_nid_sdk.model.FaceMatchRequest;
import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.model.NIDInfo;
import com.commlink.citl_nid_sdk.model.NidEcVerifyResponse;
import com.commlink.citl_nid_sdk.model.NidFaceVerificationRequest;
import com.commlink.citl_nid_sdk.model.NidFaceVerificationResponse;
import com.commlink.citl_nid_sdk.model.NidInfoEntity;
import com.commlink.citl_nid_sdk.model.Result;
import com.commlink.citl_nid_sdk.network.ApiClient;
import com.commlink.citl_nid_sdk.utils.BitmapHolder;
import com.commlink.citl_nid_sdk.utils.BitmapUtils;
import com.commlink.citl_nid_sdk.utils.CallbackHolder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationSummaryActivity extends AppCompatActivity {

    private ActivityVerificationSummaryBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private NidInfoEntity currentEntity;
    private long backPressedTime;

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

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (backPressedTime + 2000 > System.currentTimeMillis()) {
                            showExitConfirmationDialog();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Press back again to exit",
                                    Toast.LENGTH_SHORT).show();
                        }
                        backPressedTime = System.currentTimeMillis();
                    }
                });
    }

    private void showExitConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit Verification")
                .setMessage("Are you sure you want to exit the verification process?")
                .setPositiveButton("Yes", (dialog, which) ->{
                    Intent intent = new Intent(this, VerificationStepActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("finish", true);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnConfirmVerify.setOnClickListener(v -> {
            if (currentEntity != null) {
                performFaceMatch(currentEntity);
            }
            // ProcessingActivity.start(this);
        });

        binding.btnEditSelfie.setOnClickListener(v -> {
            SelfieActivity.start(this);
            finish();
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
        AlertDialog dialog = progressDialog.show();

        String selfieBase64 = BitmapUtils.toBase64(selfie);
        FaceMatchRequest request = new FaceMatchRequest(
                entity.getNidNumber(),
                entity.getDateOfBirth(),
                entity.getFullName(),
                selfieBase64);

        NidFaceVerificationRequest verificationRequest = new NidFaceVerificationRequest(
                entity.getNidNumber(),
                entity.getFullName(),
                entity.getDateOfBirth(),
                true,
                entity.getTransactionId(),
                selfieBase64);

        String apiKey = CallbackHolder.getInstance().getLicenseKey();

        /*ApiClient.getService(this).verifyFace(apiKey, verificationRequest)
                .enqueue(new Callback<NidFaceVerificationResponse>() {
                    @Override
                    public void onResponse(Call<NidFaceVerificationResponse> call,
                            Response<NidFaceVerificationResponse> response) {
                        dialog.dismiss();
                        NidFaceVerificationResponse verificationResponse = response.body();
                        if (response.isSuccessful() && response.body() != null) {
                            if (verificationResponse != null && verificationResponse.getData() != null) {
                                showStatusDialog(
                                        true,
                                        String.valueOf(verificationResponse.result.getStatusCode()),
                                        "Face verification API called successfully. Press OK to view the result",
                                        () -> relayResponse(verificationResponse));
                            } else {
                                if (verificationResponse != null && verificationResponse.getResult() != null) {
                                    if (verificationResponse.getResult().getIsError()
                                            && !verificationResponse.getData().isFaceMatched()) {
                                        binding.btnEditSelfie.setVisibility(View.VISIBLE);
                                        showStatusDialog(
                                                false,
                                                String.valueOf(verificationResponse.getResult().getStatusCode()),
                                                verificationResponse.getResult().getErrorMsg(),
                                                () -> {
                                                });
                                    }
                                }
                            }
                        } else {
                            if (response.code() == 401) {
                                showStatusDialog(
                                        false,
                                        String.valueOf(verificationResponse != null
                                                ? verificationResponse.result.getStatusCode()
                                                : 401),
                                        verificationResponse != null ? verificationResponse.result.getErrorMsg()
                                                : "Unauthorized",
                                        () -> {
                                            Intent intent = new Intent(VerificationSummaryActivity.this,
                                                    VerificationStepActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.putExtra("finish", true);
                                            startActivity(intent);
                                            finish();
                                        });
                            } else {
                                showErrorDialog(String.valueOf(response.code()), response.message());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NidFaceVerificationResponse> call, Throwable t) {
                        dialog.dismiss();
                        showErrorDialog(NIDError.E105, "Face Match API failure: " + t.getMessage());
                    }
                });*/

        ApiClient.getService(this).verifyFace(apiKey, verificationRequest).enqueue(new Callback<NidFaceVerificationResponse>() {
            @Override
            public void onResponse(Call<NidFaceVerificationResponse> call, Response<NidFaceVerificationResponse> response) {
                try {
                    dialog.dismiss();
                    NidFaceVerificationResponse verificationResponse = response.body();
                    Result<?> result = verificationResponse != null ? verificationResponse.getResult() : null;
                    String error_Msg = result != null && result.getErrorMsg() != null ? result.getErrorMsg() : response.message();
                    int statusCode = result != null && result.getStatusCode() != null ? result.getStatusCode() : response.code();
                    switch (statusCode) {
                        case 1:
                            binding.btnEditSelfie.setVisibility(View.VISIBLE);
                            binding.cvSelfie.setStrokeColor(getResources().getColor(R.color.kyc_error));
                            binding.cvSelfie.setStrokeWidth(10);
                            showStatusDialog(
                                    false,
                                    String.valueOf(statusCode),
                                    error_Msg,
                                    () -> {
                                        NIDCallback cb = CallbackHolder.getInstance().getCallback();
                                        if (cb != null) {
                                            cb.onError(new NIDError(NIDError.Code.FACE_MATCH_FAILED, "Face Verification Failed", NIDError.E104));
                                        }
                                        CallbackHolder.getInstance().clear();
                                        Intent intent = new Intent(VerificationSummaryActivity.this, VerificationStepActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("finish", true);
                                        startActivity(intent);
                                        finish();
                                    }
                            );
                            break;
                        case 200:
                            if (verificationResponse != null && verificationResponse.getData() != null) {
                                if (!verificationResponse.getResult().getIsError() && verificationResponse.getData().isFaceMatched()){
                                    showStatusDialog(
                                            true,
                                            String.valueOf(verificationResponse.result.getStatusCode()),
                                            "Face verification API called successfully. Press OK to view the result",
                                            () -> relayResponse(verificationResponse));
                                } else {
                                    binding.btnEditSelfie.setVisibility(View.VISIBLE);
                                    binding.cvSelfie.setStrokeColor(getResources().getColor(R.color.kyc_error));
                                    binding.cvSelfie.setStrokeWidth(10);
                                    showStatusDialog(
                                            false,
                                            String.valueOf(verificationResponse.getResult().getStatusCode()),
                                            "Face Not Matched",
                                            () -> {
                                                NIDCallback cb = CallbackHolder.getInstance().getCallback();
                                                if (cb != null) {
                                                    cb.onError(new NIDError(NIDError.Code.FACE_MATCH_FAILED, "Face Verification Failed", NIDError.E104));
                                                }
                                                CallbackHolder.getInstance().clear();
                                                Intent intent = new Intent(VerificationSummaryActivity.this, VerificationStepActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("finish", true);
                                                startActivity(intent);
                                                finish();
                                            }
                                    );
                                    //showErrorDialog(String.valueOf(statusCode), "Face Not Matched");
                                }
                            } else {
                                String errorMsg = result != null && result.getErrorMsg() != null ? result.getErrorMsg() : response.message();
                                //showErrorDialog(String.valueOf(statusCode), errorMsg);
                                showStatusDialog(
                                        false,
                                        String.valueOf(statusCode),
                                        errorMsg,
                                        () -> {
                                            NIDCallback cb = CallbackHolder.getInstance().getCallback();
                                            if (cb != null) {
                                                cb.onError(new NIDError(NIDError.Code.EMPTY_DATA_ERROR, errorMsg, NIDError.E106));
                                            }
                                            CallbackHolder.getInstance().clear();
                                            Intent intent = new Intent(VerificationSummaryActivity.this, VerificationStepActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.putExtra("finish", true);
                                            startActivity(intent);
                                            finish();
                                        }
                                );
                            }
                            break;

                        case 401: // Unauthorized
                            showStatusDialog(
                                    false,
                                    String.valueOf(statusCode),
                                    result != null && result.getErrorMsg() != null ? result.getErrorMsg() : "Unauthorized",
                                    () -> {
                                        com.commlink.citl_nid_sdk.core.NIDCallback cb = CallbackHolder.getInstance().getCallback();
                                        if (cb != null) {
                                            cb.onError(new com.commlink.citl_nid_sdk.model.NIDError(com.commlink.citl_nid_sdk.model.NIDError.Code.NETWORK_ERROR, "Unauthorized API Error"));
                                        }
                                        CallbackHolder.getInstance().clear();
                                        Intent intent = new Intent(VerificationSummaryActivity.this, VerificationStepActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("finish", true);
                                        startActivity(intent);
                                        finish();
                                    });
                            break;
                        default: // Any other error
                            String errorMsg1 = result != null && result.getErrorMsg() != null ? result.getErrorMsg() : response.message();
                            showStatusDialog(
                                    false,
                                    String.valueOf(statusCode),
                                    errorMsg1,
                                    () -> {
                                        com.commlink.citl_nid_sdk.core.NIDCallback cb = CallbackHolder.getInstance().getCallback();
                                        if (cb != null) {
                                            cb.onError(new com.commlink.citl_nid_sdk.model.NIDError(com.commlink.citl_nid_sdk.model.NIDError.Code.NETWORK_ERROR, "API Error: " + errorMsg1, com.commlink.citl_nid_sdk.model.NIDError.E105));
                                        }
                                        CallbackHolder.getInstance().clear();
                                        Intent intent = new Intent(VerificationSummaryActivity.this, VerificationStepActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("finish", true);
                                        startActivity(intent);
                                        finish();
                                    });
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<NidFaceVerificationResponse> call, Throwable t) {
                dialog.dismiss();
                showStatusDialog(
                        false,
                        "Error",
                        t.getMessage(),
                        () -> {
                            com.commlink.citl_nid_sdk.core.NIDCallback cb = CallbackHolder.getInstance().getCallback();
                            if (cb != null) {
                                cb.onError(new com.commlink.citl_nid_sdk.model.NIDError(com.commlink.citl_nid_sdk.model.NIDError.Code.NETWORK_ERROR, "API Timeout/Failure", com.commlink.citl_nid_sdk.model.NIDError.E105));
                            }
                            CallbackHolder.getInstance().clear();
                            Intent intent = new Intent(VerificationSummaryActivity.this, VerificationStepActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("finish", true);
                            startActivity(intent);
                            finish();
                        }
                );
            }
        });
    }

    private void relayResponse(NidFaceVerificationResponse response) {
        try {
            NIDCallback cb = CallbackHolder.getInstance().getCallback();
            if (cb != null) {
                NIDInfo info = new NIDInfo(
                        response.getData().getOcrData() != null ? response.getData().getOcrData().getNidNumber() : "",
                        response.getData().getOcrData() != null ? response.getData().getOcrData().getNameEnglish() : "",
                        response.getData().getOcrData() != null ? response.getData().getOcrData().getDateOfBirth()
                                : "");

                if (response.getData().getOcrData() != null) {
                    /*
                     * info.setNameBangla(response.getData().getOcrData().getNameBangla());
                     * info.setFatherName(response.getData().getOcrData().getFatherName());
                     * info.setMotherName(response.getData().getOcrData().getMotherName());
                     * info.setAddressBangla(response.getData().getOcrData().getAddress());
                     */
                    info.setOcrData(response.getData().getOcrData());
                }
                if (response.getData().getEcValidation() != null) {
                    info.setEcValidation(response.getData().getEcValidation());
                }
                if (response.getData().getFaceMatchDetail() != null) {
                    info.setFaceMatchDetail(response.getData().getFaceMatchDetail());
                }
                cb.onSuccess(info);
                ResultActivity.start(this, info);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String errorCode, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Verification Error")
                .setMessage("Error Code: " + errorCode + "\n" + message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccessDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.kyc_success_title)
                .setMessage(message)
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
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
            NidInfoEntity entity = db.nidInfoDao().getLastEntry();
            /*if (!entities.isEmpty()) {
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
            }*/

            if (entity != null) {
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

    private void showStatusDialog(boolean isSuccess, String code, String message,
            NidInfoActivity.DialogActionListener listener) {

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_verification_status, null);

        ImageView imgStatus = view.findViewById(R.id.imgStatus);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvErrorCode = view.findViewById(R.id.tvErrorCode);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        if (isSuccess) {
            imgStatus.setImageResource(R.drawable.ic_green_check);
            tvTitle.setText("Verification Successful");
            tvTitle.setTextColor(getResources().getColor(R.color.kyc_success));
        } else {
            imgStatus.setImageResource(R.drawable.ic_cross_circle);
            tvTitle.setText("Verification Failed");
            tvTitle.setTextColor(getResources().getColor(R.color.kyc_error));
        }

        tvErrorCode.setText("Code: " + code);
        tvMessage.setText(message);

        /*new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onPositiveClick();
                    }
                    //finish();
                })
                .setNegativeButton(
                        isSuccess ? "Cancel" : "Retry",
                        (dialog, which) -> dialog.dismiss())
                .show();*/

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onPositiveClick();
                    }
                });

        // Only add negative button if NOT success
        if (!isSuccess) {
            builder.setNegativeButton("Retry", (dialog, which) -> dialog.dismiss());
        }
        builder.show();
    }
}

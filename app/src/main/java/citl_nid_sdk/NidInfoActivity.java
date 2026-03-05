package citl_nid_sdk;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import citl_nid_sdk.databinding.ActivityNidInfoBinding;


public class NidInfoActivity extends AppCompatActivity {

    private ActivityNidInfoBinding binding;
    private String frontImagePath = null;
    private String backImagePath = null;
    private String frontOcrRawData = null;
    private String backOcrRawData = null;
    private ExecutorService executor;

    // Launcher for CaptureNIDActivity — receives image path back
    private final ActivityResultLauncher<Intent> captureNidLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String imagePath = result.getData().getStringExtra(CaptureNIDActivity.EXTRA_IMAGE_PATH);
                    String side = result.getData().getStringExtra(CaptureNIDActivity.EXTRA_CAPTURE_SIDE);

                    if (imagePath != null && "front".equals(side)) {
                        frontImagePath = imagePath;
                        Bitmap nidFront = BitmapFactory.decodeFile(frontImagePath);
                        binding.imgNidFront.setImageBitmap(nidFront);
                        binding.imgNidFront.setVisibility(View.VISIBLE);
                        binding.txtFrontPlaceholder.setVisibility(View.GONE);
                        NIDOCRProcessor ocr = new NIDOCRProcessor(this);
                        executor.execute(() -> {
                            ocr.process(nidFront, new NIDOCRProcessor.Callback() {
                                @Override
                                public void onSuccess(NIDInfo info) {
                                    frontOcrRawData = info.getOcrRawData();
                                    Log.d("NID_INFO_OCR", frontOcrRawData);
                                    binding.etNidNumber.setText(info.getNidNumber());
                                    binding.etDob.setText(info.getDateOfBirth());
                                    binding.etFullName.setText(info.getName());
                                    binding.etFullNameBangla.setText(info.getNameBangla());
                                    //binding.etFatherName.setText(info.getFatherName());
                                    binding.etFatherNameBangla.setText(info.getFatherNameBangla());
                                    //binding.etMotherName.setText(info.getMotherName());
                                    binding.etMotherNameBangla.setText(info.getMotherNameBangla());
                                }

                                @Override
                                public void onError(Exception e) {
                                }
                            });
                        });
                    } else if (imagePath != null && "back".equals(side)) {
                        backImagePath = imagePath;
                        Bitmap nidBack = BitmapFactory.decodeFile(backImagePath);
                        binding.imgNidBack.setImageBitmap(nidBack);
                        binding.imgNidBack.setVisibility(View.VISIBLE);
                        binding.txtBackPlaceholder.setVisibility(View.GONE);

                        // Extract address from back side
                        NIDOCRProcessor ocr = new NIDOCRProcessor(this);
                        executor.execute(() -> {
                            ocr.process(nidBack, new NIDOCRProcessor.Callback() {
                                @Override
                                public void onSuccess(NIDInfo info) {
                                    backOcrRawData = info.getOcrRawData();
                                    if (info.getAddressBangla() != null && !info.getAddressBangla().isEmpty()) {
                                        binding.etAddressBangla.setText(info.getAddressBangla());
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                }
                            });
                        });
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNidInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        executor = Executors.newSingleThreadExecutor();

        setupUI();
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Capture front image — launch CaptureNIDActivity
        binding.btnCaptureFront.setOnClickListener(v -> {
            Intent intent = CaptureNIDActivity.createIntent(this, "front");
            captureNidLauncher.launch(intent);
        });

        // Capture back image — launch CaptureNIDActivity
        binding.btnCaptureBack.setOnClickListener(v -> {
            Intent intent = CaptureNIDActivity.createIntent(this, "back");
            captureNidLauncher.launch(intent);
        });

        // Date of Birth picker
        binding.etDob.setOnClickListener(v -> showDatePicker());

        // Submit
        binding.btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.Theme_NIDVerification,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    binding.etDob.setText(sdf.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR) - 25,
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void validateAndSubmit() {
        boolean isValid = true;

        // Validate front image
        if (frontImagePath == null) {
            Toast.makeText(this, R.string.kyc_error_front_image, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate back image - optional
        if (backImagePath == null) {
            Toast.makeText(this, R.string.kyc_error_back_image, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate NID number
        String nidNumber = binding.etNidNumber.getText() != null
                ? binding.etNidNumber.getText().toString().trim() : "";
        if (nidNumber.isEmpty()) {
            binding.tilNidNumber.setError(getString(R.string.kyc_error_nid_number));
            isValid = false;
        } else {
            binding.tilNidNumber.setError(null);
        }

        // Validate full name
        String fullName = binding.etFullName.getText() != null
                ? binding.etFullName.getText().toString().trim() : "";
        if (fullName.isEmpty()) {
            binding.tilFullName.setError(getString(R.string.kyc_error_full_name));
            isValid = false;
        } else {
            binding.tilFullName.setError(null);
        }

        // Bangla Name, Father Name, Mother Name can be optional or validated as needed
        String nameBangla = binding.etFullNameBangla.getText() != null
                ? binding.etFullNameBangla.getText().toString().trim() : "";

        // Validate father name
        /*String fatherName = binding.etFatherName.getText() != null
                ? binding.etFatherName.getText().toString().trim() : "";
        if (fatherName.isEmpty()) {
            binding.tilFatherName.setError(getString(R.string.kyc_error_father_name));
            isValid = false;
        } else {
            binding.tilFatherName.setError(null);
        }*/

        String fatherNameBangla = binding.etFatherNameBangla.getText() != null
                ? binding.etFatherNameBangla.getText().toString().trim() : "";

        // Validate mother name
        /*String motherName = binding.etMotherName.getText() != null
                ? binding.etMotherName.getText().toString().trim() : "";
        if (motherName.isEmpty()) {
            binding.tilMotherName.setError(getString(R.string.kyc_error_mother_name));
            isValid = false;
        } else {
            binding.tilMotherName.setError(null);
        }*/

        String motherNameBangla = binding.etMotherNameBangla.getText() != null
                ? binding.etMotherNameBangla.getText().toString().trim() : "";

        String addressBangla = binding.etAddressBangla.getText() != null
                ? binding.etAddressBangla.getText().toString().trim() : "";

        // Validate DOB
        String dob = binding.etDob.getText() != null
                ? binding.etDob.getText().toString().trim() : "";
        if (dob.isEmpty()) {
            binding.tilDob.setError(getString(R.string.kyc_error_dob));
            isValid = false;
        } else {
            binding.tilDob.setError(null);
        }

        if (!isValid) return;
        String formatedDate = DateConverter.convertDate(dob);
        String nid = nidNumber.trim().toString();
        //saveToDatabase(nidNumber, fullName, nameBangla, fatherNameBangla, motherNameBangla, addressBangla, formatedDate);
        performECValidation(nid, fullName, nameBangla, fatherNameBangla, motherNameBangla, addressBangla, formatedDate, true);
    }

    private void performECValidation(
            String nidNumber,
            String fullName,
            String nameBangla,
            String fatherNameBangla,
            String motherNameBangla,
            String addressBangla,
            String dob,
            boolean isFaceMatchRequired
    ) {
        // Show progress UI or dialog
        MaterialAlertDialogBuilder progressDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Verifying")
                .setMessage("Validating NID with EC service...")
                .setCancelable(false);
        androidx.appcompat.app.AlertDialog dialog = progressDialog.show();

        String fullOcrData = (frontOcrRawData != null ? frontOcrRawData : "") + "\n" + (backOcrRawData != null ? backOcrRawData : "");
        //EcRequest request = new EcRequest(nidNumber, dob, fullName, fullOcrData);
        NidECVerifyRequest request = new NidECVerifyRequest(
                nidNumber,
                fullName,
                nameBangla,
                dob,
                fatherNameBangla,
                motherNameBangla,
                addressBangla,
                isFaceMatchRequired
        );

        String apiKey = CallbackHolder.getInstance().getLicenseKey();

        ApiClient.getService(this).validateEC(apiKey, request).enqueue(new Callback<NidEcVerifyResponse>() {
            @Override
            public void onResponse(Call<NidEcVerifyResponse> call, Response<NidEcVerifyResponse> response) {
                try {
                    dialog.dismiss();
                    NidEcVerifyResponse ecVerifyResponse = response.body();
                    if (response.isSuccessful() && response.body() != null) {
                        if (ecVerifyResponse.getData() != null) {
                            showStatusDialog(
                                    true,
                                    String.valueOf(response.code()),
                                    "NID EC Verify Successful!!",
                                    () -> {
                                        String txId = ecVerifyResponse.getData().getTransactionId();
                                        saveToDatabase(nidNumber, fullName, nameBangla, fatherNameBangla, motherNameBangla, addressBangla, dob, txId);
                                        SelfieActivity.start(NidInfoActivity.this);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                        finish();                                    }
                            );
                        } else {
                            showStatusDialog(
                                    false,
                                    String.valueOf(response.code()),
                                    "Api SUCCESS, But No Data Found For Success Response",
                                    () -> {
                                        String txId2 = "4c8f6199-1427-4385-9789-81066ea5cd9a";
                                        saveToDatabase(nidNumber, fullName, nameBangla, fatherNameBangla, motherNameBangla, addressBangla, dob, txId2);
                                        SelfieActivity.start(NidInfoActivity.this);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                        finish();                                    }
                            );
                        }
                    } else {
                        if (response.code() == 401) {
                            showStatusDialog(
                                    false,
                                    String.valueOf(ecVerifyResponse != null ? ecVerifyResponse.result.getStatusCode() : 401),
                                    ecVerifyResponse != null ? ecVerifyResponse.result.getErrorMsg() : "Unauthorized",
                                    () -> {
                                        Intent intent = new Intent(NidInfoActivity.this, VerificationStepActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("finish", true);
                                        startActivity(intent);
                                        finish();
                                    }
                            );
                        } else {
                            showErrorDialog(String.valueOf(response.code()), response.message());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<NidEcVerifyResponse> call, Throwable t) {
                dialog.dismiss();
                showErrorDialog("Api Error", t.getMessage());
            }
        });
    }

    /*private void saveToDatabase(String nidNumber, String fullName, String nameBangla, String fatherName, String fatherNameBangla, String motherName, String motherNameBangla, String dob) {
        NidInfoEntity entity = new NidInfoEntity();
        entity.setNidNumber(nidNumber);
        entity.setFullName(fullName);
        entity.setNameBangla(nameBangla);
        entity.setFatherName(fatherName);
        entity.setFatherNameBangla(fatherNameBangla);
        entity.setMotherName(motherName);
        entity.setMotherNameBangla(motherNameBangla);
        entity.setDateOfBirth(dob);
        entity.setFrontImagePath(frontImagePath);
        entity.setBackImagePath(backImagePath);

        executor.execute(() -> {
            NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
            db.nidInfoDao().insert(entity);

            runOnUiThread(this::showSuccessDialog);
        });
    }*/

    private void saveToDatabase(String nidNumber, String fullName, String nameBangla, String fatherNameBangla, String motherNameBangla, String addressBangla, String dob, String txId) {
        NidInfoEntity entity = new NidInfoEntity();
        entity.setNidNumber(nidNumber);
        entity.setFullName(fullName);
        entity.setNameBangla(nameBangla);
        entity.setFatherNameBangla(fatherNameBangla);
        entity.setMotherNameBangla(motherNameBangla);
        entity.setAddressBangla(addressBangla);
        entity.setDateOfBirth(dob);
        entity.setFrontImagePath(frontImagePath);
        entity.setBackImagePath(backImagePath);
        entity.setOcrRawDataFront(frontOcrRawData);
        entity.setOcrRawDataBack(backOcrRawData);
        entity.setTransactionId(txId);
        /*entity.setPhotoBase64St(base64);
        entity.setFaceMatchRequired(isFaceMatchRequired);*/

        executor.execute(() -> {
            NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
            db.nidInfoDao().insert(entity);

            runOnUiThread(this::showSuccessDialog);
        });
    }

    private void saveToDatabase(String nidNumber, String fullName, String nameBangla, String fatherNameBangla, String motherNameBangla, String dob) {
        NidInfoEntity entity = new NidInfoEntity();
        entity.setNidNumber(nidNumber);
        entity.setFullName(fullName);
        entity.setNameBangla(nameBangla);
        entity.setFatherNameBangla(fatherNameBangla);
        entity.setMotherNameBangla(motherNameBangla);
        entity.setDateOfBirth(dob);
        entity.setFrontImagePath(frontImagePath);
        entity.setBackImagePath(backImagePath);

        executor.execute(() -> {
            NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
            db.nidInfoDao().insert(entity);

            runOnUiThread(this::showSuccessDialog);
        });
    }

    private void showErrorDialog(String errorCode, String message) {

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_verification_error, null);

        TextView tvErrorCode = view.findViewById(R.id.tvErrorCode);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        tvErrorCode.setText("Error Code: " + errorCode);
        tvMessage.setText(message);

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showErrorDialog(boolean status, String errorCode, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Verification Failed")
                .setIcon(R.drawable.ic_cross_circle)
                .setMessage("Error Code: " + errorCode + "\n" + message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.kyc_success_title)
                .setMessage(R.string.kyc_success_message)
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    SelfieActivity.start(this);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                })
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
                    SelfieActivity.start(this);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                })
                .show();
    }

    public interface DialogActionListener {
        void onPositiveClick();
    }

    private void showStatusDialog(boolean isSuccess, String code, String message, DialogActionListener listener) {

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

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onPositiveClick();
                    }
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
        binding = null;
    }
}

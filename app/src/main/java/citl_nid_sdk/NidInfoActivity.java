package citl_nid_sdk;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
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

import citl_nid_sdk.databinding.ActivityNidInfoBinding;


public class NidInfoActivity extends AppCompatActivity {

    private ActivityNidInfoBinding binding;
    private String frontImagePath = null;
    private String backImagePath = null;
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
                        executor.execute(()->{
                            ocr.process(nidFront, new NIDOCRProcessor.Callback() {
                                @Override
                                public void onSuccess(NIDInfo info) {
                                    binding.etNidNumber.setText(info.getNidNumber());
                                    binding.etDob.setText(info.getDateOfBirth());
                                    binding.etFullName.setText(info.getName());
                                }
                                @Override
                                public void onError(Exception e) {}
                            });
                        });
                    } else if (imagePath != null && "back".equals(side)) {
                        backImagePath = imagePath;
                        binding.imgNidBack.setImageBitmap(BitmapFactory.decodeFile(backImagePath));
                        binding.imgNidBack.setVisibility(View.VISIBLE);
                        binding.txtBackPlaceholder.setVisibility(View.GONE);
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

        // Validate back image
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

        // Save to Room database
        saveToDatabase(nidNumber, fullName, dob);
    }

    private void saveToDatabase(String nidNumber, String fullName, String dob) {
        NidInfoEntity entity = new NidInfoEntity();
        entity.setNidNumber(nidNumber);
        entity.setFullName(fullName);
        entity.setDateOfBirth(dob);
        entity.setFrontImagePath(frontImagePath);
        entity.setBackImagePath(backImagePath);

        executor.execute(() -> {
            NidDatabase db = NidDatabase.getDatabase(getApplicationContext());
            db.nidInfoDao().insert(entity);

            runOnUiThread(this::showSuccessDialog);
        });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.kyc_success_title)
                .setMessage(R.string.kyc_success_message)
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
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

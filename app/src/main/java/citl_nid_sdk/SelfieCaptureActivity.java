package citl_nid_sdk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import citl_nid_sdk.databinding.ActivitySelfieCaptureBinding;

@androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
public class SelfieCaptureActivity extends AppCompatActivity {

    private ActivitySelfieCaptureBinding binding;
    private ImageCapture imageCapture;
    private androidx.camera.core.ImageAnalysis imageAnalysis;
    private FaceDetector faceDetector;
    private ExecutorService cameraExecutor;
    private String capturedImagePath = null;

    // Liveness States
    private static final int STATE_BLINK = 0;
    private static final int STATE_SMILE = 1;
    private static final int STATE_READY = 2;
    private int currentLivenessState = STATE_BLINK;

    private int blinkCount = 0;
    private static final int REQUIRED_BLINKS = 4;
    private boolean isBlinkMidway = false;
    private boolean isSmileDetected = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.kyc_camera_permission_required, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelfieCaptureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();
        initFaceDetector();

        setupUI();
        checkCameraPermission();
    }

    private void initFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.35f)
                .build();
        faceDetector = FaceDetection.getClient(options);
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Capture button
        binding.btnCapture.setOnClickListener(v -> capturePhoto());

        // Retake button
        binding.btnRetake.setOnClickListener(v -> retakePhoto());

        // Save button
        binding.btnSave.setOnClickListener(v -> savePhoto());
        
        // Initial state
        updateLivenessUI();
        binding.btnCapture.setEnabled(false);
        binding.btnCapture.setAlpha(0.5f);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                // Image Capture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                // Image Analysis for Liveness
                imageAnalysis = new androidx.camera.core.ImageAnalysis.Builder()
                        .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    processImageForLiveness(image);
                });

                // Front camera for selfie
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);

            } catch (Exception e) {
                Toast.makeText(this, R.string.kyc_camera_error, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void processImageForLiveness(androidx.camera.core.ImageProxy imageProxy) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            faceDetector.process(image)
                    .addOnSuccessListener(faces -> {
                        if (!faces.isEmpty()) {
                            checkLivenessSteps(faces.get(0));
                        }
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private void checkLivenessSteps(Face face) {
        if (currentLivenessState == STATE_READY) return;

        float leftEyeOpen = face.getLeftEyeOpenProbability() != null ? face.getLeftEyeOpenProbability() : 1.0f;
        float rightEyeOpen = face.getRightEyeOpenProbability() != null ? face.getRightEyeOpenProbability() : 1.0f;
        float smileProb = face.getSmilingProbability() != null ? face.getSmilingProbability() : 0.0f;

        runOnUiThread(() -> {
            if (currentLivenessState == STATE_BLINK) {
                // Detecting a blink (both eyes mostly closed)
                if (leftEyeOpen < 0.2f && rightEyeOpen < 0.2f) {
                    isBlinkMidway = true;
                } else if (isBlinkMidway && leftEyeOpen > 0.6f && rightEyeOpen > 0.6f) {
                    // Blink completed
                    blinkCount++;
                    isBlinkMidway = false;
                    updateLivenessUI(); // Update progress bar
                    
                    if (blinkCount >= REQUIRED_BLINKS) {
                        currentLivenessState = STATE_SMILE;
                        updateLivenessUI();
                    }
                }
            } else if (currentLivenessState == STATE_SMILE) {
                if (smileProb > 0.7f) {
                    isSmileDetected = true;
                    currentLivenessState = STATE_READY;
                    updateLivenessUI();
                    enableCapture();
                }
            }
        });
    }

    private void updateLivenessUI() {
        switch (currentLivenessState) {
            case STATE_BLINK:
                binding.txtInstruction.setText("Blink your eyes");
                binding.livenessIcon.setImageResource(R.drawable.ic_selfie);
                binding.livenessProgress.setMax(REQUIRED_BLINKS);
                binding.livenessProgress.setProgress(blinkCount);
                break;
            case STATE_SMILE:
                binding.txtInstruction.setText("Smile for selfie");
                binding.livenessIcon.setImageResource(R.drawable.ic_selfie);
                binding.livenessProgress.setMax(1);
                binding.livenessProgress.setProgress(isSmileDetected ? 1 : 0);
                startPulseAnimation();
                //highlightCheck(binding.step1Check); // Blink step is 1
                break;
            case STATE_READY:
                binding.txtInstruction.setText("Capture Selfie");
                binding.livenessIcon.setImageResource(R.drawable.ic_camera);
                binding.livenessProgress.setMax(1);
                binding.livenessProgress.setProgress(1);
                binding.livenessIcon.setColorFilter(ContextCompat.getColor(this, R.color.kyc_success));
                startPulseAnimation();
                //highlightCheck(binding.step2Check); // Smile step is 2
                break;
        }
    }

    private void highlightCheck(ImageView check) {
        check.setAlpha(1.0f);
        check.setColorFilter(ContextCompat.getColor(this, R.color.kyc_primary));
    }

    private void startPulseAnimation() {
        android.view.animation.Animation pulse = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        pulse.setDuration(500);
        binding.livenessOverlay.startAnimation(pulse);
    }

    private void enableCapture() {
        binding.btnCapture.setEnabled(true);
        binding.btnCapture.setAlpha(1.0f);
        // Optional: Auto-capture after 1 second
        // new android.os.Handler().postDelayed(this::capturePhoto, 1000);
    }

    private void resetCheckmarks() {
        /*binding.step1Check.setAlpha(0.3f);
        binding.step1Check.setColorFilter(null);
        binding.step2Check.setAlpha(0.3f);
        binding.step2Check.setColorFilter(null);*/
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        binding.btnCapture.setEnabled(false);

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(androidx.camera.core.ImageProxy image) {
                        int rotationDegrees = image.getImageInfo().getRotationDegrees();
                        BitmapUtilsExt.processImageProxy(SelfieCaptureActivity.this, image,
                                bitmap -> {
                                    image.close();
                                    // Fix rotation and Mirror for front camera
                                    Bitmap corrected = fixSelfieBitmap(bitmap, rotationDegrees);
                                    saveSelfieToFile(corrected);
                                    runOnUiThread(() -> {
                                        binding.btnCapture.setEnabled(true);
                                        showPreview();
                                    });
                                },
                                e -> {
                                    image.close();
                                    runOnUiThread(() -> {
                                        binding.btnCapture.setEnabled(true);
                                        Toast.makeText(SelfieCaptureActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                });
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        binding.btnCapture.setEnabled(true);
                        Toast.makeText(SelfieCaptureActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap fixSelfieBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        // Mirror the front camera image so it looks like what the user sees
        matrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        
        Bitmap corrected = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (corrected != bitmap) {
            bitmap.recycle();
        }
        return corrected;
    }

    private void saveSelfieToFile(Bitmap bitmap) {
        try {
            File dir = new File(getExternalFilesDir(null), "selfies");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "selfie_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            capturedImagePath = file.getAbsolutePath();
        } catch (java.io.IOException e) {
            Log.e("SelfieCapture", "Failed to save selfie", e);
        }
    }

    private void showPreview() {
        // Show captured image
        binding.imgPreview.setImageBitmap(BitmapFactory.decodeFile(capturedImagePath));
        binding.imgPreview.setVisibility(View.VISIBLE);

        // Hide camera preview and instruction
        binding.previewView.setVisibility(View.GONE);
        binding.livenessOverlay.setVisibility(View.GONE);

        // Toggle buttons
        binding.btnCapture.setVisibility(View.GONE);
        binding.txtCaptureLabel.setVisibility(View.GONE);
        binding.layoutAfterCapture.setVisibility(View.VISIBLE);
        
        // Unbind liveness analyzer
        try {
            ProcessCameraProvider.getInstance(this).get().unbindAll();
        } catch (Exception ignored) {}
    }

    private void retakePhoto() {
        // Reset liveness
        currentLivenessState = STATE_BLINK;
        blinkCount = 0;
        isBlinkMidway = false;
        isSmileDetected = false;
        updateLivenessUI();
        binding.btnCapture.setEnabled(false);
        binding.btnCapture.setAlpha(0.5f);
        binding.livenessIcon.setColorFilter(null);
        resetCheckmarks();

        // Delete captured file
        if (capturedImagePath != null) {
            File file = new File(capturedImagePath);
            if (file.exists()) file.delete();
            capturedImagePath = null;
        }

        // Hide preview
        binding.imgPreview.setVisibility(View.GONE);

        // Show camera preview
        binding.previewView.setVisibility(View.VISIBLE);
        binding.livenessOverlay.setVisibility(View.VISIBLE);

        // Toggle buttons
        binding.btnCapture.setVisibility(View.VISIBLE);
        binding.txtCaptureLabel.setVisibility(View.VISIBLE);
        binding.layoutAfterCapture.setVisibility(View.GONE);

        // Restart camera
        startCamera();
    }

    private void savePhoto() {
        if (capturedImagePath != null) {
            Toast.makeText(this, R.string.kyc_selfie_saved, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (faceDetector != null) {
            faceDetector.close();
        }
        binding = null;
    }
}

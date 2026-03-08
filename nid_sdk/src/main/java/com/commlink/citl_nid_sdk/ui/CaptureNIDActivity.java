package com.commlink.citl_nid_sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.util.Size;
import android.widget.Toast;

import com.commlink.citl_nid_sdk.R;
import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.utils.BitmapHolder;
import com.commlink.citl_nid_sdk.utils.BitmapUtils;
import com.commlink.citl_nid_sdk.utils.BitmapUtilsExt;
import com.commlink.citl_nid_sdk.utils.CallbackHolder;
import com.commlink.citl_nid_sdk.utils.CameraPermissionHelper;
import com.yalantis.ucrop.UCrop;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureNIDActivity extends AppCompatActivity {

    public static final String EXTRA_CAPTURE_SIDE = "capture_side";
    public static final String EXTRA_IMAGE_PATH = "image_path";

    private PreviewView previewView;
    private OverlayView overlayView;
    private ImageButton captureButton;
    private TextView instruction;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isCameraReady = false;

    // Launcher for CropActivity — receives cropped image path
    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String croppedPath = result.getData().getStringExtra(CropActivity.EXTRA_CROPPED_IMAGE_PATH);
                    String side = result.getData().getStringExtra(CropActivity.EXTRA_CAPTURE_SIDE);

                    if (croppedPath != null) {
                        // Forward cropped result to the calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_IMAGE_PATH, croppedPath);
                        if (side != null)
                            resultIntent.putExtra(EXTRA_CAPTURE_SIDE, side);
                        setResult(Activity.RESULT_OK, resultIntent);
                    }
                }
                finish();
            });

    // Launcher for uCrop — receives cropped image path
    private final ActivityResultLauncher<Intent> uCropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    android.net.Uri croppedUri = UCrop.getOutput(result.getData());
                    String side = getIntent().getStringExtra(EXTRA_CAPTURE_SIDE);

                    if (croppedUri != null) {
                        String croppedPath = croppedUri.getPath();
                        // Forward cropped result to the calling activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_IMAGE_PATH, croppedPath);
                        if (side != null)
                            resultIntent.putExtra(EXTRA_CAPTURE_SIDE, side);
                        setResult(Activity.RESULT_OK, resultIntent);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(result.getData());
                    if (cropError != null) {
                        Log.e("CaptureNIDActivity", "uCrop failed", cropError);
                    }
                }
                finish();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_nid);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        captureButton = findViewById(R.id.captureButton);
        instruction = findViewById(R.id.instructionText);

        overlayView.setForFace(false);
        cameraExecutor = Executors.newSingleThreadExecutor();

        captureButton.setEnabled(false);

        if (CameraPermissionHelper.hasCameraPermission(this)) {
            startCamera();
        } else {
            CameraPermissionHelper.requestCameraPermission(this);
        }

        captureButton.setOnClickListener(v -> {
            if (isCameraReady) {
                captureManual();
            }
        });

        setupInstructions();

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

    private void setupInstructions() {
        String side = getIntent().getStringExtra(EXTRA_CAPTURE_SIDE);
        if ("front".equals(side)) {
            instruction.setText(R.string.nid_capture_front_instruction);
        } else if ("back".equals(side)) {
            instruction.setText(R.string.nid_capture_back_instruction);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CameraPermissionHelper.getRequestCode()) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                handleCameraError(new SecurityException("Camera permission denied"));
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                handleCameraError(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindUseCases(ProcessCameraProvider cameraProvider) {
        try {
            // Use 4:3 aspect ratio as it's common for photo capture
            ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                    .setAspectRatioStrategy(new AspectRatioStrategy(
                            AspectRatio.RATIO_4_3,
                            AspectRatioStrategy.FALLBACK_RULE_AUTO))
                    .setResolutionStrategy(new ResolutionStrategy(
                            new Size(1920, 1080), // Prefer 1080p if available
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                    .build();

            Preview preview = new Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build();

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setResolutionSelector(resolutionSelector)
                    .build();

            ImageAnalysis analysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(resolutionSelector)
                    .build();

            analysis.setAnalyzer(cameraExecutor, this::analyzeForAutoCapture);

            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    analysis);

            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            isCameraReady = true;
            runOnUiThread(() -> captureButton.setEnabled(true));
        } catch (Exception e) {
            handleCameraError(e);
        }
    }

    private void analyzeForAutoCapture(@NonNull ImageProxy image) {
        image.close();
    }

    private void captureManual() {
        if (imageCapture == null || !isCameraReady) {
            Log.w("CaptureNIDActivity", "Camera not ready yet");
            return;
        }

        captureButton.setEnabled(false);
        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        int rotationDegrees = image.getImageInfo().getRotationDegrees();
                        BitmapUtilsExt.processImageProxy(CaptureNIDActivity.this, image,
                                bitmap -> {
                                    image.close();
                                    processCaptureAndLaunchCrop(bitmap, rotationDegrees);
                                },
                                e -> {
                                    image.close();
                                    runOnUiThread(() -> {
                                        captureButton.setEnabled(true);
                                        handleCameraError(e);
                                    });
                                });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                        runOnUiThread(() -> {
                            captureButton.setEnabled(true);
                            handleCameraError(exception);
                        });
                    }
                });
    }

    /**
     * Handles bitmap rotation, center-cropping (zoom), and launches uCrop.
     */
    private void processCaptureAndLaunchCrop(Bitmap bitmap, int rotationDegrees) {
        try {
            // 1. Fix rotation
            Bitmap rotatedBitmap = rotateBitmap(bitmap, rotationDegrees);
            if (rotatedBitmap == null)
                return;

            // 2. Perform Automatic Center Crop (Simulated Zoom)
            // Using 0.8 zoom factor to ensure the card fills more of the frame
            Bitmap zoomedBitmap = centerCrop(rotatedBitmap, 0.65f);

            // 3. Save to temporary file for uCrop
            String savedPath = saveBitmapToFile(zoomedBitmap);

            // Clean up intermediate bitmaps safely
            if (zoomedBitmap != rotatedBitmap && !zoomedBitmap.isRecycled()) {
                // zoomedBitmap is what we saved, keep it for BitmapHolder if needed, or recycle
                // if only file is needed
                // rotatedBitmap can be recycled now
            }
            if (rotatedBitmap != bitmap && !rotatedBitmap.isRecycled()) {
                rotatedBitmap.recycle();
            }

            runOnUiThread(() -> {
                captureButton.setEnabled(true);
                if (savedPath != null) {
                    launchUCrop(savedPath);
                } else {
                    handleCameraError(new IOException("Failed to save captured image"));
                }
            });
        } catch (Exception e) {
            Log.e("CaptureNIDActivity", "Processing failed", e);
            runOnUiThread(() -> {
                captureButton.setEnabled(true);
                handleCameraError(e);
            });
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || bitmap == null)
            return bitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        try {
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return rotated;
        } catch (OutOfMemoryError e) {
            Log.e("CaptureNIDActivity", "OOM in rotation", e);
            return bitmap;
        }
    }

    /**
     * Crops the bitmap from the center to a 4:3 aspect ratio and applies a zoom
     * factor.
     */
    private Bitmap centerCrop(Bitmap src, float zoomFactor) {
        if (src == null)
            return null;

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        // NID/Document standard 4:3 aspect ratio
        float targetRatio = 4f / 3f;

        int newWidth, newHeight;

        if (srcWidth > srcHeight * targetRatio) {
            // Source is wider than 4:3 (Landscape)
            newHeight = (int) (srcHeight * zoomFactor);
            newWidth = (int) (newHeight * targetRatio);
        } else {
            // Source is taller than 4:3 (Portrait or square-ish)
            newWidth = (int) (srcWidth * zoomFactor);
            newHeight = (int) (newWidth / targetRatio);
        }

        // Constraints
        newWidth = Math.min(newWidth, srcWidth);
        newHeight = Math.min(newHeight, srcHeight);

        int x = (srcWidth - newWidth) / 2;
        int y = (srcHeight - newHeight) / 2;

        try {
            return Bitmap.createBitmap(src, x, y, newWidth, newHeight);
        } catch (Exception e) {
            Log.e("CaptureNIDActivity", "Center crop failed", e);
            return src;
        }
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled())
            return null;
        try {
            String side = getIntent().getStringExtra(EXTRA_CAPTURE_SIDE);
            String prefix = "front".equals(side) ? "nid_front_" : "nid_back_";
            File dir = new File(getExternalFilesDir(null), "nid_images");
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, prefix + System.currentTimeMillis() + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); // 100 for best quality
                fos.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("CaptureNIDActivity", "Failed to save bitmap", e);
            return null;
        }
    }

    private void launchUCrop(String imagePath) {
        String side = getIntent().getStringExtra(EXTRA_CAPTURE_SIDE);
        String prefix = "front".equals(side) ? "nid_front_cropped_" : "nid_back_cropped_";
        File dir = new File(getExternalFilesDir(null), "nid_images");
        if (!dir.exists())
            dir.mkdirs();
        File destinationFile = new File(dir, prefix + System.currentTimeMillis() + ".jpg");

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(95);
        options.setHideBottomControls(false);
        options.setShowCropGrid(false);
        options.setFreeStyleCropEnabled(false); // Force fixed aspect ratio
        options.setToolbarTitle("Crop Card");

        // Auto-fit image to crop bounds
        options.setImageToCropBoundsAnimDuration(500);

        UCrop uCrop = UCrop.of(android.net.Uri.fromFile(new File(imagePath)), android.net.Uri.fromFile(destinationFile))
                .withAspectRatio(4, 3) // Fixed 4:3 for NID
                .withMaxResultSize(1920, 1440) // High res for OCR
                .withOptions(options);

        uCropLauncher.launch(uCrop.getIntent(this));
    }

    private void goToSelfie(Bitmap nidBitmap) {
        BitmapHolder.setNidBitmap(nidBitmap);
        SelfieActivity.start(this);
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, CaptureNIDActivity.class));
    }

    public static Intent createIntent(Context context, String side) {
        Intent intent = new Intent(context, CaptureNIDActivity.class);
        intent.putExtra(EXTRA_CAPTURE_SIDE, side);
        return intent;
    }

    private void handleCameraError(Exception e) {
        Log.e("CaptureNIDActivity", "Camera error", e);
        NIDCallback cb = CallbackHolder.getInstance().getCallback();
        if (cb != null) {
            cb.onError(new NIDError(NIDError.Code.CAMERA_ERROR, "Camera error", e));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}

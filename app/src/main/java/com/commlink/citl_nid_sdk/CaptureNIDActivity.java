package com.commlink.citl_nid_sdk;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import citl_nid_sdk.R;

public class CaptureNIDActivity extends AppCompatActivity {

    private PreviewView previewView;
    private OverlayView overlayView;
    private ImageButton captureButton;
    private TextView instruction;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isCameraReady = false;

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

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
            Preview preview = new Preview.Builder().build();
            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            ImageAnalysis analysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

            analysis.setAnalyzer(cameraExecutor, this::analyzeForAutoCapture);

            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    analysis
            );

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
                        BitmapUtilsExt.processImageProxy(CaptureNIDActivity.this, image,
                                bitmap -> {
                                    image.close();
                                    runOnUiThread(() -> {
                                        captureButton.setEnabled(true);
                                        goToSelfie(bitmap);
                                    });
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
                }
        );
    }

    private void goToSelfie(android.graphics.Bitmap nidBitmap) {
        BitmapHolder.setNidBitmap(nidBitmap);
        SelfieActivity.start(this);
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


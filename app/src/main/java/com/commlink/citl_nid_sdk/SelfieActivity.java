package com.commlink.citl_nid_sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import citl_nid_sdk.R;

public class SelfieActivity extends AppCompatActivity implements LivenessDetector.ActionProgressCallback {

    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView instruction;
    private ImageButton captureButton;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private LivenessDetector livenessDetector;
    private boolean livenessPassed = false;
    private boolean isCameraReady = false;

    private ProgressBar pbBlink, pbSmile, pbTurn;
    private ImageView imgBlinkCheck, imgSmileCheck, imgTurnCheck;
    private LivenessDetector.ActionType currentAction;

    public static void start(Context context) {
        context.startActivity(new Intent(context, SelfieActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        instruction = findViewById(R.id.instructionText);
        captureButton = findViewById(R.id.captureButton);

        pbBlink = findViewById(R.id.pbBlink);
        pbSmile = findViewById(R.id.pbSmile);
        pbTurn = findViewById(R.id.pbTurn);

        imgBlinkCheck = findViewById(R.id.imgBlinkCheck);
        imgSmileCheck = findViewById(R.id.imgSmileCheck);
        imgTurnCheck = findViewById(R.id.imgTurnCheck);

        overlayView.setForFace(true);
        cameraExecutor = Executors.newSingleThreadExecutor();
        livenessDetector = new LivenessDetector();
        livenessDetector.setProgressCallback(this);

        captureButton.setEnabled(false);

        if (CameraPermissionHelper.hasCameraPermission(this)) {
            startCamera();
        } else {
            CameraPermissionHelper.requestCameraPermission(this);
        }

        captureButton.setOnClickListener(v -> {
            if (isCameraReady && livenessPassed) {
                captureSelfie();
            } else if (!isCameraReady) {
                instruction.setText("Camera not ready");
            } else {
                instruction.setText(R.string.nid_selfie_liveness_not_ready);
            }
        });

        // Set instruction for first random step
        instruction.setText(livenessDetector.getCurrentInstruction());
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

            analysis.setAnalyzer(cameraExecutor, this::analyzeLiveness);

            CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

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
            runOnUiThread(() -> captureButton.setEnabled(false));
        } catch (Exception e) {
            handleCameraError(e);
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeLiveness(@NonNull ImageProxy imageProxy) {
        try {
            InputImage img = InputImage.fromMediaImage(
                    imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            livenessDetector.analyzeFrame(img, result -> {
                if (!result.faces.isEmpty()) {
                    livenessDetector.processFace(result.faces);
                }
                imageProxy.close();
            });
        } catch (Exception e) {
            imageProxy.close();
            runOnUiThread(() -> handleLivenessError(e));
        }
    }

    @Override
    public void onActionProgress(LivenessDetector.ActionType action, float progress, boolean completed) {
        runOnUiThread(() -> {
            // Show current step progress on oval (round progress)
            overlayView.setOvalProgress(progress);
            updateProgressBar(action, progress);
            // Keep instruction in sync with current action
            instruction.setText(livenessDetector.getCurrentInstruction());
            if (completed) {
                updateCheckmark(action, true);
            }
        });
    }

    @Override
    public void onActionCompleted(LivenessDetector.ActionType action) {
        runOnUiThread(() -> {
            updateCheckmark(action, true);
            // Next random step is now active: get instruction from detector
            String nextInstruction = livenessDetector.getCurrentInstruction();
            instruction.setText(nextInstruction);
            // Reset oval progress for next step (it will fill again for next action)
            overlayView.setOvalProgress(0f);
            currentAction = livenessDetector.getCurrentAction();
            if (currentAction != null) {
                resetProgressBarForNextAction(currentAction);
            } else {
                instruction.setText(R.string.nid_liveness_completed);
            }
        });
    }
    
    /**
     * Reset progress bar visual for next action (optional - for smooth transition)
     */
    private void resetProgressBarForNextAction(LivenessDetector.ActionType nextAction) {
        // This is optional - you can animate progress bar reset if desired
        // The actual progress value is managed by LivenessDetector
        switch (nextAction) {
            case BLINK:
                pbBlink.setProgress(0);
                break;
            case SMILE:
                pbSmile.setProgress(0);
                break;
            case TURN_HEAD_LEFT:
            case TURN_HEAD_RIGHT:
                pbTurn.setProgress(0);
                break;
        }
    }

    @Override
    public void onAllActionsCompleted() {
        runOnUiThread(() -> {
            livenessPassed = true;
            instruction.setText(R.string.nid_selfie_liveness_passed);
            captureButton.setEnabled(true);
            captureSelfie();
        });
    }

    private void updateProgressBar(LivenessDetector.ActionType action, float progress) {
        int progressPercent = (int) (progress * 100);
        switch (action) {
            case BLINK:
                pbBlink.setProgress(progressPercent);
                break;
            case SMILE:
                pbSmile.setProgress(progressPercent);
                break;
            case TURN_HEAD_LEFT:
            case TURN_HEAD_RIGHT:
                pbTurn.setProgress(progressPercent);
                break;
        }
    }

    private void updateCheckmark(LivenessDetector.ActionType action, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        switch (action) {
            case BLINK:
                imgBlinkCheck.setVisibility(visibility);
                break;
            case SMILE:
                imgSmileCheck.setVisibility(visibility);
                break;
            case TURN_HEAD_LEFT:
            case TURN_HEAD_RIGHT:
                imgTurnCheck.setVisibility(visibility);
                break;
        }
    }

    private void updateInstructionForCurrentAction() {
        currentAction = livenessDetector.getCurrentAction();
        if (currentAction == null) {
            instruction.setText(R.string.nid_liveness_completed);
            return;
        }

        switch (currentAction) {
            case BLINK:
                instruction.setText(R.string.nid_liveness_action_blink);
                break;
            case SMILE:
                instruction.setText(R.string.nid_liveness_action_smile);
                break;
            case TURN_HEAD_LEFT:
                instruction.setText(R.string.nid_liveness_action_turn_left);
                break;
            case TURN_HEAD_RIGHT:
                instruction.setText(R.string.nid_liveness_action_turn_right);
                break;
        }
    }

    private void captureSelfie() {
        if (imageCapture == null || !isCameraReady) {
            Log.w("SelfieActivity", "Camera not ready yet");
            return;
        }

        captureButton.setEnabled(false);
        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        BitmapUtilsExt.processImageProxy(SelfieActivity.this, image,
                                bitmap -> {
                                    image.close();
                                    BitmapHolder.setSelfieBitmap(bitmap);
                                    ProcessingActivity.start(SelfieActivity.this);
                                    finish();
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

    private void handleLivenessError(Exception e) {
        Log.e("SelfieActivity", "Liveness error", e);
        NIDCallback cb = CallbackHolder.getInstance().getCallback();
        if (cb != null) {
            cb.onError(new NIDError(NIDError.Code.LIVENESS_FAILED,
                    "Liveness error", e));
        }
        finish();
    }

    private void handleCameraError(Exception e) {
        Log.e("SelfieActivity", "Camera error", e);
        NIDCallback cb = CallbackHolder.getInstance().getCallback();
        if (cb != null) {
            cb.onError(new NIDError(NIDError.Code.CAMERA_ERROR,
                    "Camera error", e));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}

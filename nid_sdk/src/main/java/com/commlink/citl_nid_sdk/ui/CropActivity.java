package com.commlink.citl_nid_sdk.ui;
import com.commlink.citl_nid_sdk.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Activity that displays a captured image with a crop rectangle overlay.
 * After user adjusts the crop and taps "Done", the cropped image is saved
 * and the path is returned via setResult.
 */
public class CropActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final String EXTRA_CAPTURE_SIDE = "capture_side";
    public static final String EXTRA_CROPPED_IMAGE_PATH = "cropped_image_path";

    private CropImageView cropImageView;
    private String originalImagePath;
    private String captureSide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        cropImageView = findViewById(R.id.cropImageView);

        originalImagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        captureSide = getIntent().getStringExtra(EXTRA_CAPTURE_SIDE);

        if (originalImagePath == null) {
            Toast.makeText(this, "No image to crop", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load the image
        Bitmap bitmap = BitmapFactory.decodeFile(originalImagePath);
        if (bitmap == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        cropImageView.setImageBitmap(bitmap);

        // Cancel button
        findViewById(R.id.btnCropCancel).setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        // Done button — crop and return
        findViewById(R.id.btnCropDone).setOnClickListener(v -> cropAndReturn());
    }

    private void cropAndReturn() {
        Bitmap cropped = cropImageView.getCroppedBitmap();
        if (cropped == null) {
            Toast.makeText(this, "Crop failed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save cropped bitmap to file
        String croppedPath = saveCroppedBitmap(cropped);
        if (croppedPath == null) {
            Toast.makeText(this, "Failed to save cropped image", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_CROPPED_IMAGE_PATH, croppedPath);
        if (captureSide != null) {
            resultIntent.putExtra(EXTRA_CAPTURE_SIDE, captureSide);
        }
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private String saveCroppedBitmap(Bitmap bitmap) {
        try {
            String prefix = "front".equals(captureSide) ? "nid_front_cropped_" : "nid_back_cropped_";
            File dir = new File(getExternalFilesDir(null), "nid_images");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, prefix + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("CropActivity", "Failed to save cropped bitmap", e);
            return null;
        }
    }

    public static Intent createIntent(Context context, String imagePath, String side) {
        Intent intent = new Intent(context, CropActivity.class);
        intent.putExtra(EXTRA_IMAGE_PATH, imagePath);
        intent.putExtra(EXTRA_CAPTURE_SIDE, side);
        return intent;
    }
}

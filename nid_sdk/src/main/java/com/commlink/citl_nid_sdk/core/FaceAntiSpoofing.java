package com.commlink.citl_nid_sdk.core;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Helper class for Silent Face Anti-Spoofing using TensorFlow Lite.
 * Based on the Silent-Face-Anti-Spoofing model by minivision-ai.
 */
public class FaceAntiSpoofing {
    private static final String TAG = "FaceAntiSpoofing";
    
    // Default model name in assets
    private static final String MODEL_NAME = "FaceAntiSpoofing.tflite";
    
    // Model input size (Silent-Face models are often 80x80)
    private static final int INPUT_SIZE = 80;
    
    private Interpreter interpreter;
    private final Context context;

    public FaceAntiSpoofing(Context context) {
        this.context = context;
        try {
            interpreter = new Interpreter(loadModelFile());
            Log.d(TAG, "TFLite model loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading TFLite model: " + e.getMessage());
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Check if the face in the bitmap is real or fake.
     * @param bitmap Cropped face bitmap
     * @return Score between 0 and 1 (Higher means more likely real)
     */
    public float analyzeLiveness(Bitmap bitmap) {
        if (interpreter == null) return 1.0f; // Default to pass if model not loaded

        // 1. Preprocess image
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0, 255.0f)) // Normalize to [0, 1]
                .build();

        TensorImage tensorImage = new TensorImage(interpreter.getInputTensor(0).dataType());
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        // 2. Prepare output buffer
        // Silent-Face models usually output [1, 3] float array (Real, Fake1, Fake2) or similar
        float[][] output = new float[1][3];

        // 3. Run inference
        interpreter.run(tensorImage.getBuffer(), output);

        // 4. Interpretation (MiniFASNet specific)
        // Usually, index 1 is 'Real' and others are spoof categories
        // Depending on the conversion, this might need tuning
        float realScore = output[0][1]; 
        
        Log.d(TAG, "Inference scores: Real=" + output[0][1] + ", Fake1=" + output[0][0] + ", Fake2=" + output[0][2]);
        
        return realScore;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}

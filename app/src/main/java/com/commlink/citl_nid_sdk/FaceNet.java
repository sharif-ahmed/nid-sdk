package com.commlink.citl_nid_sdk;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FaceNet {

    private static final String MODEL_FILE = "MobileFaceNet.tflite";
    private static final int INPUT_SIZE = 112;
    private static final int EMBEDDING_SIZE = 192;

    private Interpreter interpreter;

    public FaceNet(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(loadModelFile(context), options);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        FileInputStream fis = new FileInputStream(context.getAssets().openFd(MODEL_FILE).getFileDescriptor());
        FileChannel channel = fis.getChannel();
        long startOffset = context.getAssets().openFd(MODEL_FILE).getStartOffset();
        long declaredLength = context.getAssets().openFd(MODEL_FILE).getDeclaredLength();
        return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] getEmbedding(Bitmap bitmap) {
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        Bitmap rgb = ensureConfig(scaled);

        float[][][][] input = new float[1][INPUT_SIZE][INPUT_SIZE][3];
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int px = rgb.getPixel(x, y);
                float r = ((px >> 16) & 0xFF) / 255f;
                float g = ((px >> 8) & 0xFF) / 255f;
                float b = (px & 0xFF) / 255f;
                input[0][y][x][0] = r;
                input[0][y][x][1] = g;
                input[0][y][x][2] = b;
            }
        }

        float[][] embedding = new float[1][EMBEDDING_SIZE];
        interpreter.run(input, embedding);
        return embedding[0];
    }

    private Bitmap ensureConfig(Bitmap src) {
        if (src.getConfig() != Bitmap.Config.ARGB_8888) {
            return src.copy(Bitmap.Config.ARGB_8888, false);
        }
        return src;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}


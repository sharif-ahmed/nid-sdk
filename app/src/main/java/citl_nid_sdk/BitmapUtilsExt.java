package citl_nid_sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BitmapUtilsExt {

    public interface OnBitmapReady {
        void onBitmap(Bitmap bitmap);
    }

    public interface OnError {
        void onError(Exception e);
    }

    public static void processImageProxy(Context context, @NonNull ImageProxy imageProxy,
                                        OnBitmapReady callback, OnError onError) {

        try {
            int format = imageProxy.getFormat();
            Bitmap bitmap;

            if (format == ImageFormat.JPEG) {
                // ImageCapture returns JPEG format - single plane
                ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
                if (planes.length > 0) {
                    ByteBuffer buffer = planes[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } else {
                    throw new IllegalArgumentException("No image planes available");
                }
            } else if (format == ImageFormat.YUV_420_888) {
                // ImageAnalysis returns YUV format - three planes
                ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
                if (planes.length < 3) {
                    throw new IllegalArgumentException("YUV image must have 3 planes");
                }
                
                ByteBuffer yPlane = planes[0].getBuffer();
                ByteBuffer uPlane = planes[1].getBuffer();
                ByteBuffer vPlane = planes[2].getBuffer();

                int ySize = yPlane.remaining();
                int uSize = uPlane.remaining();
                int vSize = vPlane.remaining();

                byte[] nv21 = new byte[ySize + uSize + vSize];

                yPlane.get(nv21, 0, ySize);
                vPlane.get(nv21, ySize, vSize);
                uPlane.get(nv21, ySize + vSize, uSize);

                YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,
                        imageProxy.getWidth(), imageProxy.getHeight(), null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0,
                        imageProxy.getWidth(), imageProxy.getHeight()), 90, out);
                byte[] jpegBytes = out.toByteArray();

                bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        jpegBytes, 0, jpegBytes.length);
            } else {
                // Fallback: try to get Image and convert
                Image image = imageProxy.getImage();
                if (image != null) {
                    android.media.Image.Plane[] imagePlanes = image.getPlanes();
                    if (imagePlanes.length > 0) {
                        ByteBuffer buffer = imagePlanes[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    } else {
                        throw new IllegalArgumentException("Unsupported image format: " + format);
                    }
                } else {
                    throw new IllegalArgumentException("Image is null");
                }
            }

            if (bitmap != null) {
                callback.onBitmap(bitmap);
            } else {
                onError.onError(new IllegalArgumentException("Failed to decode bitmap"));
            }
        } catch (Exception e) {
            onError.onError(e);
        }
    }
}


package citl_nid_sdk;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {

    /**
     * Converts bitmap to Base64 with compression and scaling to prevent OOM and large payloads.
     */
    public static String toBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        
        // 1. Scale down if too large (Max 1024px)
        Bitmap scaledBitmap = scaleBitmap(bitmap, 1024);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 2. Compress to JPEG with 80% quality
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] bytes = baos.toByteArray();
        
        // 3. Cleanup scaled bitmap if new one was created
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle();
        }
        
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }

        float aspectRatio = (float) width / (float) height;
        int newWidth, newHeight;

        if (width > height) {
            newWidth = maxDimension;
            newHeight = Math.round(maxDimension / aspectRatio);
        } else {
            newHeight = maxDimension;
            newWidth = Math.round(maxDimension * aspectRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}

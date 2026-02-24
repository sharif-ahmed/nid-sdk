package citl_nid_sdk;

import android.graphics.Bitmap;

class BitmapHolder {
    private static Bitmap nidBitmap;
    private static Bitmap selfieBitmap;

    public static void setNidBitmap(Bitmap b) {
        nidBitmap = b;
    }

    public static Bitmap getNidBitmap() {
        return nidBitmap;
    }

    public static void setSelfieBitmap(Bitmap b) {
        selfieBitmap = b;
    }

    public static Bitmap getSelfieBitmap() {
        return selfieBitmap;
    }

    public static void clear() {
        nidBitmap = null;
        selfieBitmap = null;
    }
}


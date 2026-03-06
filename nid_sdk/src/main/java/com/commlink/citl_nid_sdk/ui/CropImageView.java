package com.commlink.citl_nid_sdk.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Custom ImageView that draws a draggable/resizable crop rectangle over the image.
 * Supports drag-to-move and corner-drag-to-resize.
 */
public class CropImageView extends AppCompatImageView {

    private final Paint borderPaint = new Paint();
    private final Paint overlayPaint = new Paint();
    private final Paint cornerPaint = new Paint();
    private final Paint gridPaint = new Paint();

    private RectF cropRect = new RectF();
    private RectF imageBounds = new RectF();
    private boolean isInitialized = false;

    // Fixed aspect ratio for NID (85.6mm / 54.0mm ≈ 1.585)
    private static final float NID_ASPECT_RATIO = 85.6f / 54.0f;
    private boolean useFixedAspectRatio = true;

    // Touch handling
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_MOVE = 1;
    private static final int TOUCH_CORNER_TL = 2;
    private static final int TOUCH_CORNER_TR = 3;
    private static final int TOUCH_CORNER_BL = 4;
    private static final int TOUCH_CORNER_BR = 5;

    private int touchMode = TOUCH_NONE;
    private float lastTouchX, lastTouchY;
    private static final float CORNER_TOUCH_RADIUS = 70f;
    private static final float MIN_CROP_SIZE = 120f;

    public CropImageView(Context context) {
        super(context);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_CENTER);
        // Important for smooth custom drawing
        setLayerType(LAYER_TYPE_HARDWARE, null);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setAntiAlias(true);

        overlayPaint.setColor(Color.argb(180, 0, 0, 0));
        overlayPaint.setStyle(Paint.Style.FILL);

        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(8f);
        cornerPaint.setAntiAlias(true);
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);

        gridPaint.setColor(Color.argb(80, 255, 255, 255));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Recalculate image bounds whenever layout changes
        updateImageBounds();
        if (!isInitialized && imageBounds.width() > 0) {
            initCropRect();
        }
    }

    private void updateImageBounds() {
        if (getDrawable() == null) return;
        Matrix matrix = getImageMatrix();
        imageBounds.set(0, 0,
                getDrawable().getIntrinsicWidth(),
                getDrawable().getIntrinsicHeight());
        matrix.mapRect(imageBounds);
    }

    private void initCropRect() {
        if (imageBounds.width() <= 0) return;

        // Start with a large crop box matching NID aspect ratio
        float width = imageBounds.width() * 0.9f;
        float height = width / NID_ASPECT_RATIO;

        if (height > imageBounds.height() * 0.9f) {
            height = imageBounds.height() * 0.9f;
            width = height * NID_ASPECT_RATIO;
        }

        float left = imageBounds.centerX() - (width / 2);
        float top = imageBounds.centerY() - (height / 2);
        
        cropRect.set(left, top, left + width, top + height);
        isInitialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInitialized || imageBounds.width() <= 0) {
            updateImageBounds();
            initCropRect();
        }
        if (!isInitialized) return;

        int w = getWidth();
        int h = getHeight();

        // Draw semi-transparent overlay
        canvas.drawRect(0, 0, w, cropRect.top, overlayPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, overlayPaint);
        canvas.drawRect(cropRect.right, cropRect.top, w, cropRect.bottom, overlayPaint);
        canvas.drawRect(0, cropRect.bottom, w, h, overlayPaint);

        // Draw crop border
        canvas.drawRect(cropRect, borderPaint);

        // Draw grid lines
        float thirdW = cropRect.width() / 3f;
        float thirdH = cropRect.height() / 3f;
        canvas.drawLine(cropRect.left + thirdW, cropRect.top, cropRect.left + thirdW, cropRect.bottom, gridPaint);
        canvas.drawLine(cropRect.left + 2 * thirdW, cropRect.top, cropRect.left + 2 * thirdW, cropRect.bottom, gridPaint);
        canvas.drawLine(cropRect.left, cropRect.top + thirdH, cropRect.right, cropRect.top + thirdH, gridPaint);
        canvas.drawLine(cropRect.left, cropRect.top + 2 * thirdH, cropRect.right, cropRect.top + 2 * thirdH, gridPaint);

        // Draw corner handles
        float cornerLen = 40f;
        // TL
        canvas.drawLine(cropRect.left - 2, cropRect.top, cropRect.left + cornerLen, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.top - 2, cropRect.left, cropRect.top + cornerLen, cornerPaint);
        // TR
        canvas.drawLine(cropRect.right + 2, cropRect.top, cropRect.right - cornerLen, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.top - 2, cropRect.right, cropRect.top + cornerLen, cornerPaint);
        // BL
        canvas.drawLine(cropRect.left - 2, cropRect.bottom, cropRect.left + cornerLen, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.bottom + 2, cropRect.left, cropRect.bottom - cornerLen, cornerPaint);
        // BR
        canvas.drawLine(cropRect.right + 2, cropRect.bottom, cropRect.right - cornerLen, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.bottom + 2, cropRect.right, cropRect.bottom - cornerLen, cornerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchMode = detectTouchTarget(x, y);
                if (touchMode != TOUCH_NONE) {
                    lastTouchX = x;
                    lastTouchY = y;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_NONE) return false;

                float dx = x - lastTouchX;
                float dy = y - lastTouchY;

                if (touchMode == TOUCH_MOVE) {
                    moveCropRect(dx, dy);
                } else {
                    resizeCropRect(touchMode, dx, dy);
                }

                lastTouchX = x;
                lastTouchY = y;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchMode = TOUCH_NONE;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private int detectTouchTarget(float x, float y) {
        if (isNear(x, y, cropRect.left, cropRect.top)) return TOUCH_CORNER_TL;
        if (isNear(x, y, cropRect.right, cropRect.top)) return TOUCH_CORNER_TR;
        if (isNear(x, y, cropRect.left, cropRect.bottom)) return TOUCH_CORNER_BL;
        if (isNear(x, y, cropRect.right, cropRect.bottom)) return TOUCH_CORNER_BR;
        if (cropRect.contains(x, y)) return TOUCH_MOVE;
        return TOUCH_NONE;
    }

    private boolean isNear(float x, float y, float tx, float ty) {
        return Math.sqrt(Math.pow(x - tx, 2) + Math.pow(y - ty, 2)) < CORNER_TOUCH_RADIUS;
    }

    private void moveCropRect(float dx, float dy) {
        float newLeft = cropRect.left + dx;
        float newTop = cropRect.top + dy;
        float newRight = cropRect.right + dx;
        float newBottom = cropRect.bottom + dy;

        if (newLeft < imageBounds.left) { dx = imageBounds.left - cropRect.left; }
        if (newTop < imageBounds.top) { dy = imageBounds.top - cropRect.top; }
        if (newRight > imageBounds.right) { dx = imageBounds.right - cropRect.right; }
        if (newBottom > imageBounds.bottom) { dy = imageBounds.bottom - cropRect.bottom; }

        cropRect.offset(dx, dy);
    }

    private void resizeCropRect(int mode, float dx, float dy) {
        float left = cropRect.left;
        float top = cropRect.top;
        float right = cropRect.right;
        float bottom = cropRect.bottom;

        switch (mode) {
            case TOUCH_CORNER_TL:
                left += dx;
                top += dy;
                break;
            case TOUCH_CORNER_TR:
                right += dx;
                top += dy;
                break;
            case TOUCH_CORNER_BL:
                left += dx;
                bottom += dy;
                break;
            case TOUCH_CORNER_BR:
                right += dx;
                bottom += dy;
                break;
        }

        // Apply aspect ratio constraints if needed
        if (useFixedAspectRatio) {
            float width = right - left;
            float height = width / NID_ASPECT_RATIO;
            
            // Adjust height based on which edge we are pulling
            if (mode == TOUCH_CORNER_TL || mode == TOUCH_CORNER_TR) {
                top = bottom - height;
            } else {
                bottom = top + height;
            }
        }

        // Min size and image bound checks
        if (right - left < MIN_CROP_SIZE) return;
        if (bottom - top < MIN_CROP_SIZE) return;
        if (left < imageBounds.left || right > imageBounds.right || 
            top < imageBounds.top || bottom > imageBounds.bottom) return;

        cropRect.set(left, top, right, bottom);
    }

    public Bitmap getCroppedBitmap() {
        if (getDrawable() == null) return null;
        Bitmap original = ((android.graphics.drawable.BitmapDrawable) getDrawable()).getBitmap();
        if (original == null) return null;

        float scaleX = (float) original.getWidth() / imageBounds.width();
        float scaleY = (float) original.getHeight() / imageBounds.height();

        int x = (int) ((cropRect.left - imageBounds.left) * scaleX);
        int y = (int) ((cropRect.top - imageBounds.top) * scaleY);
        int w = (int) (cropRect.width() * scaleX);
        int h = (int) (cropRect.height() * scaleY);

        // Basic bounds safety
        x = Math.max(0, x);
        y = Math.max(0, y);
        w = Math.min(w, original.getWidth() - x);
        h = Math.min(h, original.getHeight() - y);

        if (w <= 0 || h <= 0) return original;
        return Bitmap.createBitmap(original, x, y, w, h);
    }
}

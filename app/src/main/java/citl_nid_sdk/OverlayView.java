package citl_nid_sdk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {

    private Paint strokePaint;
    private Paint shadePaint;
    private Paint progressArcPaint;
    private boolean forFace = false;
    /** Progress for current step (0..1), drawn as round arc on oval */
    private float ovalProgress = 0f;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(9f);

        shadePaint = new Paint();
        shadePaint.setColor(Color.parseColor("#88000000"));
        shadePaint.setStyle(Paint.Style.FILL);

        progressArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressArcPaint.setColor(Color.parseColor("#1E88E5"));
        progressArcPaint.setStyle(Paint.Style.STROKE);
        progressArcPaint.setStrokeWidth(14f);
        progressArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setForFace(boolean forFace) {
        this.forFace = forFace;
        invalidate();
    }

    /**
     * Set progress for the round progress drawn on the oval (0..1).
     * Step progress is shown as an arc around the oval shape.
     */
    public void setOvalProgress(float progress) {
        if (progress < 0f) progress = 0f;
        if (progress > 1f) progress = 1f;
        if (ovalProgress != progress) {
            ovalProgress = progress;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        RectF focusRect;
        if (forFace) {
            float ovalWidth = width * 0.7f;
            float ovalHeight = height * 0.5f;
            float left = (width - ovalWidth) / 2;
            float top = (height - ovalHeight) / 3;
            focusRect = new RectF(left, top, left + ovalWidth, top + ovalHeight);
            canvas.drawARGB(0, 0, 0, 0);

            canvas.saveLayer(0, 0, width, height, null);
            canvas.drawRect(0, 0, width, height, shadePaint);

            Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawOval(focusRect, clearPaint);
            canvas.restore();

            canvas.drawOval(focusRect, strokePaint);

            // Round progress arc on oval (step progress)
            if (ovalProgress > 0.001f) {
                float sweepAngle = ovalProgress * 360f;
                canvas.drawArc(focusRect, 270f, sweepAngle, false, progressArcPaint);
            }

        } else {
            float cardWidth = width * 0.85f;
            float cardHeight = cardWidth * 0.63f;
            float left = (width - cardWidth) / 2;
            float top = (height - cardHeight) / 2;
            focusRect = new RectF(left, top, left + cardWidth, top + cardHeight);

            canvas.saveLayer(0, 0, width, height, null);
            canvas.drawRect(0, 0, width, height, shadePaint);

            Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRoundRect(focusRect, 30f, 30f, clearPaint);
            canvas.restore();

            canvas.drawRoundRect(focusRect, 30f, 30f, strokePaint);
        }
    }
}


package com.example.skillswap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDonutChartView extends View {

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<DonutSegment> segments = new ArrayList<>();

    public AdminDonutChartView(Context context) {
        super(context);
        init();
    }

    public AdminDonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdminDonutChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        backgroundPaint.setColor(Color.parseColor("#E0E0E0"));

        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.BUTT);

        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setColor(Color.parseColor("#212121"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        float textSizeSp = 12f;
        float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                textSizeSp, getResources().getDisplayMetrics());
        labelPaint.setTextSize(textSizePx);
    }

    public void setSegments(List<DonutSegment> newSegments) {
        segments.clear();
        if (newSegments != null) {
            segments.addAll(newSegments);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);

        if (size <= 0) return;

        float strokeWidth = size * 0.16f; // 16% of size
        backgroundPaint.setStrokeWidth(strokeWidth);
        segmentPaint.setStrokeWidth(strokeWidth);

        float radius = (size - strokeWidth) / 2f;
        float cx = width / 2f;
        float cy = height / 2f;

        // Background ring
        canvas.drawCircle(cx, cy, radius, backgroundPaint);

        if (segments.isEmpty()) {
            // No data: optionally could draw a text like "No data"
            return;
        }

        float startAngle = -90f; // start from top
        for (DonutSegment seg : segments) {
            if (seg.percentage <= 0f) continue;
            float sweepAngle = 360f * seg.percentage;
            segmentPaint.setColor(seg.color);
            canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius,
                    startAngle, sweepAngle, false, segmentPaint);

            // Draw label only for reasonably sized segments (>= 5%)
            if (seg.percentage >= 0.05f) {
                float midAngle = startAngle + sweepAngle / 2f;
                double rad = Math.toRadians(midAngle);
                float labelRadius = radius - strokeWidth * 0.35f;
                float lx = cx + (float) Math.cos(rad) * labelRadius;
                float ly = cy + (float) Math.sin(rad) * labelRadius;

                String percentText = String.format(Locale.getDefault(), "%.0f%%", seg.percentage * 100f);
                String text = seg.label + " " + percentText;
                // y is baseline; adjust a bit upward
                canvas.drawText(text, lx, ly, labelPaint);
            }

            startAngle += sweepAngle;
        }
    }

    public static class DonutSegment {
        public final String label;
        public final float percentage; // between 0 and 1
        public final int color;

        public DonutSegment(String label, float percentage, int color) {
            this.label = label;
            this.percentage = percentage;
            this.color = color;
        }
    }
}

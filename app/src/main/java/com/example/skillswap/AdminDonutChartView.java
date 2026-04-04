package com.example.skillswap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class AdminDonutChartView extends View {

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
        backgroundPaint.setColor(Color.parseColor("#F5F5F5"));

        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.BUTT);
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

        // Thicker donut ring as requested
        float strokeWidth = size * 0.25f; 
        backgroundPaint.setStrokeWidth(strokeWidth);
        segmentPaint.setStrokeWidth(strokeWidth);

        float radius = (size - strokeWidth) / 2f;
        float cx = width / 2f;
        float cy = height / 2f;

        // Background ring
        canvas.drawCircle(cx, cy, radius, backgroundPaint);

        if (segments.isEmpty()) {
            return;
        }

        float startAngle = -90f; // start from top
        for (DonutSegment seg : segments) {
            if (seg.percentage <= 0f) continue;
            float sweepAngle = 360f * seg.percentage;
            segmentPaint.setColor(seg.color);
            canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius,
                    startAngle, sweepAngle, false, segmentPaint);

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

package com.pedometer.step.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.pedometer.step.R;

public class CircularProgressBar extends View {
    private Paint progressPaint;
    private Paint backgroundPaint;
    private RectF circleRect;
    private float progress = 0;
    private float maxProgress = 100;

    public CircularProgressBar(Context context) {
        super(context);
        init(null);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dpToPx(12));
        progressPaint.setColor(getResources().getColor(R.color.blue_500));
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(dpToPx(12));
        backgroundPaint.setColor(getResources().getColor(R.color.blue_100));
        backgroundPaint.setAntiAlias(true);

        circleRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float padding = dpToPx(12);
        circleRect.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background circle
        canvas.drawArc(circleRect, 0, 360, false, backgroundPaint);

        // Draw progress
        float sweepAngle = (progress / maxProgress) * 360;
        canvas.drawArc(circleRect, -90, sweepAngle, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
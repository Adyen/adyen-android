package com.adyen.ui.views.loadinganimation;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public abstract class Animation extends Drawable implements Animatable {

    private HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener> animators = new HashMap<>();

    private int alpha = 255;
    private static final Rect ZERO_BOUNDS_RECT = new Rect();
    private Rect drawBounds = ZERO_BOUNDS_RECT;

    private Paint paint = new Paint();

    public Animation() {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        //do nothing, overwrite abstract method from super class
    }

    @Override
    public void draw(Canvas canvas) {
        draw(canvas, paint);
    }

    public abstract void draw(Canvas canvas, Paint paint);

    public abstract HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener> onCreateAnimators();

    @Override
    public void start() {
        if (animatorsIsNullOrEmpty()) {
            animators = onCreateAnimators();
        }

        if (isStarted()) {
            return;
        }
        startAnimators();
        invalidateSelf();
    }

    private void startAnimators() {
        if (animators != null) {
            final Set<Map.Entry<ValueAnimator, ValueAnimator.AnimatorUpdateListener>> entries = animators.entrySet();
            for (Map.Entry<ValueAnimator, ValueAnimator.AnimatorUpdateListener> entry : entries) {
                ValueAnimator.AnimatorUpdateListener updateListener = entry.getValue();
                final ValueAnimator animator = entry.getKey();
                if (updateListener != null) {
                    animator.addUpdateListener(updateListener);
                }
                animator.start();
            }
        }
    }

    @Override
    public void stop() {
        if (animators != null) {
            for (ValueAnimator animator : animators.keySet()) {
                if (animator != null && animator.isStarted()) {
                    animator.removeAllUpdateListeners();
                    animator.end();
                }
            }
        }
    }

    private boolean isStarted() {
        return (!animatorsIsNullOrEmpty()) && animators.keySet().iterator().next().isStarted();
    }

    @Override
    public boolean isRunning() {
        return (!animatorsIsNullOrEmpty()) && animators.keySet().iterator().next().isRunning();
    }

    private boolean animatorsIsNullOrEmpty() {
        return (animators == null || animators.keySet().isEmpty());
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.drawBounds = new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom);

    }

    public int getWidth() {
        return drawBounds.width();
    }

    public int getHeight() {
        return drawBounds.height();
    }

}

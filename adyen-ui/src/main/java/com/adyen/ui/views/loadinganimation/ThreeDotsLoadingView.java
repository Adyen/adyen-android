package com.adyen.ui.views.loadinganimation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.adyen.ui.R;

public class ThreeDotsLoadingView extends View {

    int minWidth = 48;
    int maxWidth = 48;
    int minHeight = 48;
    int maxHeight = 48;

    private Animation animation;
    private int color;

    private boolean shouldStartAnimationDrawable;

    public ThreeDotsLoadingView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public ThreeDotsLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, R.style.LoadingAnimationView);
    }

    public ThreeDotsLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.LoadingAnimationView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ThreeDotsLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, R.style.LoadingAnimationView);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThreeDotsLoadingView,
                defStyleAttr, defStyleRes);

        minWidth = a.getDimensionPixelSize(R.styleable.ThreeDotsLoadingView_minWidth, minWidth);
        maxWidth = a.getDimensionPixelSize(R.styleable.ThreeDotsLoadingView_maxWidth, maxWidth);
        minHeight = a.getDimensionPixelSize(R.styleable.ThreeDotsLoadingView_minHeight, minHeight);
        maxHeight = a.getDimensionPixelSize(R.styleable.ThreeDotsLoadingView_maxHeight, maxHeight);
        color = a.getColor(R.styleable.ThreeDotsLoadingView_indicatorColor, ContextCompat.getColor(getContext(),
                R.color.light_green));

        setLoadingAnimation(new ThreeDotsLoadingAnimation());

        a.recycle();
    }

    public void setLoadingAnimation(ThreeDotsLoadingAnimation d) {
        if (animation != d) {
            if (animation != null) {
                animation.setCallback(null);
                unscheduleDrawable(animation);
            }

            animation = d;
            setColor(color);
            if (d != null) {
                d.setCallback(this);
            }
            postInvalidate();
        }
    }

    public void setColor(int color) {
        this.color = color;
        animation.setColor(color);
    }

    @Override
    protected boolean verifyDrawable(Drawable drawableToVerify) {
        return drawableToVerify == animation || super.verifyDrawable(drawableToVerify);
    }

    void startAnimation() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        shouldStartAnimationDrawable = true;
        postInvalidate();
    }

    void stopAnimation() {
        if (animation != null) {
            animation.stop();
        }
        shouldStartAnimationDrawable = false;
        postInvalidate();
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
            if (v == GONE || v == INVISIBLE) {
                stopAnimation();
            } else {
                startAnimation();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (verifyDrawable(drawable)) {
            final Rect dirty = drawable.getBounds();
            final int scrollX = getScrollX() + getPaddingLeft();
            final int scrollY = getScrollY() + getPaddingTop();

            invalidate(dirty.left + scrollX, dirty.top + scrollY,
                    dirty.right + scrollX, dirty.bottom + scrollY);
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        updateDrawableBounds(width, height);
    }

    private void updateDrawableBounds(int w, int h) {
        w -= getPaddingRight() + getPaddingLeft();
        h -= getPaddingTop() + getPaddingBottom();

        int right = w;
        int bottom = h;
        int top = 0;
        int left = 0;

        if (animation != null) {
            final int intrinsicWidth = animation.getIntrinsicWidth();
            final int intrinsicHeight = animation.getIntrinsicHeight();
            final float intrinsicAspect = (float) intrinsicWidth / intrinsicHeight;
            final float boundAspect = (float) w / h;

            if (Math.abs(intrinsicAspect - boundAspect) > 0.0000001) {
                if (boundAspect > intrinsicAspect) {
                    final int width = (int) (h * intrinsicAspect);
                    left = (w - width) / 2;
                    right = left + width;
                } else {
                    final int height = (int) (w * (1 / intrinsicAspect));
                    top = (h - height) / 2;
                    bottom = top + height;
                }
            }
            animation.setBounds(left, top, right, bottom);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
    }

    void drawTrack(Canvas canvas) {
        if (animation != null) {
            final int saveCount = canvas.save();

            canvas.translate(getPaddingLeft(), getPaddingTop());

            animation.draw(canvas);
            canvas.restoreToCount(saveCount);

            if (shouldStartAnimationDrawable) {
                animation.start();
                shouldStartAnimationDrawable = false;
            }
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = 0;
        int dh = 0;

        final Drawable d = animation;
        if (d != null) {
            dw = Math.max(minWidth, Math.min(maxWidth, d.getIntrinsicWidth()));
            dh = Math.max(minHeight, Math.min(maxHeight, d.getIntrinsicHeight()));
        }

        updateDrawableState();

        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();

        final int measuredWidth = resolveSizeAndState(dw, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(dh, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private void updateDrawableState() {
        final int[] state = getDrawableState();
        if (animation != null && animation.isStateful()) {
            animation.setState(state);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (animation != null) {
            animation.setHotspot(x, y);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

}

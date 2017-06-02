package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class GiroPayEditText extends EditText {

    private Drawable rightDrawable = null;
    private OnDrawableClickListener onDrawableClickListener;
    private static final int DRAWABLE_CLICK_PADDING = 15;

    public GiroPayEditText(Context context) {
        super(context);
    }

    public GiroPayEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GiroPayEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GiroPayEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && rightDrawable != null && onDrawableClickListener != null) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final Rect bounds = rightDrawable.getBounds();

            if (x >= (this.getWidth() - bounds.width() - DRAWABLE_CLICK_PADDING)
                    && x <= (this.getWidth() - this.getPaddingRight() + DRAWABLE_CLICK_PADDING)
                    && y >= (this.getPaddingTop() - DRAWABLE_CLICK_PADDING)
                    && y <= (this.getHeight() - this.getPaddingBottom()) + DRAWABLE_CLICK_PADDING) {

                onDrawableClickListener.onDrawableClick();
                event.setAction(MotionEvent.ACTION_CANCEL);
                return false;
            }
        }
        return super.onTouchEvent(event);
    }


    public void setCancelDrawable(Drawable drawable, OnDrawableClickListener listener) {
        this.rightDrawable = drawable;
        this.onDrawableClickListener = listener;
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

    }

    public interface OnDrawableClickListener {
        void onDrawableClick();
    }

}

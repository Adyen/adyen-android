/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 03/05/2018.
 */

package com.adyen.checkout.ui.internal.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.TextViewUtil;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;

public class CodeView extends AppCompatEditText {
    private static final int DEFAULT_LENGTH = 4;

    private static final String STATE_SUPER_STATE = "STATE_SUPER_STATE";

    private static final String STATE_LENGTH = "STATE_LENGTH";

    private RectF mRectF;

    private Paint mRectPaint;

    private Paint mTintedRectPaint;

    private int mLength;

    private boolean mTouching;

    private InputFilter.LengthFilter mLengthFilter;

    public CodeView(@NonNull Context context) {
        this(context, null);
    }

    public CodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public CodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getPaint().setColor(ThemeUtil.getAttributeColor(context, android.R.attr.textColorPrimary));
        mRectF = new RectF();
        initRectPaints(context);
        setBackgroundColor(Color.TRANSPARENT);
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setCursorVisible(false);
        setMovementMethod(null);
        setImeOptions(getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CodeView);
            setLength(typedArray.getInt(R.styleable.CodeView_codeView_length, DEFAULT_LENGTH));
            typedArray.recycle();
        } else {
            setLength(DEFAULT_LENGTH);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean touching = event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_CANCEL;

        if (mTouching != touching) {
            mTouching = touching;
            invalidate();
        }

        return super.onTouchEvent(event);
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable state = super.onSaveInstanceState();

        if (state != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(STATE_SUPER_STATE, state);
            bundle.putInt(STATE_LENGTH, mLength);
            state = bundle;
        }

        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER_STATE));
            setLength(bundle.getInt(STATE_LENGTH));
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        if (layoutParams != null && layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            final float textSize = getTextSize();
            final float spacing = textSize / 10;
            int measuredWidth = (int) Math.ceil(mLength * (textSize + spacing));
            setMeasuredDimension(measuredWidth, getMeasuredHeight());
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        TextPaint textPaint = getPaint();
        final float ascent = textPaint.ascent();
        final float descent = textPaint.descent();

        final float textSize = getTextSize();
        final float textOffsetInRect = textSize / 4;
        final float strokeWidth = textSize / 20;
        final float cornerRadius = textSize / 10;
        final float spacing = textSize / 10;

        final char[] chars = getText().toString().toCharArray();

        final int baseline = (int) ((getHeight() / 2) - ((descent + ascent) / 2));

        mRectF.set(strokeWidth / 2, baseline + ascent, textSize + strokeWidth / 2, baseline + descent);
        mRectPaint.setStrokeWidth(strokeWidth);
        mTintedRectPaint.setStrokeWidth(strokeWidth);

        for (int i = 0; i < mLength; i++) {
            final boolean highlight = i < chars.length || mTouching;
            canvas.drawRoundRect(mRectF, cornerRadius, cornerRadius, (highlight ? mTintedRectPaint : mRectPaint));

            if (i < chars.length) {
                canvas.drawText(String.valueOf(chars[i]), mRectF.left + textOffsetInRect, baseline, textPaint);
            }

            mRectF.offset(textSize + spacing, 0);
        }
    }

    public void setLength(int length) {
        if (length != mLength) {
            Editable text = getText();
            int textLength = text.length();

            if (textLength > length) {
                text.delete(textLength, length);
            }

            if (mLengthFilter != null) {
                TextViewUtil.removeInputFilter(this, mLengthFilter);
            }

            mLengthFilter = new InputFilter.LengthFilter(length);
            TextViewUtil.addInputFilter(this, mLengthFilter);

            mLength = length;

            invalidate();
        }
    }

    private void initRectPaints(@NonNull Context context) {
        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setColor(ContextCompat.getColor(context, R.color.code_view_rect));
        mRectPaint.setStyle(Paint.Style.STROKE);

        mTintedRectPaint = new Paint();
        mTintedRectPaint.setAntiAlias(true);
        mTintedRectPaint.setColor(ThemeUtil.getPrimaryThemeColor(context));
        mTintedRectPaint.setStyle(Paint.Style.STROKE);
    }
}

/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/08/2017.
 */

package com.adyen.checkout.ui.internal.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

public class CustomTextInputLayout extends LinearLayout {
    private TextView mCaptionTextView;

    private CharSequence mCaption;

    private CharSequence mHint;

    public CustomTextInputLayout(@NonNull Context context) {
        this(context, null);
    }

    public CustomTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        updateCaptionAndHint();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        TextView editText = getInputText();

        if (editText != null) {
            mCaptionTextView.setPadding(
                    editText.getPaddingLeft(),
                    mCaptionTextView.getPaddingTop(),
                    editText.getPaddingRight(),
                    mCaptionTextView.getPaddingBottom()
            );
            // Was throwing an exception when rendering on Android Studio
            if (!isInEditMode()) {
                int paddingTop = getResources().getDimensionPixelSize(R.dimen.standard_half_margin);
                editText.setPadding(editText.getPaddingLeft(), paddingTop, editText.getPaddingRight(), editText.getPaddingBottom());
            }

            editText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    updateCaptionAndHint();
                }
            });
        }
    }

    public void setCaption(@StringRes int captionResId) {
        setCaption(getResources().getText(captionResId));
    }

    public void setCaption(@Nullable CharSequence caption) {
        mCaption = caption;
        mCaptionTextView.setText(mCaption);
        updateCaptionAndHint();
    }

    public void setHint(@StringRes int hintResId) {
        setHint(getResources().getText(hintResId));
    }

    public void setHint(@NonNull CharSequence hint) {
        mHint = hint;
        updateCaptionAndHint();
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        setOrientation(VERTICAL);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomTextInputLayout);
        mCaption = typedArray.getString(R.styleable.CustomTextInputLayout_customTextInputLayout_caption);
        mHint = typedArray.getString(R.styleable.CustomTextInputLayout_customTextInputLayout_hint);
        typedArray.recycle();

        final LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mCaptionTextView = new AppCompatTextView(context);
        mCaptionTextView.setMaxLines(1);
        mCaptionTextView.setEllipsize(TextUtils.TruncateAt.END);
        mCaptionTextView.setLayoutParams(layoutParams);
        mCaptionTextView.setTextColor(ThemeUtil.getPrimaryThemeColor(context));
        addView(mCaptionTextView, 0);

        setAddStatesFromChildren(true);
        updateCaptionAndHint();
    }

    @Nullable
    private TextView getInputText() {
        if (getChildCount() >= 2) {
            View secondChild = getChildAt(1);

            if (secondChild instanceof TextView) {
                return (TextView) secondChild;
            }
        }

        return null;
    }

    private void updateCaptionAndHint() {
        TextView editText = getInputText();

        if (editText != null) {
            int prevVisibilityCaption = mCaptionTextView.getVisibility();
            final int newVisibilityCaption = editText.hasFocus() || editText.length() > 0 ? VISIBLE : INVISIBLE;
            mCaptionTextView.setText(mCaption);
            mCaptionTextView.setVisibility(newVisibilityCaption);

            if (prevVisibilityCaption != newVisibilityCaption && !isInEditMode()) {
                Animation animation;

                if (newVisibilityCaption == VISIBLE) {
                    animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_from_bottom);
                } else {
                    animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_to_bottom);
                }

                mCaptionTextView.startAnimation(animation);
            }

            if (newVisibilityCaption == VISIBLE) {
                editText.setHint(mHint);
            } else {
                editText.setHint(mCaption);
            }
        }
    }
}

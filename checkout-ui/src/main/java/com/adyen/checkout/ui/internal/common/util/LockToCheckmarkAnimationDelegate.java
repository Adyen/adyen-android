package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;
import android.widget.TextView;

import com.adyen.checkout.ui.R;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 25/04/2018.
 */
public final class LockToCheckmarkAnimationDelegate {
    private final TextView mTextView;

    private final ValidationCallback mValidationCallback;

    private boolean mValid;

    public LockToCheckmarkAnimationDelegate(@NonNull TextView textView, @NonNull ValidationCallback validationCallback) {
        mTextView = textView;
        mValidationCallback = validationCallback;

        setDrawableRight();
        performValidation();
    }

    public void onTextChanged() {
        performValidation();
    }

    public void onFocusChanged() {
        performValidation();
    }

    private void performValidation() {
        boolean valid = mValidationCallback.isValid();

        if (valid != mValid) {
            setDrawableRight();
            mValid = valid;

            Drawable drawable = mTextView.getCompoundDrawables()[2];

            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        }
    }

    private void setDrawableRight() {
        Context context = mTextView.getContext();

        if (mValid) {
            //noinspection ConstantConditions
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_lock_to_checkmark_animated_reverse).mutate();
            ThemeUtil.setTintFromAttributeColor(context, drawable, R.attr.colorIconActive);
            TextViewUtil.setCompoundDrawableRight(mTextView, drawable);
        } else {
            //noinspection ConstantConditions
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_lock_to_checkmark_animated).mutate();
            ThemeUtil.setTintFromAttributeColor(context, drawable, R.attr.colorIconActive);
            TextViewUtil.setCompoundDrawableRight(mTextView, drawable);
        }
    }

    public interface ValidationCallback {
        boolean isValid();
    }
}

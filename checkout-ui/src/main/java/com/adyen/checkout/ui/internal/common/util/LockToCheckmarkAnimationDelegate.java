package com.adyen.checkout.ui.internal.common.util;

import android.support.annotation.NonNull;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
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

    private final AnimatedVectorDrawableCompat mAnimatedVectorDrawable;

    private final AnimatedVectorDrawableCompat mAnimatedVectorDrawableReverse;

    private boolean mValid;

    public LockToCheckmarkAnimationDelegate(@NonNull TextView textView, @NonNull ValidationCallback validationCallback) {
        mTextView = textView;
        mValidationCallback = validationCallback;

        mAnimatedVectorDrawable = AnimatedVectorDrawableCompat.create(mTextView.getContext(), R.drawable.ic_lock_to_checkmark_animated);
        mAnimatedVectorDrawableReverse = AnimatedVectorDrawableCompat
                .create(mTextView.getContext(), R.drawable.ic_lock_to_checkmark_animated_reverse);

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
            ((AnimatedVectorDrawableCompat) mTextView.getCompoundDrawables()[2]).start();
        }
    }

    private void setDrawableRight() {
        if (mValid) {
            TextViewUtil.setCompoundDrawableRight(mTextView, mAnimatedVectorDrawableReverse.mutate());
        } else {
            TextViewUtil.setCompoundDrawableRight(mTextView, mAnimatedVectorDrawable.mutate());
        }
    }

    public interface ValidationCallback {
        boolean isValid();
    }
}

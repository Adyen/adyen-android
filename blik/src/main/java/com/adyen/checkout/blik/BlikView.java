/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */

package com.adyen.checkout.blik;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod;
import com.adyen.checkout.components.ui.Validation;
import com.adyen.checkout.components.ui.view.AdyenLinearLayout;
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.google.android.material.textfield.TextInputLayout;

public class BlikView
        extends AdyenLinearLayout<BlikOutputData, BlikConfiguration, GenericComponentState<BlikPaymentMethod>, BlikComponent>
        implements Observer<BlikOutputData> {
    private static final String TAG = LogUtil.getTag();

    BlikInputData mBlikInputData = new BlikInputData();

    TextInputLayout mBlikCodeInput;

    AdyenTextInputEditText mBlikCodeEditText;

    public BlikView(@NonNull Context context) {
        this(context, null);
    }

    public BlikView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public BlikView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.blik_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);
    }

    @Override
    protected void initLocalizedStrings(@NonNull Context localizedContext) {
        final int[] myAttrs = {android.R.attr.hint};
        final TypedArray typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Blik_BlikCodeInput, myAttrs);
        mBlikCodeInput.setHint(typedArray.getString(0));
        typedArray.recycle();
    }

    @Override
    public void initView() {
        mBlikCodeInput = findViewById(R.id.textInputLayout_blikCode);
        mBlikCodeEditText = (AdyenTextInputEditText) mBlikCodeInput.getEditText();

        if (mBlikCodeEditText == null) {
            throw new CheckoutException("Could not find views inside layout.");
        }

        mBlikCodeEditText.setOnChangeListener(editable -> {
            mBlikInputData.setBlikCode(mBlikCodeEditText.getRawValue());
            notifyInputDataChanged();
            mBlikCodeInput.setError(null);
        });

        mBlikCodeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final BlikOutputData outputData = getComponent().getOutputData();
            final Validation blikCodeValidation = outputData != null
                    ? outputData.getBlikCodeField().getValidation()
                    : null;
            if (hasFocus) {
                mBlikCodeInput.setError(null);
            } else if (blikCodeValidation != null && !blikCodeValidation.isValid()) {
                final int errorReasonResId = ((Validation.Invalid) blikCodeValidation).getReason();
                mBlikCodeInput.setError(mLocalizedContext.getString(errorReasonResId));
            }
        });
    }

    @Override
    public void onChanged(@Nullable BlikOutputData blikOutputData) {
        Logger.v(TAG, "blikOutputData changed");
    }

    @Override
    public void onComponentAttached() {
        // nothing to impl
    }

    @Override
    protected void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner) {
        getComponent().observeOutputData(lifecycleOwner, this);
    }

    @Override
    public boolean isConfirmationRequired() {
        return true;
    }

    @Override
    public void highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors");

        final BlikOutputData outputData;
        if (getComponent().getOutputData() != null) {
            outputData = getComponent().getOutputData();
        } else {
            return;
        }

        final Validation blikCodeValidation = outputData.getBlikCodeField().getValidation();

        if (!blikCodeValidation.isValid()) {
            mBlikCodeInput.requestFocus();
            final int errorReasonResId = ((Validation.Invalid) blikCodeValidation).getReason();
            mBlikCodeInput.setError(mLocalizedContext.getString(errorReasonResId));
        }
    }

    void notifyInputDataChanged() {
        getComponent().inputDataChanged(mBlikInputData);
    }
}

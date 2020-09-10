/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */

package com.adyen.checkout.mbway;

import android.annotation.SuppressLint;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.ui.view.AdyenLinearLayout;
import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.mbway.ui.R;

@SuppressWarnings("AbbreviationAsWordInName")
public class MBWayView extends AdyenLinearLayout<MBWayOutputData, MBWayConfiguration, PaymentComponentState, MBWayComponent>
        implements Observer<MBWayOutputData> {
    private static final String TAG = LogUtil.getTag();

    @SuppressLint(Lint.SYNTHETIC)
    MBWayInputData mMBWayInputData = new MBWayInputData();

    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mEmailInput;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mMobileNumberInput;

    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mEmailEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mMobileNumberEditText;


    public MBWayView(@NonNull Context context) {
        this(context, null);
    }

    public MBWayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public MBWayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.mbway_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);
    }



    @Override
    protected void initLocalizedStrings(@NonNull Context localizedContext) {
        final int[] myAttrs = {android.R.attr.hint};
        TypedArray typedArray;

        // Email
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_MBWay_EmailInput, myAttrs);
        mEmailInput.setHint(typedArray.getString(0));
        typedArray.recycle();

        // Mobile Number
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_MBWay_MobileNumberInput, myAttrs);
        mMobileNumberInput.setHint(typedArray.getString(0));
        typedArray.recycle();
    }

    @Override
    public void initView() {
        mEmailInput = findViewById(R.id.textInputLayout_email);
        mMobileNumberInput = findViewById(R.id.textInputLayout_mobileNumber);
        mEmailEditText = (AdyenTextInputEditText) mEmailInput.getEditText();
        mMobileNumberEditText = (AdyenTextInputEditText) mMobileNumberInput.getEditText();

        if (mEmailEditText == null || mMobileNumberEditText == null) {
            throw new CheckoutException("Could not find views inside layout.");
        }

        mEmailEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mMBWayInputData.setEmail(mEmailEditText.getRawValue());
                notifyInputDataChanged();
                mEmailInput.setError(null);
            }
        });

        mMobileNumberEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mMBWayInputData.setMobilePhoneNumber(mMobileNumberEditText.getRawValue());
                notifyInputDataChanged();
                mMobileNumberInput.setError(null);
            }
        });

        mEmailEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final MBWayOutputData outputData = getComponent().getOutputData();
                if (hasFocus) {
                    mEmailInput.setError(null);
                } else if (outputData != null && !outputData.getEmailField().isValid()) {
                    mEmailInput.setError(mLocalizedContext.getString(R.string.checkout_mbway_email_not_valid));
                }
            }
        });

        mMobileNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final MBWayOutputData outputData = getComponent().getOutputData();
                if (hasFocus) {
                    mMobileNumberInput.setError(null);
                } else if (outputData != null && !outputData.getMobilePhoneNumberField().isValid()) {
                    mMobileNumberInput.setError(mLocalizedContext.getString(R.string.checkout_mbway_phone_number_not_valid));
                }
            }
        });
    }

    @Override
    protected void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner) {
        getComponent().observeOutputData(lifecycleOwner, this);
    }

    @Override
    public void onComponentAttached() {
        // nothing to impl
    }

    @Override
    public void onChanged(@Nullable MBWayOutputData mbWayOutputData) {
        Logger.v(TAG, "MBWayOutputData changed");
    }

    @Override
    public boolean isConfirmationRequired() {
        return true;
    }

    @Override
    public void highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors");

        final MBWayOutputData outputData;
        if (getComponent().getOutputData() != null) {
            outputData = getComponent().getOutputData();
        } else {
            return;
        }

        boolean errorFocused = false;

        if (!outputData.getEmailField().isValid()) {
            errorFocused = true;
            mEmailInput.requestFocus();
            mEmailInput.setError(mLocalizedContext.getString(R.string.checkout_mbway_email_not_valid));
        }

        if (!outputData.getMobilePhoneNumberField().isValid()) {
            if (!errorFocused) {
                mMobileNumberInput.requestFocus();
            }
            mMobileNumberInput.setError(mLocalizedContext.getString(R.string.checkout_mbway_phone_number_not_valid));
        }
    }

    @SuppressLint(Lint.SYNTHETIC)
    void notifyInputDataChanged() {
        getComponent().inputDataChanged(mMBWayInputData);
    }
}

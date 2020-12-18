/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */

package com.adyen.checkout.mbway;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.adyen.checkout.components.PaymentComponentState;
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod;
import com.adyen.checkout.components.ui.view.AdyenLinearLayout;
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.mbway.ui.R;
import com.google.android.material.textfield.TextInputLayout;

@SuppressWarnings("AbbreviationAsWordInName")
public class MBWayView
        extends AdyenLinearLayout<MBWayOutputData, MBWayConfiguration, PaymentComponentState<MBWayPaymentMethod>, MBWayComponent>
        implements Observer<MBWayOutputData> {
    private static final String TAG = LogUtil.getTag();

    MBWayInputData mMBWayInputData = new MBWayInputData();

    TextInputLayout mMobileNumberInput;

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

        final TypedArray typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_MBWay_MobileNumberInput, myAttrs);
        mMobileNumberInput.setHint(typedArray.getString(0));
        typedArray.recycle();
    }

    @Override
    public void initView() {
        mMobileNumberInput = findViewById(R.id.textInputLayout_mobileNumber);
        mMobileNumberEditText = (AdyenTextInputEditText) mMobileNumberInput.getEditText();

        if (mMobileNumberEditText == null) {
            throw new CheckoutException("Could not find views inside layout.");
        }

        mMobileNumberEditText.setOnChangeListener(editable -> {
            mMBWayInputData.setMobilePhoneNumber(mMobileNumberEditText.getRawValue());
            notifyInputDataChanged();
            mMobileNumberInput.setError(null);
        });

        mMobileNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final MBWayOutputData outputData = getComponent().getOutputData();
            if (hasFocus) {
                mMobileNumberInput.setError(null);
            } else if (outputData != null && !outputData.getMobilePhoneNumberField().isValid()) {
                mMobileNumberInput.setError(mLocalizedContext.getString(R.string.checkout_mbway_phone_number_not_valid));
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

        if (!outputData.getMobilePhoneNumberField().isValid()) {
            mMobileNumberInput.setError(mLocalizedContext.getString(R.string.checkout_mbway_phone_number_not_valid));
        }
    }

    void notifyInputDataChanged() {
        getComponent().inputDataChanged(mMBWayInputData);
    }
}

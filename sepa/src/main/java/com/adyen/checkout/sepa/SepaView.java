/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/8/2019.
 */

package com.adyen.checkout.sepa;

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
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod;
import com.adyen.checkout.components.ui.Validation;
import com.adyen.checkout.components.ui.view.AdyenLinearLayout;
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.google.android.material.textfield.TextInputLayout;

public class SepaView
        extends AdyenLinearLayout<SepaOutputData, SepaConfiguration, GenericComponentState<SepaPaymentMethod>, SepaComponent>
        implements Observer<SepaOutputData> {
    private static final String TAG = LogUtil.getTag();

    SepaInputData mSepaInputData = new SepaInputData();

    TextInputLayout mHolderNameInput;

    TextInputLayout mIbanNumberInput;

    AdyenTextInputEditText mHolderNameEditText;
    AdyenTextInputEditText mIbanNumberEditText;

    public SepaView(@NonNull Context context) {
        this(context, null);
    }

    public SepaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public SepaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.sepa_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);
    }

    @Override
    protected void initLocalizedStrings(@NonNull Context localizedContext) {
        final int[] myAttrs = {android.R.attr.hint};
        TypedArray typedArray;

        // Holder name
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Sepa_HolderNameInput, myAttrs);
        mHolderNameInput.setHint(typedArray.getString(0));
        typedArray.recycle();

        // Account Number
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Sepa_AccountNumberInput, myAttrs);
        mIbanNumberInput.setHint(typedArray.getString(0));
        typedArray.recycle();
    }

    @Override
    public void initView() {
        mHolderNameInput = findViewById(R.id.textInputLayout_holderName);
        mIbanNumberInput = findViewById(R.id.textInputLayout_ibanNumber);
        mHolderNameEditText = (AdyenTextInputEditText) mHolderNameInput.getEditText();
        mIbanNumberEditText = (AdyenTextInputEditText) mIbanNumberInput.getEditText();

        if (mHolderNameEditText == null || mIbanNumberEditText == null) {
            throw new CheckoutException("Could not find views inside layout.");
        }

        mHolderNameEditText.setOnChangeListener(editable -> {
            mSepaInputData.setName(mHolderNameEditText.getRawValue());
            notifyInputDataChanged();
            mHolderNameInput.setError(null);
        });

        mIbanNumberEditText.setOnChangeListener(editable -> {
            mSepaInputData.setIban(mIbanNumberEditText.getRawValue());
            notifyInputDataChanged();
            mIbanNumberInput.setError(null);
        });

        mIbanNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final SepaOutputData outputData = getComponent().getOutputData();
            final Validation ibanNumberValidation = outputData != null
                    ? outputData.getIbanNumberField().getValidation()
                    : null;
            if (hasFocus) {
                mIbanNumberInput.setError(null);
            } else if (ibanNumberValidation != null && !ibanNumberValidation.isValid()) {
                final int errorReasonResId = ((Validation.Invalid) ibanNumberValidation).getReason();
                mIbanNumberInput.setError(mLocalizedContext.getString(errorReasonResId));
            }
        });
    }

    @Override
    public void onChanged(@Nullable SepaOutputData sepaOutputData) {
        Logger.v(TAG, "sepaOutputData changed");
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

        final SepaOutputData outputData;
        if (getComponent().getOutputData() != null) {
            outputData = getComponent().getOutputData();
        } else {
            return;
        }

        boolean errorFocused = false;

        final Validation ownerNameValidation = outputData.getOwnerNameField().getValidation();
        if (!ownerNameValidation.isValid()) {
            errorFocused = true;
            mHolderNameInput.requestFocus();
            final int errorReasonResId = ((Validation.Invalid) ownerNameValidation).getReason();
            mHolderNameInput.setError(mLocalizedContext.getString(errorReasonResId));
        }

        final Validation ibanNumberValidation = outputData.getIbanNumberField().getValidation();
        if (!ibanNumberValidation.isValid()) {
            if (!errorFocused) {
                mIbanNumberInput.requestFocus();
            }
            final int errorReasonResId = ((Validation.Invalid) ibanNumberValidation).getReason();
            mIbanNumberInput.setError(mLocalizedContext.getString(errorReasonResId));
        }
    }

    void notifyInputDataChanged() {
        getComponent().inputDataChanged(mSepaInputData);
    }
}

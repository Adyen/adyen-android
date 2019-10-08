/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/8/2019.
 */

package com.adyen.checkout.sepa;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.adyen.checkout.base.ui.view.AdyenLinearLayout;
import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.sepa.ui.R;

public class SepaView extends AdyenLinearLayout<SepaComponent> implements Observer<SepaOutputData> {
    private static final String TAG = LogUtil.getTag();

    @SuppressLint(Lint.SYNTHETIC)
    SepaInputData mSepaInputData = new SepaInputData();

    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mHolderNameInput;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mIbanNumberInput;

    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mHolderNameEditText;
    @SuppressLint(Lint.SYNTHETIC)
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
    public void initView() {
        mHolderNameInput = findViewById(R.id.textInputLayout_holderName);
        mIbanNumberInput = findViewById(R.id.textInputLayout_ibanNumber);
        mHolderNameEditText = (AdyenTextInputEditText) mHolderNameInput.getEditText();
        mIbanNumberEditText = (AdyenTextInputEditText) mIbanNumberInput.getEditText();

        if (mHolderNameEditText == null || mIbanNumberEditText == null) {
            throw new CheckoutException("Could not find views inside layout.");
        }

        mHolderNameEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mSepaInputData.setName(mHolderNameEditText.getRawValue());
                notifyInputDataChanged();
            }
        });

        mIbanNumberEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mSepaInputData.setIban(mIbanNumberEditText.getRawValue());
                notifyInputDataChanged();
            }
        });
        mIbanNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mIbanNumberInput.setErrorEnabled(!hasFocus);

                if (!hasFocus && !getComponent().getOutputData().getIbanNumberField().isValid()) {
                    mIbanNumberInput.setError(getContext().getString(R.string.checkout_iban_not_valid));
                }
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
    public void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner) {
        getComponent().observeOutputData(lifecycleOwner, this);
    }

    @Override
    public boolean isConfirmationRequired() {
        return true;
    }

    @SuppressLint(Lint.SYNTHETIC)
    void notifyInputDataChanged() {
        getComponent().inputDataChanged(mSepaInputData);
    }
}

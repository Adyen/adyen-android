/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.adyen.checkout.card.ui.CardNumberInput;
import com.adyen.checkout.card.ui.ExpiryDateInput;
import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.api.ImageLoader;
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod;
import com.adyen.checkout.components.ui.FieldState;
import com.adyen.checkout.components.ui.Validation;
import com.adyen.checkout.components.ui.view.AdyenLinearLayout;
import com.adyen.checkout.components.ui.view.RoundCornerImageView;
import com.google.android.material.textfield.TextInputLayout;

/**
 * CardView for {@link BcmcComponent}.
 */
public final class BcmcView
        extends AdyenLinearLayout<BcmcOutputData, BcmcConfiguration, GenericComponentState<CardPaymentMethod>, BcmcComponent>
        implements Observer<BcmcOutputData> {

    private RoundCornerImageView mCardBrandLogoImageView;

    private CardNumberInput mCardNumberEditText;
    private ExpiryDateInput mExpiryDateEditText;

    private TextInputLayout mExpiryDateInput;
    private TextInputLayout mCardNumberInput;

    private SwitchCompat mSwitchStorePaymentMethod;

    private final BcmcInputData mCardInputData = new BcmcInputData();

    private ImageLoader mImageLoader;

    public BcmcView(@NonNull Context context) {
        this(context, null);
    }

    public BcmcView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public BcmcView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.bcmc_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);
    }

    @Override
    protected void initLocalizedStrings(@NonNull Context localizedContext) {
        int[] myAttrs = {android.R.attr.hint};
        TypedArray typedArray;

        // Card Number
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_CardNumberInput, myAttrs);
        mCardNumberInput.setHint(typedArray.getString(0));
        typedArray.recycle();

        // Expiry Date
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_ExpiryDateInput, myAttrs);
        mExpiryDateInput.setHint(typedArray.getString(0));
        typedArray.recycle();

        // Store Switch
        myAttrs = new int[] {android.R.attr.text};
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_StorePaymentSwitch, myAttrs);
        mSwitchStorePaymentMethod.setText(typedArray.getString(0));
        typedArray.recycle();
    }

    @Override
    public void initView() {
        mCardBrandLogoImageView = findViewById(R.id.cardBrandLogo_imageView);

        initCardNumberInput();
        initExpiryDateInput();
        initStorePaymentMethodSwitch();
    }

    @Override
    public void onComponentAttached() {
        mImageLoader = ImageLoader.getInstance(getContext(), getComponent().getConfiguration().getEnvironment());
    }

    @Override
    public void onChanged(@Nullable BcmcOutputData cardOutputData) {
        if (cardOutputData != null) {
            onCardNumberValidated(cardOutputData.getCardNumberField());
        }
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
        final BcmcOutputData outputData;
        if (getComponent().getOutputData() != null) {
            outputData = getComponent().getOutputData();
        } else {
            return;
        }

        boolean isErrorFocused = false;

        final Validation cardNumberValidation = outputData.getCardNumberField().getValidation();
        if (!cardNumberValidation.isValid()) {
            isErrorFocused = true;
            mCardNumberEditText.requestFocus();
            final int errorReasonResId = ((Validation.Invalid) cardNumberValidation).getReason();
            setCardNumberError(errorReasonResId);
        }

        final Validation expiryFieldValidation = outputData.getExpiryDateField().getValidation();
        if (!expiryFieldValidation.isValid()) {
            if (!isErrorFocused) {
                mExpiryDateInput.requestFocus();
            }
            final int errorReasonResId = ((Validation.Invalid) expiryFieldValidation).getReason();
            mExpiryDateInput.setError(mLocalizedContext.getString(errorReasonResId));
        }
    }

    private void notifyInputDataChanged() {
        getComponent().inputDataChanged(mCardInputData);
    }

    private void onCardNumberValidated(@NonNull FieldState<String> validatedNumber) {
        if (!getComponent().isCardNumberSupported(validatedNumber.getValue())) {
            mCardBrandLogoImageView.setStrokeWidth(0f);
            mCardBrandLogoImageView.setImageResource(R.drawable.ic_card);
        } else {
            mCardBrandLogoImageView.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH);
            mImageLoader.load(BcmcComponent.SUPPORTED_CARD_TYPE.getTxVariant(), mCardBrandLogoImageView);
        }
    }

    private void initCardNumberInput() {
        mCardNumberInput = findViewById(R.id.textInputLayout_cardNumber);
        mCardNumberEditText = (CardNumberInput) mCardNumberInput.getEditText();
        //noinspection ConstantConditions
        mCardNumberEditText.setOnChangeListener(editable -> {
            mCardInputData.setCardNumber(mCardNumberEditText.getRawValue());
            notifyInputDataChanged();
            setCardNumberError(null);
        });
        mCardNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final BcmcOutputData outputData = getComponent().getOutputData();
            final Validation cardNumberValidation = outputData != null
                    ? outputData.getCardNumberField().getValidation()
                    : null;
            if (hasFocus) {
                setCardNumberError(null);
            } else if (cardNumberValidation != null && !cardNumberValidation.isValid()) {
                final int errorReasonResId = ((Validation.Invalid) cardNumberValidation).getReason();
                setCardNumberError(errorReasonResId);
            }
        });
    }

    private void setCardNumberError(@StringRes Integer stringResId) {
        if (stringResId == null) {
            mCardNumberInput.setError(null);
            mCardBrandLogoImageView.setVisibility(View.VISIBLE);
        } else {
            mCardNumberInput.setError(mLocalizedContext.getString(stringResId));
            mCardBrandLogoImageView.setVisibility(View.GONE);
        }
    }

    private void initExpiryDateInput() {
        mExpiryDateInput = findViewById(R.id.textInputLayout_expiryDate);
        mExpiryDateEditText = (ExpiryDateInput) mExpiryDateInput.getEditText();
        //noinspection ConstantConditions
        mExpiryDateEditText.setOnChangeListener(editable -> {
            mCardInputData.setExpiryDate(mExpiryDateEditText.getDate());
            notifyInputDataChanged();
            mExpiryDateInput.setError(null);
        });
        mExpiryDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final BcmcOutputData outputData = getComponent().getOutputData();
            final Validation expiryDateValidation = outputData != null
                    ? outputData.getExpiryDateField().getValidation()
                    : null;
            if (hasFocus) {
                mExpiryDateInput.setError(null);
            } else if (expiryDateValidation != null && !expiryDateValidation.isValid()) {
                final int errorReasonResId = ((Validation.Invalid) expiryDateValidation).getReason();
                mExpiryDateInput.setError(mLocalizedContext.getString(errorReasonResId));
            }
        });
    }

    private void initStorePaymentMethodSwitch() {
        mSwitchStorePaymentMethod = findViewById(R.id.switch_storePaymentMethod);

        mSwitchStorePaymentMethod.setVisibility(getComponent().getConfiguration().isStorePaymentFieldVisible() ? VISIBLE : GONE);
        mSwitchStorePaymentMethod.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mCardInputData.setStorePaymentSelected(isChecked);
            notifyInputDataChanged();
        });
    }
}

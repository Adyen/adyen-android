/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc;

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
import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.view.AdyenLinearLayout;
import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.base.ui.view.RoundCornerImageView;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.bcmc.ui.R;
import com.adyen.checkout.card.CardValidationUtils;
import com.adyen.checkout.card.ui.CardNumberInput;
import com.adyen.checkout.card.ui.ExpiryDateInput;

/**
 * CardView for {@link BcmcComponent}.
 */
@SuppressWarnings("SyntheticAccessor")
public final class BcmcView
        extends AdyenLinearLayout<BcmcOutputData, BcmcConfiguration, PaymentComponentState, BcmcComponent> implements Observer<BcmcOutputData> {

    private RoundCornerImageView mCardBrandLogoImageView;

    private CardNumberInput mCardNumberEditText;
    private ExpiryDateInput mExpiryDateEditText;

    private TextInputLayout mExpiryDateInput;
    private TextInputLayout mCardNumberInput;

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
        final int[] myAttrs = {android.R.attr.hint};
        TypedArray typedArray;

        // Card Number
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_CardNumberInput, myAttrs);
        mCardNumberInput.setHint(typedArray.getString(0));
        typedArray.recycle();

        // Expiry Date
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_ExpiryDateInput, myAttrs);
        mExpiryDateInput.setHint(typedArray.getString(0));
        typedArray.recycle();
    }

    @Override
    public void initView() {
        mCardBrandLogoImageView = findViewById(R.id.cardBrandLogo_imageView);

        initCardNumberInput();
        initExpiryDateInput();
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

        if (!outputData.getCardNumberField().isValid()) {
            isErrorFocused = true;
            mCardNumberEditText.requestFocus();
            mCardNumberInput.setError(mLocalizedContext.getString(R.string.checkout_card_number_not_valid));
        }

        if (!outputData.getExpiryDateField().isValid()) {
            if (!isErrorFocused) {
                mExpiryDateInput.requestFocus();
            }
            mExpiryDateInput.setError(mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid));
        }
    }

    private void notifyInputDataChanged() {
        getComponent().inputDataChanged(mCardInputData);
    }

    private void onCardNumberValidated(@NonNull ValidatedField<String> validatedNumber) {
        if (validatedNumber.getValidation() == ValidatedField.Validation.VALID) {
            changeFocusOfInput(validatedNumber.getValue());
        }

        if (!getComponent().isCardNumberSupported(validatedNumber.getValue())) {
            mCardBrandLogoImageView.setStrokeWidth(0f);
            mCardBrandLogoImageView.setImageResource(R.drawable.ic_card);
        } else {
            mCardBrandLogoImageView.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH);
            mImageLoader.load(BcmcComponent.SUPPORTED_CARD_TYPE.getTxVariant(), mCardBrandLogoImageView);
        }
    }

    private void changeFocusOfInput(String numberValue) {
        final int length = numberValue.length();

        if (length == CardValidationUtils.MAXIMUM_CARD_NUMBER_LENGTH) {
            goToNextInputIfFocus(mCardNumberEditText);
        }
    }

    private void goToNextInputIfFocus(View view) {
        if (getRootView().findFocus() == view) {
            findViewById(view.getNextFocusForwardId()).requestFocus();
        }
    }

    private void initCardNumberInput() {
        mCardNumberInput = findViewById(R.id.textInputLayout_cardNumber);
        mCardNumberEditText = (CardNumberInput) mCardNumberInput.getEditText();
        //noinspection ConstantConditions
        mCardNumberEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mCardInputData.setCardNumber(mCardNumberEditText.getRawValue());
                notifyInputDataChanged();
                mCardNumberInput.setError(null);
            }
        });
        mCardNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final BcmcOutputData outputData = getComponent().getOutputData();
                if (hasFocus) {
                    mCardNumberInput.setError(null);
                } else if (outputData != null && !outputData.getCardNumberField().isValid()) {
                    mCardNumberInput.setError(mLocalizedContext.getString(R.string.checkout_card_number_not_valid));
                }
            }
        });
    }

    private void initExpiryDateInput() {
        mExpiryDateInput = findViewById(R.id.textInputLayout_expiryDate);
        mExpiryDateEditText = (ExpiryDateInput) mExpiryDateInput.getEditText();
        //noinspection ConstantConditions
        mExpiryDateEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mCardInputData.setExpiryDate(mExpiryDateEditText.getDate());
                notifyInputDataChanged();
                mExpiryDateInput.setError(null);
            }
        });
        mExpiryDateEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final BcmcOutputData outputData = getComponent().getOutputData();
                if (hasFocus) {
                    mExpiryDateInput.setError(null);
                } else if (outputData != null && !outputData.getExpiryDateField().isValid()) {
                    mExpiryDateInput.setError(mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid));
                }
            }
        });
    }
}

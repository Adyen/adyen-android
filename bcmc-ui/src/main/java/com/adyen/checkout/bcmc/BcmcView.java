/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc;

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

import com.adyen.checkout.base.ComponentView;
import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.base.ui.view.RoundCornerImageView;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.bcmc.data.BcmcInputData;
import com.adyen.checkout.bcmc.data.BcmcOutputData;
import com.adyen.checkout.bcmc.ui.R;
import com.adyen.checkout.card.CardValidationUtils;
import com.adyen.checkout.card.ui.CardNumberInput;
import com.adyen.checkout.card.ui.ExpiryDateInput;

/**
 * CardView for {@link BcmcComponent}.
 */
@SuppressWarnings("SyntheticAccessor")
public final class BcmcView extends LinearLayout implements ComponentView<BcmcComponent>, Observer<BcmcOutputData> {

    private RoundCornerImageView mCardBrandLogoImageView;

    private final CardNumberInput mCardNumberEditText;
    private final ExpiryDateInput mExpiryDateEditText;

    private final TextInputLayout mExpiryDateInput;
    private final TextInputLayout mCardNumberInput;

    private final BcmcInputData mCardInputData;

    private ImageLoader mImageLoader;

    @Nullable
    private BcmcComponent mComponent;

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

        LayoutInflater.from(context).inflate(R.layout.bcmc_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);

        mCardBrandLogoImageView = findViewById(R.id.cardBrandLogo_imageView);

        mCardNumberInput = findViewById(R.id.textInputLayout_cardNumber);
        mCardNumberEditText = (CardNumberInput) mCardNumberInput.getEditText();
        mCardNumberEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(Editable editable) {
                mCardInputData.setCardNumber(mCardNumberEditText.getRawValue());
                notifyInputDataChanged();
            }
        });
        mCardNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mCardNumberInput.setErrorEnabled(!hasFocus);

                if (!hasFocus && (isOutputEmpty() || !mComponent.getOutputData().getCardNumberField().isValid())) {
                    mCardNumberInput.setError(getContext().getString(R.string.checkout_card_number_not_valid));
                }
            }
        });

        mExpiryDateInput = findViewById(R.id.textInputLayout_expiryDate);
        mExpiryDateEditText = (ExpiryDateInput) mExpiryDateInput.getEditText();
        mExpiryDateEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(Editable editable) {

                mCardInputData.setExpiryDate(mExpiryDateEditText.getDate());
                notifyInputDataChanged();
            }
        });
        mExpiryDateEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mExpiryDateInput.setErrorEnabled(!hasFocus);

                if (!hasFocus && (isOutputEmpty() || !mComponent.getOutputData().getExpiryDateField().isValid())) {
                    mExpiryDateInput.setError(getContext().getString(R.string.checkout_expiry_date_not_valid));
                }
            }
        });

        mCardInputData = new BcmcInputData();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onChanged(@NonNull BcmcOutputData cardOutputData) {
        if (!cardOutputData.isEmpty()) {
            onCardNumberValidated(cardOutputData.getCardNumberField());
        }
    }

    @Override
    public void attach(@NonNull BcmcComponent component, @NonNull LifecycleOwner lifecycleOwner) {
        mComponent = component;

        mImageLoader = ImageLoader.getInstance(getContext(), component.getConfiguration().getEnvironment());

        mComponent.observeOutputData(lifecycleOwner, this);

        mComponent.sendAnalyticsEvent(getContext());
    }

    @Override
    public boolean isConfirmationRequired() {
        return true;
    }

    private void notifyInputDataChanged() {
        if (mComponent != null) {
            mComponent.inputDataChanged(mCardInputData);
        }
    }

    private void onCardNumberValidated(@NonNull ValidatedField<String> validatedNumber) {
        if (validatedNumber.getValidation() == ValidatedField.Validation.VALID) {
            changeFocusOfInput(validatedNumber.getValue());
        }

        if (!mComponent.isCardNumberSupported(validatedNumber.getValue())) {
            mCardBrandLogoImageView.setStrokeWidth(0f);
            mCardBrandLogoImageView.setImageResource(R.drawable.ic_card);
        } else {
            mCardBrandLogoImageView.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH);
            mImageLoader.load(BcmcComponent.SUPPORTED_CARD_TYPE.getTxVariant(), mCardBrandLogoImageView);
        }
    }

    private boolean isOutputEmpty() {
        return mComponent.getOutputData().isEmpty();
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
}

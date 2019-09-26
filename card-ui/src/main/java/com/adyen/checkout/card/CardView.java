/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.card;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.adyen.checkout.base.ComponentView;
import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.base.ui.view.RoundCornerImageView;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.CardInputData;
import com.adyen.checkout.card.data.CardOutputData;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.card.ui.CardNumberInput;
import com.adyen.checkout.card.ui.ExpiryDateInput;
import com.adyen.checkout.card.ui.R;
import com.adyen.checkout.card.ui.SecurityCodeInput;

import java.util.List;

/**
 * CardView for {@link CardComponent}.
 */
@SuppressWarnings("SyntheticAccessor")
public final class CardView extends LinearLayout implements ComponentView<CardComponent>, Observer<CardOutputData> {

    private RoundCornerImageView mCardBrandLogoImageView;

    private final CardNumberInput mCardNumberEditText;
    private final ExpiryDateInput mExpiryDateEditText;

    private final TextInputLayout mExpiryDateInput;
    private final TextInputLayout mSecurityCodeInput;
    private final TextInputLayout mCardNumberInput;
    private final TextInputLayout mCardHolderInput;
    private final SwitchCompat mStorePaymentMethod;

    private final CardInputData mCardInputData;

    private ImageLoader mImageLoader;

    @Nullable
    private CardComponent mComponent;

    public CardView(@NonNull Context context) {
        this(context, null);
    }

    public CardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public CardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.card_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);

        mCardBrandLogoImageView = findViewById(R.id.cardBrandLogo_imageView);

        mStorePaymentMethod = findViewById(R.id.switch_storePaymentMethod);

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
                if (mComponent!=null && !mComponent.isStoredPaymentMethod()) {
                    mCardNumberInput.setErrorEnabled(!hasFocus);

                    if (!hasFocus && (isOutputEmpty() || !mComponent.getOutputData().getCardNumberField().isValid())) {
                        mCardNumberInput.setError(getContext().getString(R.string.checkout_card_number_not_valid));
                    }
                }
            }
        });

        mStorePaymentMethod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCardInputData.setStorePayment(isChecked);
                notifyInputDataChanged();
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

                if (!hasFocus && (isOutputEmpty() || (mComponent!=null && !mComponent.getOutputData().getExpiryDateField().isValid()))) {
                    mExpiryDateInput.setError(getContext().getString(R.string.checkout_expiry_date_not_valid));
                }
            }
        });

        mSecurityCodeInput = findViewById(R.id.textInputLayout_securityCode);
        final SecurityCodeInput securityCodeEditText = (SecurityCodeInput) mSecurityCodeInput.getEditText();
        securityCodeEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(Editable editable) {
                mCardInputData.setSecurityCode(editable.toString());
                notifyInputDataChanged();
            }
        });
        securityCodeEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mSecurityCodeInput.setErrorEnabled(!hasFocus);

                if (!hasFocus && (isOutputEmpty() || (mComponent!=null && !mComponent.getOutputData().getSecurityCodeField().isValid()))) {
                    mSecurityCodeInput.setError(getContext().getString(R.string.checkout_security_code_not_valid));
                }
            }
        });

        mCardHolderInput = findViewById(R.id.textInputLayout_cardHolder);
        final AdyenTextInputEditText cardHolderEditText = (AdyenTextInputEditText) mCardHolderInput.getEditText();
        cardHolderEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(Editable editable) {
                mCardInputData.setHolderName(editable.toString());
                notifyInputDataChanged();
            }
        });
        cardHolderEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mCardHolderInput.setErrorEnabled(!hasFocus);

                if (!hasFocus && (isOutputEmpty() || (mComponent!=null && !mComponent.getOutputData().getHolderNameField().isValid()))) {
                    mCardHolderInput.setError(getContext().getString(R.string.checkout_holder_name_not_valid));
                }
            }
        });

        mCardInputData = new CardInputData();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onChanged(@NonNull CardOutputData cardOutputData) {
        if (!cardOutputData.isEmpty()) {
            onCardNumberValidated(cardOutputData.getCardNumberField());
            onExpiryDateValidated(cardOutputData.getExpiryDateField());
        }

        if (mComponent != null && mComponent.isStoredPaymentMethod()) {
            mSecurityCodeInput.getEditText().requestFocus();
        }
    }

    @Override
    public void attach(@NonNull CardComponent component, @NonNull LifecycleOwner lifecycleOwner) {
        mComponent = component;

        if (mComponent.isStoredPaymentMethod()) {
            final CardInputData storedCardInput = mComponent.getStoredPaymentInputData();

            mCardNumberEditText.setText(
                    getContext().getString(R.string.card_number_4digit, storedCardInput.getCardNumber()));
            mCardNumberEditText.setEnabled(false);

            mExpiryDateEditText.setDate(storedCardInput.getExpiryDate());
            mExpiryDateEditText.setEnabled(false);

            mStorePaymentMethod.setVisibility(GONE);
            mCardHolderInput.setVisibility(GONE);
        } else {
            mCardHolderInput.setVisibility(mComponent.isHolderNameRequire() ? VISIBLE : GONE);
            mStorePaymentMethod.setVisibility(mComponent.showStorePaymentField() ? VISIBLE : GONE);
        }

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
        if (mComponent==null) {
            return;
        }
        final List<CardType> supportedCardType = mComponent.getSupportedFilterCards(validatedNumber.getValue());
        if (supportedCardType.isEmpty()) {
            mCardBrandLogoImageView.setStrokeWidth(0f);
            mCardBrandLogoImageView.setImageResource(R.drawable.ic_card);
        } else {
            mCardBrandLogoImageView.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH);
            mImageLoader.load(supportedCardType.get(0).getTxVariant(), mCardBrandLogoImageView);
        }

    }

    private void onExpiryDateValidated(@Nullable ValidatedField<ExpiryDate> validatedExpiryDate) {
        if (validatedExpiryDate != null && validatedExpiryDate.getValidation() == ValidatedField.Validation.VALID) {
            goToNextInputIfFocus(mExpiryDateEditText);
        }
    }

    private boolean isOutputEmpty() {
        if (mComponent==null) {
            return true;
        }
        return mComponent.getOutputData().isEmpty();
    }

    private void changeFocusOfInput(String numberValue) {
        final int length = numberValue.length();

        if (length == CardValidationUtils.GENERAL_CARD_NUMBER_LENGTH
                || length == CardValidationUtils.AMEX_CARD_NUMBER_LENGTH && CardType.estimate(numberValue).contains(CardType.AMERICAN_EXPRESS)) {
            goToNextInputIfFocus(mCardNumberEditText);
        }
    }

    private void goToNextInputIfFocus(View view) {
        if (getRootView().findFocus() == view) {
            findViewById(view.getNextFocusForwardId()).requestFocus();
        }
    }
}

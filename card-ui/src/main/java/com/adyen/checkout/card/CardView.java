/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.card;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.view.AdyenLinearLayout;
import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.base.ui.view.RoundCornerImageView;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.card.ui.CardNumberInput;
import com.adyen.checkout.card.ui.ExpiryDateInput;
import com.adyen.checkout.card.ui.R;
import com.adyen.checkout.card.ui.SecurityCodeInput;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * CardView for {@link CardComponent}.
 */
@SuppressWarnings("PMD.GodClass")
public final class CardView extends AdyenLinearLayout<CardOutputData, CardConfiguration, CardComponentState, CardComponent>
        implements Observer<CardOutputData> {

    private RoundCornerImageView mCardBrandLogoImageView;

    private CardNumberInput mCardNumberEditText;
    private ExpiryDateInput mExpiryDateEditText;

    private TextInputLayout mExpiryDateInput;
    private TextInputLayout mSecurityCodeInput;
    private TextInputLayout mCardNumberInput;
    private SwitchCompat mStorePaymentMethodSwitch;
    private TextInputLayout mCardHolderInput;

    private final CardInputData mCardInputData = new CardInputData();

    private ImageLoader mImageLoader;

    public CardView(@NonNull Context context) {
        this(context, null);
    }

    public CardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * View for CardComponent.
     */
    public CardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.card_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Prevent taking screenshot and screen on recents.
        final Activity activity = getActivity(getContext());
        if (activity != null) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final Activity activity = getActivity(getContext());
        if (activity != null) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public void initView() {
        initCardNumberInput();
        initExpiryDateInput();
        initSecurityCodeInput();
        initHolderNameInput();

        mCardBrandLogoImageView = findViewById(R.id.cardBrandLogo_imageView);

        mStorePaymentMethodSwitch = findViewById(R.id.switch_storePaymentMethod);
        mStorePaymentMethodSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCardInputData.setStorePayment(isChecked);
                notifyInputDataChanged();
            }
        });


        if (getComponent().isStoredPaymentMethod()) {
            setStoredCardInterface(getComponent().getStoredPaymentInputData());
        } else {
            mCardHolderInput.setVisibility(getComponent().isHolderNameRequire() ? VISIBLE : GONE);
            mStorePaymentMethodSwitch.setVisibility(getComponent().showStorePaymentField() ? VISIBLE : GONE);
        }

        notifyInputDataChanged();
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

        // Security Code
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_SecurityCodeInput, myAttrs);
        mSecurityCodeInput.setHint(typedArray.getString(0));
        typedArray.recycle();

        // Card Holder
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_HolderNameInput, myAttrs);
        mCardHolderInput.setHint(typedArray.getString(0));
        typedArray.recycle();

        // Store Switch
        myAttrs = new int[] {android.R.attr.text};
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_StorePaymentSwitch, myAttrs);
        mStorePaymentMethodSwitch.setText(typedArray.getString(0));
        typedArray.recycle();
    }

    @Override
    public void onComponentAttached() {
        mImageLoader = ImageLoader.getInstance(getContext(), getComponent().getConfiguration().getEnvironment());
    }

    @Override
    public void onChanged(@Nullable CardOutputData cardOutputData) {
        if (cardOutputData != null) {
            onCardNumberValidated(cardOutputData.getCardNumberField());
            onExpiryDateValidated(cardOutputData.getExpiryDateField());
            mSecurityCodeInput.setVisibility(cardOutputData.isCvcHidden() ? GONE : VISIBLE);
            if (cardOutputData.isCvcHidden()) {
                // We don't expect the hidden status to change back to visible, so we don't worry about putting the margin back.
                final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mExpiryDateInput.getLayoutParams();
                params.setMarginEnd(0);
                mExpiryDateInput.setLayoutParams(params);
            }
        }

        if (getComponent().isStoredPaymentMethod()) {
            mSecurityCodeInput.getEditText().requestFocus();
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

        final CardOutputData outputData;
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
                isErrorFocused = true;
                mExpiryDateInput.requestFocus();
            }
            mExpiryDateInput.setError(mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid));
        }

        if (!outputData.getSecurityCodeField().isValid()) {
            if (!isErrorFocused) {
                isErrorFocused = true;
                mSecurityCodeInput.requestFocus();
            }
            mSecurityCodeInput.setError(mLocalizedContext.getString(R.string.checkout_security_code_not_valid));
        }

        if (mCardHolderInput.getVisibility() == VISIBLE && !outputData.getHolderNameField().isValid()) {
            if (!isErrorFocused) {
                mCardHolderInput.requestFocus();
            }
            mCardHolderInput.setError(mLocalizedContext.getString(R.string.checkout_holder_name_not_valid));
        }
    }

    private void notifyInputDataChanged() {
        getComponent().inputDataChanged(mCardInputData);
    }

    private void onCardNumberValidated(@NonNull ValidatedField<String> validatedNumber) {
        if (validatedNumber.getValidation() == ValidatedField.Validation.VALID) {
            changeFocusOfInput(validatedNumber.getValue());
        }

        final List<CardType> supportedCardType = getComponent().getSupportedFilterCards();
        if (supportedCardType.isEmpty()) {
            mCardBrandLogoImageView.setStrokeWidth(0f);
            mCardBrandLogoImageView.setImageResource(R.drawable.ic_card);
            mCardNumberEditText.setAmexCardFormat(false);
        } else {
            mCardBrandLogoImageView.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH);
            mImageLoader.load(supportedCardType.get(0).getTxVariant(), mCardBrandLogoImageView);
            mCardNumberEditText.setAmexCardFormat(supportedCardType.contains(CardType.AMERICAN_EXPRESS));
        }
    }

    private void onExpiryDateValidated(@NonNull ValidatedField<ExpiryDate> validatedExpiryDate) {
        if (validatedExpiryDate.getValidation() == ValidatedField.Validation.VALID) {
            goToNextInputIfFocus(mExpiryDateEditText);
        }
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

    private void initCardNumberInput() {
        mCardNumberInput = findViewById(R.id.textInputLayout_cardNumber);
        mCardNumberEditText = (CardNumberInput) mCardNumberInput.getEditText();
        mCardNumberEditText.setOnChangeListener(editable -> {
            mCardInputData.setCardNumber(mCardNumberEditText.getRawValue());
            notifyInputDataChanged();
            mCardNumberInput.setError(null);
        });
        mCardNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!getComponent().isStoredPaymentMethod()) {
                    final CardOutputData outputData = getComponent().getOutputData();
                    if (hasFocus) {
                        mCardNumberInput.setError(null);
                    } else if (outputData != null && !outputData.getCardNumberField().isValid()) {
                        mCardNumberInput.setError(mLocalizedContext.getString(R.string.checkout_card_number_not_valid));
                    }
                }
            }
        });
    }

    private void initExpiryDateInput() {
        mExpiryDateInput = findViewById(R.id.textInputLayout_expiryDate);
        mExpiryDateEditText = (ExpiryDateInput) mExpiryDateInput.getEditText();
        mExpiryDateEditText.setOnChangeListener(editable -> {
            mCardInputData.setExpiryDate(mExpiryDateEditText.getDate());
            notifyInputDataChanged();
            mExpiryDateInput.setError(null);
        });
        mExpiryDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final CardOutputData outputData = getComponent().getOutputData();
            if (hasFocus) {
                mExpiryDateInput.setError(null);
            } else if (outputData != null && !outputData.getExpiryDateField().isValid()) {
                mExpiryDateInput.setError(mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid));
            }
        });
    }

    private void initSecurityCodeInput() {
        mSecurityCodeInput = findViewById(R.id.textInputLayout_securityCode);
        final SecurityCodeInput securityCodeEditText = (SecurityCodeInput) mSecurityCodeInput.getEditText();
        securityCodeEditText.setOnChangeListener(editable -> {
            mCardInputData.setSecurityCode(editable.toString());
            notifyInputDataChanged();
            mSecurityCodeInput.setError(null);
        });
        securityCodeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final CardOutputData outputData = getComponent().getOutputData();
            if (hasFocus) {
                mSecurityCodeInput.setError(null);
            } else if (outputData != null && !outputData.getSecurityCodeField().isValid()) {
                mSecurityCodeInput.setError(mLocalizedContext.getString(R.string.checkout_security_code_not_valid));
            }
        });
    }

    private void initHolderNameInput() {
        mCardHolderInput = findViewById(R.id.textInputLayout_cardHolder);
        final AdyenTextInputEditText cardHolderEditText = (AdyenTextInputEditText) mCardHolderInput.getEditText();
        cardHolderEditText.setOnChangeListener(editable -> {
            mCardInputData.setHolderName(editable.toString());
            notifyInputDataChanged();
            mCardHolderInput.setError(null);
        });
        cardHolderEditText.setOnFocusChangeListener((v, hasFocus) -> {
            final CardOutputData outputData = getComponent().getOutputData();
            if (hasFocus) {
                mCardHolderInput.setError(null);
            } else if (outputData != null && !outputData.getHolderNameField().isValid()) {
                mCardHolderInput.setError(mLocalizedContext.getString(R.string.checkout_holder_name_not_valid));
            }
        });
    }

    private void setStoredCardInterface(@NonNull CardInputData storedCardInput) {
        mCardNumberEditText.setText(mLocalizedContext.getString(R.string.card_number_4digit, storedCardInput.getCardNumber()));
        mCardNumberEditText.setEnabled(false);

        mExpiryDateEditText.setDate(storedCardInput.getExpiryDate());
        mExpiryDateEditText.setEnabled(false);

        mStorePaymentMethodSwitch.setVisibility(GONE);
        mCardHolderInput.setVisibility(GONE);
    }

    @Nullable
    private Activity getActivity(@NonNull final Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }

        if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }
}

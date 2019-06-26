/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.card;

import static com.adyen.checkout.card.data.validator.NumberValidator.AMEX_NUMBER_SIZE;
import static com.adyen.checkout.card.data.validator.NumberValidator.GENERAL_CARD_NUMBER_SIZE;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.adyen.checkout.base.ComponentView;
import com.adyen.checkout.base.component.validator.Validity;
import com.adyen.checkout.base.util.CustomTextWatcher;
import com.adyen.checkout.card.data.input.CardInputData;
import com.adyen.checkout.card.data.output.CardOutputData;
import com.adyen.checkout.card.data.output.ExpiryDateField;
import com.adyen.checkout.card.data.output.HolderNameField;
import com.adyen.checkout.card.data.output.NumberField;
import com.adyen.checkout.card.data.output.SecurityCodeField;
import com.adyen.checkout.card.model.CardType;
import com.adyen.checkout.card.ui.R;

/**
 * CardView for {@link CardComponent}.
 */
@SuppressWarnings("SyntheticAccessor")
public final class CardView extends LinearLayout implements ComponentView<CardComponent>, Observer<CardOutputData> {

    private RecyclerView mCardListRecyclerView;
    private CardListAdapter mCardListAdapter;

    private final EditText mCardNumberEditText;
    private final EditText mExpiryDateEditText;
    private final EditText mSecurityCodeEditText;
    private final EditText mCardHolderEditText;

    private final TextInputLayout mExpiryDateInput;
    private final TextInputLayout mSecurityCodeInput;
    private final TextInputLayout mCardNumberInput;
    private final TextInputLayout mCardHolderInput;

    private final CardInputData mCardInputData;

    @Nullable
    private CardComponent mComponent;

    public CardView(@NonNull Context context) {
        this(context, null);
    }

    public CardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * CardView constructor.
     *
     * @param context      {@link Context}
     * @param attrs        {@link AttributeSet}
     * @param defStyleAttr {@link Integer}
     */
    public CardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.card_view, this, true);


        mCardListRecyclerView = findViewById(R.id.recyclerView_cardList);
        mCardListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL,
                false));


        mCardNumberInput = findViewById(R.id.textInputLayout_cardNumber);
        mCardNumberEditText = mCardNumberInput.getEditText();
        mCardNumberEditText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChangedByUser(@NonNull Editable s) {
                mCardInputData.setCardNumber(s.toString());
                notifyInputDataChanged();
            }
        });
        mCardNumberEditText.setOnFocusChangeListener((v, hasFocus) -> {
            mCardNumberInput.setErrorEnabled(!hasFocus);

            if (!hasFocus && !mComponent.getOutputData().getNumber().getValidationResult().isValid()) {
                mCardNumberInput.setError(getContext().getString(R.string.checkout_card_number_not_valid));
            }
        });

        mExpiryDateInput = findViewById(R.id.textInputLayout_expiryDate);
        mExpiryDateEditText = mExpiryDateInput.getEditText();
        mExpiryDateEditText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChangedByUser(@NonNull Editable s) {
                mCardInputData.setExpiryDate(s.toString());
                notifyInputDataChanged();
            }
        });
        mExpiryDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            mExpiryDateInput.setErrorEnabled(!hasFocus);

            if (!hasFocus && !mComponent.getOutputData().getExpiryDate().getValidationResult().isValid()) {
                mExpiryDateInput.setError(getContext().getString(R.string.checkout_expiry_date_not_valid));
            }
        });

        mSecurityCodeInput = findViewById(R.id.textInputLayout_securityCode);
        mSecurityCodeEditText = mSecurityCodeInput.getEditText();
        mSecurityCodeEditText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChangedByUser(@NonNull Editable s) {
                mCardInputData.setSecurityCode(s.toString());
                notifyInputDataChanged();
            }
        });
        mSecurityCodeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            mSecurityCodeInput.setErrorEnabled(!hasFocus);

            if (!hasFocus && !mComponent.getOutputData().getSecurityCode().getValidationResult().isValid()) {
                mSecurityCodeInput.setError(getContext().getString(R.string.checkout_security_code_not_valid));
            }
        });

        mCardHolderInput = findViewById(R.id.textInputLayout_cardHolder);
        mCardHolderEditText = mCardHolderInput.getEditText();
        mCardHolderEditText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChangedByUser(@NonNull Editable s) {
                mCardInputData.setHolderName(s.toString());
                notifyInputDataChanged();
            }
        });
        mCardHolderEditText.setOnFocusChangeListener((v, hasFocus) -> {
            mCardHolderInput.setErrorEnabled(!hasFocus);

            if (!hasFocus && !mComponent.getOutputData().getHolderNameField().getValidationResult().isValid()) {
                mCardHolderInput.setError(getContext().getString(R.string.checkout_holder_name_not_valid));
            }
        });

        mCardInputData = new CardInputData();
    }

    @Override
    public void onChanged(@NonNull CardOutputData cardOutputData) {
        updateNumber(cardOutputData.getNumber());
        updateExpiryDate(cardOutputData.getExpiryDate());
        updateSecurityCode(cardOutputData.getSecurityCode());
        updateCardHolder(cardOutputData.getHolderNameField());
    }

    private void notifyInputDataChanged() {
        if (mComponent != null) {
            post(() -> mComponent.inputDataChanged(mCardInputData));
        }
    }

    private void updateNumber(@Nullable NumberField number) {
        if (number != null) {
            final String displayValue = number.getDisplayValue();
            mCardNumberEditText.setText(displayValue);
            mCardNumberEditText.setSelection(displayValue.length());
            if (number.getValidationResult().getValidity() == Validity.VALID) {
                changeFocusOfInput(number);
            }

            mCardListAdapter.setCards(mComponent.getSupportedFilterCards(displayValue));
        }
    }

    private void updateCardHolder(@Nullable HolderNameField holderNameField) {
        if (holderNameField != null) {
            final String displayValue = holderNameField.getDisplayValue();
            mCardHolderEditText.setText(displayValue);
            mCardHolderEditText.setSelection(displayValue.length());
        }
    }

    private void changeFocusOfInput(NumberField number) {
        final String numberValue = number.getValue();
        final int length = numberValue.length();

        if (length == GENERAL_CARD_NUMBER_SIZE || length == AMEX_NUMBER_SIZE && CardType.estimate(numberValue).contains(
                CardType.AMERICAN_EXPRESS)) {
            goToNextInputIfFocus(mCardNumberEditText);
        }
    }

    private void updateExpiryDate(@Nullable ExpiryDateField expiryDate) {
        if (expiryDate != null) {
            final String displayValue = expiryDate.getDisplayValue();
            mExpiryDateEditText.setText(displayValue);
            mExpiryDateEditText.setSelection(displayValue.length());
            if (expiryDate.getValidationResult().getValidity() == Validity.VALID) {
                goToNextInputIfFocus(mExpiryDateEditText);
            }
        }
    }

    private void updateSecurityCode(@Nullable SecurityCodeField securityCode) {
        if (securityCode != null) {
            final String displayValue = securityCode.getDisplayValue();
            mSecurityCodeEditText.setText(displayValue);
            mSecurityCodeEditText.setSelection(displayValue.length());
        }
    }

    @Override
    public void attach(@NonNull CardComponent component, @NonNull LifecycleOwner lifecycleOwner) {
        mComponent = component;
        mComponent.observeOutputData(lifecycleOwner, this);
        mComponent.getCardLogoImages().observe(lifecycleOwner, stringDrawableHashMap -> {
            mCardListAdapter.setCardLogos(stringDrawableHashMap);
        });

        mCardHolderInput.setVisibility(mComponent.isHolderNameRequire() ? VISIBLE : GONE);

        mCardListAdapter = new CardListAdapter(mComponent);
        mCardListAdapter.setCards(mComponent.getSupportedFilterCards(null));
        mCardListRecyclerView.setAdapter(mCardListAdapter);

        mComponent.sendAnalyticsEvent(getContext());
    }

    private void goToNextInputIfFocus(View view) {
        if (getRootView().findFocus() == view) {
            findViewById(view.getNextFocusForwardId()).requestFocus();
        }
    }

    @Override
    public boolean isConfirmationRequired() {
        return true;
    }
}

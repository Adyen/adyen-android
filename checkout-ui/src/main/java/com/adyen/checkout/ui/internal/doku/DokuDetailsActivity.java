package com.adyen.checkout.ui.internal.doku;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.DokuDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.common.util.TextViewUtil;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

import java.util.regex.Matcher;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 */
public class DokuDetailsActivity extends CheckoutDetailsActivity implements View.OnClickListener {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private PaymentMethod mPaymentMethod;

    private Button mPayButton;

    private EditText mShopperEmailEditText;

    private EditText mLastNameEditText;

    private EditText mFirstNameEditText;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, DokuDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    public void onClick(View v) {
        if (v == mPayButton && validate()) {
            String shopperEmail = mShopperEmailEditText.getText().toString().trim();
            String lastName = mLastNameEditText.getText().toString().trim();
            String firstName = mFirstNameEditText.getText().toString().trim();

            DokuDetails dokuDetails = new DokuDetails.Builder(shopperEmail, firstName, lastName).build();
            getPaymentHandler().initiatePayment(mPaymentMethod, dokuDetails);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);

        setContentView(R.layout.activity_doku_details);
        setTitle(mPaymentMethod.getName());
        ValidationListener validationListener = new ValidationListener();

        mShopperEmailEditText = findViewById(R.id.editText_shopperEmail);
        mShopperEmailEditText.addTextChangedListener(validationListener);
        mShopperEmailEditText.setOnFocusChangeListener(validationListener);

        mLastNameEditText = findViewById(R.id.editText_lastName);
        mLastNameEditText.addTextChangedListener(validationListener);
        mLastNameEditText.setOnFocusChangeListener(validationListener);

        mFirstNameEditText = findViewById(R.id.editText_firstName);
        mFirstNameEditText.addTextChangedListener(validationListener);
        mFirstNameEditText.setOnFocusChangeListener(validationListener);

        mPayButton = findViewById(R.id.button_continue);
        PayButtonUtil.setPayButtonText(this, mPayButton);
        mPayButton.setOnClickListener(this);

        validate();

        if (savedInstanceState == null) {
            KeyboardUtil.show(mShopperEmailEditText);
        }
    }

    private boolean validateEmail() {
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(mShopperEmailEditText.getText().toString().trim());
        boolean isValid = matcher.matches();
        updateTextColor(mShopperEmailEditText, isValid || (mShopperEmailEditText.hasFocus() && matcher.hitEnd()));

        return isValid;
    }

    private boolean validateFirstName() {
        boolean isValid = !mLastNameEditText.getText().toString().trim().isEmpty();
        updateTextColor(mLastNameEditText, isValid);

        return isValid;
    }

    private boolean validateLastName() {
        boolean isValid = !mFirstNameEditText.getText().toString().trim().isEmpty();
        updateTextColor(mFirstNameEditText, isValid);

        return isValid;
    }

    private boolean validate() {
        boolean isValid = validateEmail() & validateFirstName() & validateLastName();
        mPayButton.setEnabled(isValid);

        return isValid;
    }

    private void updateTextColor(@NonNull TextView textView, boolean showAsValid) {
        if (showAsValid) {
            TextViewUtil.setDefaultTextColor(textView);
        } else {
            TextViewUtil.setErrorTextColor(textView);
        }
    }

    private final class ValidationListener extends SimpleTextWatcher implements View.OnFocusChangeListener {
        @Override
        public void afterTextChanged(Editable s) {
            validate();
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            validate();
        }
    }
}

/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 02/05/2018.
 */

package com.adyen.checkout.ui.internal.card;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.adyen.checkout.core.AdditionalDetails;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.CupSecurePlusDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

public class CupSecurePlusDetailsActivity extends CheckoutDetailsActivity {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private static final String EXTRA_ADDITIONAL_DETAILS = "EXTRA_ADDITIONAL_DETAILS";

    private static final int EXPECTED_SMS_CODE_SIZE = 6;

    private TextView mSmsCodePromptTextView;

    private EditText mSmsCodeEditText;

    private Button mPayButton;

    private TextView mSurchargeTextView;

    private PaymentMethod mPaymentMethod;

    private AdditionalDetails mAdditionalDetails;

    @NonNull
    public static Intent newIntent(
            @NonNull Context context,
            @NonNull PaymentReference paymentReference,
            @NonNull PaymentMethod paymentMethod,
            @NonNull AdditionalDetails additionalDetails
    ) {
        Intent intent = new Intent(context, CupSecurePlusDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);
        intent.putExtra(EXTRA_ADDITIONAL_DETAILS, additionalDetails);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mPaymentMethod = intent.getParcelableExtra(EXTRA_PAYMENT_METHOD);
        mAdditionalDetails = intent.getParcelableExtra(EXTRA_ADDITIONAL_DETAILS);

        setContentView(R.layout.activity_cup_secure_plus_details);

        mSmsCodePromptTextView = findViewById(R.id.textView_smsCodePrompt);

        mSmsCodeEditText = findViewById(R.id.editText_smsCode);
        mSmsCodeEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updatePayButton();
            }
        });

        mPayButton = findViewById(R.id.button_pay);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String smsCode = getTrimmedSmsCode();
                CupSecurePlusDetails cupSecurePlusDetails = new CupSecurePlusDetails.Builder(smsCode).build();
                getPaymentHandler().submitAdditionalDetails(cupSecurePlusDetails);
            }
        });

        mSurchargeTextView = findViewById(R.id.textView_surcharge);

        PayButtonUtil.setPayButtonText(this, mPaymentMethod, mPayButton, mSurchargeTextView);

        updatePayButton();
    }

    private void updatePayButton() {
        mPayButton.setEnabled(getTrimmedSmsCode().length() == EXPECTED_SMS_CODE_SIZE);
    }

    @NonNull
    private String getTrimmedSmsCode() {
        return mSmsCodeEditText.getText().toString().trim();
    }
}

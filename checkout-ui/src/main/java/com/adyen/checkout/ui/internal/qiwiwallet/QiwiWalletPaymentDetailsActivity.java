/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 16/11/2017.
 */

package com.adyen.checkout.ui.internal.qiwiwallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.QiwiWalletDetails;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.Adapter;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;

import java.util.ArrayList;
import java.util.List;

public class QiwiWalletPaymentDetailsActivity extends CheckoutDetailsActivity {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private PaymentMethod mPaymentMethod;

    private Spinner mPhoneNumberPrefixSpinner;

    private EditText mPhoneNumberEditText;

    private Button mPayButton;

    private TextView mSurchargeTextView;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, QiwiWalletPaymentDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);

        setContentView(R.layout.activity_qiwi_wallet_details);
        setTitle(mPaymentMethod.getName());
        mPhoneNumberPrefixSpinner = findViewById(R.id.spinner_phoneNumberPrefix);
        Adapter<Item> adapter = Adapter.forSpinner(new Adapter.TextDelegate<Item>() {
            @NonNull
            @Override
            public String getText(@NonNull Item item) {
                return getString(R.string.checkout_qiwiwallet_country_code_format, item.getId(), item.getName());
            }
        });
        adapter.setItems(getItems(mPaymentMethod));
        mPhoneNumberPrefixSpinner.setAdapter(adapter);
        mPhoneNumberEditText = findViewById(R.id.editText_phoneNumber);
        mPayButton = findViewById(R.id.button_continue);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telephoneNumberPrefix = ((Item) mPhoneNumberPrefixSpinner.getSelectedItem()).getId();
                String telephoneNumber = mPhoneNumberEditText.getText().toString().trim();
                QiwiWalletDetails qiwiWalletDetails = new QiwiWalletDetails.Builder(telephoneNumberPrefix, telephoneNumber).build();
                getPaymentHandler().initiatePayment(mPaymentMethod, qiwiWalletDetails);
            }
        });

        mSurchargeTextView = findViewById(R.id.textView_surcharge);

        PayButtonUtil.setPayButtonText(this, mPaymentMethod, mPayButton, mSurchargeTextView);
    }

    @NonNull
    private List<Item> getItems(@NonNull PaymentMethod paymentMethod) {
        List<InputDetail> inputDetails = paymentMethod.getInputDetails();

        if (inputDetails != null) {
            for (InputDetail inputDetail : inputDetails) {
                List<Item> items = inputDetail.getItems();

                if (items != null && QiwiWalletDetails.KEY_TELEPHONE_NUMBER_PREFIX.equals(inputDetail.getKey())) {
                    return items;
                }
            }
        }

        return new ArrayList<>();
    }
}

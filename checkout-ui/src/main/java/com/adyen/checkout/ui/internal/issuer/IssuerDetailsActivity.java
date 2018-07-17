package com.adyen.checkout.ui.internal.issuer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.IssuerDetails;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.ui.internal.common.util.recyclerview.HeaderItemDecoration;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 */
public class IssuerDetailsActivity extends CheckoutDetailsActivity implements IssuersAdapter.Listener {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private PaymentMethod mPaymentMethod;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, IssuerDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    public void onIssuerClick(@NonNull PaymentMethod paymentMethod, @NonNull Item item) {
        IssuerDetails issuerDetails = new IssuerDetails.Builder(item.getId()).build();
        getPaymentHandler().initiatePayment(paymentMethod, issuerDetails);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);

        setContentView(R.layout.activity_issuer_details);
        setTitle(mPaymentMethod.getName());
        RecyclerView issuersRecyclerView = findViewById(R.id.recyclerView_issuers);
        issuersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        issuersRecyclerView.addItemDecoration(new HeaderItemDecoration(createHeaderTextView(), new HeaderItemDecoration.Callback() {
            @Override
            public boolean isHeaderPosition(int position) {
                return position == 0;
            }
        }));
        issuersRecyclerView.setAdapter(new IssuersAdapter(this, mPaymentMethod, getLogoApi(), this));
    }

    @NonNull
    private TextView createHeaderTextView() {
        int padding = getResources().getDimensionPixelSize(R.dimen.standard_margin);

        TextView headerTextView = new AppCompatTextView(this);
        headerTextView.setPadding(padding, padding, padding, padding);
        headerTextView.setTextColor(ThemeUtil.getPrimaryThemeColor(this));
        headerTextView.setText(R.string.checkout_issuer_choose_issuer);

        return headerTextView;
    }
}

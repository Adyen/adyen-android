/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 21/03/2018.
 */

package com.adyen.checkout.ui.internal.common.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.base.internal.Objects;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.common.model.CheckoutSessionProvider;

public abstract class CheckoutSessionFragment extends Fragment implements CheckoutSessionProvider {
    @NonNull
    protected static final String ARG_PAYMENT_REFERENCE = "ARG_PAYMENT_REFERENCE";

    private PaymentReference mPaymentReference;

    private PaymentHandler mPaymentHandler;

    private PaymentSession mPaymentSession;

    @NonNull
    @Override
    public PaymentReference getPaymentReference() {
        if (mPaymentReference == null) {
            Bundle arguments = Objects.requireNonNull(getArguments(), "Arguments Bundle is null.");
            mPaymentReference = Objects
                    .requireNonNull(arguments.<PaymentReference>getParcelable(ARG_PAYMENT_REFERENCE), "PaymentReference is null.");
        }

        return mPaymentReference;
    }

    @NonNull
    @Override
    public PaymentHandler getPaymentHandler() {
        if (mPaymentHandler == null) {
            mPaymentHandler = getPaymentReference().getPaymentHandler(getActivity());
        }

        return mPaymentHandler;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPaymentHandler().getPaymentSessionObservable().observe(getActivity(), new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                mPaymentSession = paymentSession;
            }
        });
    }

    @NonNull
    protected LogoApi getLogoApi() {
        return getPaymentHandler().getLogoApi();
    }

    @Nullable
    protected PaymentSession getPaymentSession() {
        return mPaymentSession;
    }
}

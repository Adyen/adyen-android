/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 06/04/2018.
 */

package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.core.model.SurchargeConfiguration;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.model.CheckoutSessionProvider;
import com.adyen.checkout.util.AmountFormat;
import com.adyen.checkout.util.internal.TextFormat;

public final class PayButtonUtil {
    public static <T extends AppCompatActivity & CheckoutSessionProvider> void setPayButtonText(
            @NonNull final T checkoutSessionActivity,
            @NonNull final PaymentMethod paymentMethod,
            @NonNull final TextView payButton,
            @NonNull final TextView surchargeTextView
    ) {
        Observable<PaymentSession> paymentSessionObservable = checkoutSessionActivity.getPaymentHandler().getPaymentSessionObservable();
        paymentSessionObservable.observe(checkoutSessionActivity, new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                setPayButtonText(paymentSession, paymentMethod, payButton, surchargeTextView);
            }
        });
    }

    public static <T extends Fragment & CheckoutSessionProvider> void setPayButtonText(
            @NonNull final T checkoutSessionFragment,
            @NonNull final PaymentMethod paymentMethod,
            @NonNull final TextView payButton,
            @NonNull final TextView surchargeTextView
    ) {
        FragmentActivity activity = checkoutSessionFragment.getActivity();

        if (activity == null) {
            return;
        }

        Observable<PaymentSession> paymentSessionObservable = checkoutSessionFragment.getPaymentHandler().getPaymentSessionObservable();
        paymentSessionObservable.observe(activity, new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                Context context = checkoutSessionFragment.getContext();

                if (context != null) {
                    setPayButtonText(paymentSession, paymentMethod, payButton, surchargeTextView);
                }
            }
        });
    }

    private static void setPayButtonText(
            @NonNull PaymentSession paymentSession,
            @NonNull PaymentMethod paymentMethod,
            @NonNull TextView payButton,
            @NonNull TextView surchargeTextView
    ) {
        Context context = payButton.getContext();

        CharSequence transactionAmount;
        CharSequence surchargeAmount;

        try {
            final SurchargeConfiguration surchargeConfiguration = paymentMethod.getConfiguration(SurchargeConfiguration.class);
            String currencyCode = surchargeConfiguration.getSurchargeCurrencyCode();
            transactionAmount = AmountFormat.format(context, surchargeConfiguration.getSurchargeFinalAmount(), currencyCode);
            surchargeAmount = AmountFormat.format(context, surchargeConfiguration.getSurchargeTotalCost(), currencyCode);
        } catch (CheckoutException e) {
            transactionAmount = AmountFormat.format(context, paymentSession.getPayment().getAmount());
            surchargeAmount = null;
        }

        payButton.setText(TextFormat.format(context, R.string.checkout_pay_amount_format, transactionAmount));

        if (surchargeAmount != null) {
            surchargeTextView.setVisibility(View.VISIBLE);
            surchargeTextView.setText(TextFormat.format(context, R.string.checkout_surcharge_total_amount_format, surchargeAmount));
        } else {
            surchargeTextView.setVisibility(View.GONE);
            surchargeTextView.setText(null);
        }
    }

    private PayButtonUtil() {
        throw new IllegalStateException("No instances.");
    }
}

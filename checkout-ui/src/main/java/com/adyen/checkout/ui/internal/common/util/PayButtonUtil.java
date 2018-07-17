package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.model.Amount;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.model.CheckoutSessionProvider;
import com.adyen.checkout.util.AmountFormat;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 06/04/2018.
 */
public final class PayButtonUtil {
    public static <T extends AppCompatActivity & CheckoutSessionProvider> void setPayButtonText(
            @NonNull final T checkoutSessionActivity,
            @NonNull final TextView textView
    ) {
        Observable<PaymentSession> paymentSessionObservable = checkoutSessionActivity.getPaymentHandler().getPaymentSessionObservable();
        paymentSessionObservable.observe(checkoutSessionActivity, new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                Amount amount = paymentSession.getPayment().getAmount();
                CharSequence payButtonText = AmountFormat
                        .getStringWithFormattedAmounts(checkoutSessionActivity, R.string.checkout_pay_amount_format, amount);
                textView.setText(payButtonText);
            }
        });
    }

    public static <T extends Fragment & CheckoutSessionProvider> void setPayButtonText(
            @NonNull final T checkoutSessionFragment,
            @NonNull final TextView textView
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
                    Amount amount = paymentSession.getPayment().getAmount();
                    CharSequence payButtonText = AmountFormat.getStringWithFormattedAmounts(context, R.string.checkout_pay_amount_format, amount);
                    textView.setText(payButtonText);
                }
            }
        });
    }

    private PayButtonUtil() {
        throw new IllegalStateException("No instances.");
    }
}

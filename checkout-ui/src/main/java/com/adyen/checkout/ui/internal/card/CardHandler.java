/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 07/06/2018.
 */

package com.adyen.checkout.ui.internal.card;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.CardDetails;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.util.PaymentMethodTypes;

import java.util.List;

public final class CardHandler implements PaymentMethodHandler {
    @NonNull
    public static final Factory FACTORY = new Factory() {
        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            if (PaymentMethodTypes.CARD.equals(paymentMethod.getType())) {
                return true;
            }

            List<InputDetail> inputDetails = paymentMethod.getInputDetails();

            if (inputDetails == null) {
                return false;
            }

            InputDetail cardNumberDetail = InputDetailImpl.findByKey(inputDetails, CardDetails.KEY_ENCRYPTED_CARD_NUMBER);
            InputDetail expiryMonthDetail = InputDetailImpl.findByKey(inputDetails, CardDetails.KEY_ENCRYPTED_EXPIRY_MONTH);
            InputDetail expiryYearInputDetail = InputDetailImpl.findByKey(inputDetails, CardDetails.KEY_ENCRYPTED_EXPIRY_YEAR);

            if (cardNumberDetail == null || expiryMonthDetail == null || expiryYearInputDetail == null) {
                return false;
            }

            if (!inputDetails.remove(cardNumberDetail) || !inputDetails.remove(expiryMonthDetail) || !inputDetails.remove(expiryYearInputDetail)) {
                return false;
            }

            for (InputDetail inputDetail : inputDetails) {
                String key = inputDetail.getKey();

                if (!CardDetails.KEY_ENCRYPTED_SECURITY_CODE.equals(key)
                        && !CardDetails.KEY_HOLDER_NAME.equals(key)
                        && !CardDetails.KEY_PHONE_NUMBER.equals(key)
                        && !CardDetails.KEY_INSTALLMENTS.equals(key)
                        && !CardDetails.KEY_STORE_DETAILS.equals(key)) {
                    if (!inputDetail.isOptional()) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public boolean isAvailableToShopper(
                @NonNull Application application,
                @NonNull PaymentSession paymentSession,
                @NonNull PaymentMethod paymentMethod
        ) {
            if (paymentSession.getPublicKey() != null) {
                return true;
            } else {
                final Context context = application;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, ERROR_MESSAGE_PUBLIC_KEY_NULL, Toast.LENGTH_LONG).show();
                    }
                });

                return false;
            }
        }
    };

    static final String ERROR_MESSAGE_PUBLIC_KEY_NULL = "Public key for card payments has not been generated.";

    private final PaymentReference mPaymentReference;

    private final PaymentMethod mPaymentMethod;

    public CardHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        activity.finishActivity(requestCode);
        Intent intent = CardDetailsActivity.newIntent(activity, mPaymentReference, mPaymentMethod);
        activity.startActivityForResult(intent, requestCode);
    }
}

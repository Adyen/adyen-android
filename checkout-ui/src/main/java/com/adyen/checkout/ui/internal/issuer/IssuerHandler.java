/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/06/2018.
 */

package com.adyen.checkout.ui.internal.issuer;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.IssuerDetails;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;

import java.util.List;

public final class IssuerHandler implements PaymentMethodHandler {
    @NonNull
    public static final Factory FACTORY = new Factory() {
        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            List<InputDetail> inputDetails = paymentMethod.getInputDetails();

            if (inputDetails == null) {
                return false;
            }

            InputDetail issuerDetail = InputDetailImpl.findByKey(inputDetails, IssuerDetails.KEY_ISSUER);

            if (issuerDetail == null || !inputDetails.remove(issuerDetail)) {
                return false;
            }

            List<Item> items = issuerDetail.getItems();

            if (items == null || items.isEmpty()) {
                return false;
            }

            for (InputDetail inputDetail : inputDetails) {
                if (!inputDetail.isOptional()) {
                    return false;
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
            return true;
        }
    };

    private final PaymentReference mPaymentReference;

    private final PaymentMethod mPaymentMethod;

    public IssuerHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        activity.finishActivity(requestCode);
        Intent intent = IssuerDetailsActivity.newIntent(activity, mPaymentReference, mPaymentMethod);
        activity.startActivityForResult(intent, requestCode);
    }
}

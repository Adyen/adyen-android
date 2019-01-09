/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 16/03/2018.
 */

package com.adyen.checkout.ui.internal.card;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.model.Card;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.core.model.StoredDetails;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

public class CardCheckoutMethodFactory extends CheckoutMethodFactory {
    public CardCheckoutMethodFactory(@NonNull Application application) {
        super(application);
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initOneClickCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final List<CheckoutMethod> checkoutMethods = new ArrayList<>();
        List<PaymentMethod> oneClickPaymentMethods = paymentSession.getOneClickPaymentMethods();

        if (oneClickPaymentMethods != null) {
            Application application = getApplication();

            for (PaymentMethod paymentMethod : oneClickPaymentMethods) {
                if (!CardHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
                    continue;
                }

                StoredDetails storedDetails = paymentMethod.getStoredDetails();
                Card card = storedDetails != null ? storedDetails.getCard() : null;

                if (card != null) {
                    checkoutMethods.add(new CardCheckoutMethod.OneClick(application, paymentMethod, card));
                }
            }
        }

        return new Callable<List<CheckoutMethod>>() {
            @Override
            public List<CheckoutMethod> call() {
                return checkoutMethods;
            }
        };
    }

    @NonNull
    @Override
    public Callable<List<CheckoutMethod>> initCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final List<CheckoutMethod> checkoutMethods = new ArrayList<>();

        Set<PaymentMethod> paymentMethods = new TreeSet<>(new Comparator<PaymentMethod>() {
            @Override
            public int compare(PaymentMethod o1, PaymentMethod o2) {
                if (o1.getType().equals(o2.getType())) {
                    // Payment Methods with the same type are considered equal.
                    return 0;
                } else {
                    // Otherwise keep the ordering as is.
                    return -1;
                }
            }
        });
        Application application = getApplication();

        for (PaymentMethod paymentMethod : paymentSession.getPaymentMethods()) {
            PaymentMethod group = paymentMethod.getGroup();

            if (group != null && CardHandler.FACTORY.supports(application, group)) {
                if (CardHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
                    paymentMethods.add(group);
                }
            } else if (CardHandler.FACTORY.supports(application, paymentMethod)) {
                if (CardHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
                    paymentMethods.add(paymentMethod);
                }
            }
        }

        for (PaymentMethod paymentMethod : paymentMethods) {
            checkoutMethods.add(new CardCheckoutMethod.Default(application, paymentMethod));
        }

        return new Callable<List<CheckoutMethod>>() {
            @Override
            public List<CheckoutMethod> call() {
                return checkoutMethods;
            }
        };
    }
}

package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

/**
 * A {@link PaymentSession} holds all relevant information that is needed to make a payment.
 * <p>
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 04/08/2017.
 */
public interface PaymentSession {
    /**
     * @return The {@link Payment} object holding information about the payment.
     */
    @NonNull
    Payment getPayment();

    /**
     * @return A {@link List} of available {@link PaymentMethod PaymentMethods}.
     */
    @NonNull
    List<PaymentMethod> getPaymentMethods();

    /**
     * @return A {@link List} of available one-click {@link PaymentMethod PaymentMethods}, i.e. methods that have been used by the shopper before
     * and for which details were stored.
     */
    @Nullable
    List<PaymentMethod> getOneClickPaymentMethods();

    /**
     * @return The public key to encrypt card data with.
     */
    @Nullable
    String getPublicKey();

    /**
     * @return The generation {@link Date} used for card data encryption.
     */
    @NonNull
    Date getGenerationTime();
}

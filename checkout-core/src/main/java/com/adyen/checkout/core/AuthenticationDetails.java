/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 09/05/2018.
 */

package com.adyen.checkout.core;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.Authentication;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentResultCode;

import java.util.List;

/**
 * The {@link AuthenticationDetails} class describes all required parameters for an authentication.
 */
public interface AuthenticationDetails extends Parcelable {
    /**
     * @return The type of payment method for which authentication details are needed.
     */
    @NonNull
    String getPaymentMethodType();

    /**
     * Get authentication data that might be needed for the shopper authentication.
     *
     * @param authenticationClass The {@link Authentication} {@link Class}.
     * @param <T> The {@link Authentication} type.
     * @return The parsed {@link Authentication}.
     * @throws CheckoutException If the data does not match the provided {@link Authentication} {@link Class}.
     */
    @NonNull
    <T extends Authentication> T getAuthentication(@NonNull Class<T> authenticationClass) throws CheckoutException;

    /**
     * @return The {@link List} of authentication {@link InputDetail InputDetails}.
     */
    @NonNull
    List<InputDetail>  getInputDetails();

    /**
     * @return The payment result code {@link PaymentResultCode}.
     */
    @NonNull
    PaymentResultCode getResultCode();
}

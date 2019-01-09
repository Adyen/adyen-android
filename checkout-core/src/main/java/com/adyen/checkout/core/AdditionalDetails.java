/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/05/2018.
 */

package com.adyen.checkout.core;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.RedirectData;

import java.util.List;

/**
 * Interface providing information about additional details that need to be provided to continue with the payment after it has been initialized
 * with {@link PaymentHandler#initiatePayment(com.adyen.checkout.core.model.PaymentMethod, com.adyen.checkout.core.model.PaymentMethodDetails)}.
 */
public interface AdditionalDetails extends Parcelable {
    /**
     * @return The type of payment method for which additional details are needed.
     */
    @NonNull
    String getPaymentMethodType();

    /**
     * @return The {@link List} of additional {@link InputDetail InputDetails}.
     */
    @NonNull
    List<InputDetail> getInputDetails();

    /**
     * Get additional data that might be needed for a redirect.
     *
     * @param redirectDataClass The {@link RedirectData} {@link Class}.
     * @param <T> The {@link RedirectData} type.
     * @return The parsed {@link RedirectData}.
     * @throws CheckoutException If the data does not match the provided {@link RedirectData} {@link Class}.
     */
    @NonNull
    <T extends RedirectData> T getRedirectData(@NonNull Class<T> redirectDataClass) throws CheckoutException;
}

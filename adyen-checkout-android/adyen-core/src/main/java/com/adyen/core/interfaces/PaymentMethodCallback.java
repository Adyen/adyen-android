package com.adyen.core.interfaces;

import android.support.annotation.NonNull;

import com.adyen.core.models.PaymentMethod;

import java.io.Serializable;

/**
 * The interface for notifying {@link com.adyen.core.PaymentRequest} when a payment method is selected.
 */
public interface PaymentMethodCallback extends Serializable {

    /**
     * The callback which is called when the payment method is selected.
     * @param paymentMethod {@link PaymentMethod} payment method which is selected by the shopper
     *                                           from the payment methods list.
     */
    void completionWithPaymentMethod(@NonNull PaymentMethod paymentMethod);
}

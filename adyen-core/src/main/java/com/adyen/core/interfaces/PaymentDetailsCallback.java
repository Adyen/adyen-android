package com.adyen.core.interfaces;

import android.support.annotation.NonNull;

import com.adyen.core.models.paymentdetails.PaymentDetails;

import java.util.Map;

/**
 * The interface for notifying {@link com.adyen.core.PaymentRequest} with payment details.
 */
public interface PaymentDetailsCallback {

    /**
     * The callback which is called when payment details are provided by the client.
     * @param paymentDetails payment details which are provided by the client.
     */
    void completionWithPaymentDetails(@NonNull Map<String, Object> paymentDetails);

    /**
     * The callback which is called when payment details are provided by the client.
     * @param paymentDetails payment details which are provided by the client.
     */
    void completionWithPaymentDetails(PaymentDetails paymentDetails);
}

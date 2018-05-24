package com.adyen.core.interfaces;

import android.support.annotation.NonNull;

/**
 * Callback to be used for providing payment initialization data.
 */

public interface PaymentDataCallback {
    /**
     * The callback for notifying {@link com.adyen.core.PaymentRequest} when payment data is provided.
     * @param paymentData Raw data for initialization in the byte array format.
     */
    void completionWithPaymentData(@NonNull byte[] paymentData);
}

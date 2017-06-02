package com.adyen.core.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The result class for {@link com.adyen.core.PaymentRequest}.
 */

public class PaymentRequestResult {

    private Throwable error;
    private Payment payment;

    /**
     * The constructor for the case where the payment request has failed unexpectedly.
     * @param error
     */
    public PaymentRequestResult(Throwable error) {
        this.error = error;
    }

    /**
     * The constructor for the case where the payment request has been processed on the Adyen backend.
     * @param payment
     */
    public PaymentRequestResult(@NonNull final Payment payment) {
        this.payment = payment;
    }

    /**
     * Check if the payment was successfully processed.
     * This does not mean that the payment was authorised.
     * @return true if the payment was successfully processed on the Adyen backend, otherwise false.
     */
    public boolean isProcessed() {
        return error == null;
    }

    /**
     * Get the payment.
     * @return The payment if it exists, null if an error occurred.
     */
    public @Nullable Payment getPayment() {
        return payment;
    }

    /**
     * Get the error.
     * @return An error if something went wrong when processing the payment, null if no error is known.
     */
    public @Nullable Throwable getError() {
        return error;
    }

}

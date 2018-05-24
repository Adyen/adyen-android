package com.adyen.core.interfaces;

import android.support.annotation.NonNull;

import com.adyen.core.PaymentRequest;
import com.adyen.core.models.PaymentRequestResult;

/**
 * Listener that is required to be implemented for a {@link com.adyen.core.PaymentRequest}.
 *
 * PaymentRequest communicates with the client via this listener.
 *
 */
public interface PaymentRequestListener {

    /**
     * The client is asked to provide the payment initialization data.
     * This data is supposed to be retrieved from your merchant server. The merchant server gets this
     * data from the Adyen backend.
     *
     * @param paymentRequest {@link PaymentRequest} for which the payment data is requested for.
     * @param sdkToken The token that has to be included in the payment data setup request.
     * @param callback {@link PaymentDataCallback} instance for notifying the {@link PaymentRequest}.
     */
    void onPaymentDataRequested(@NonNull PaymentRequest paymentRequest, @NonNull String sdkToken,
                                @NonNull PaymentDataCallback callback);

    /**
     * Once the payment process has been completed a result is delivered.
     *
     * @param paymentRequest Payment request for which the payment result is delivered.
     * @param result {@link PaymentRequestResult} object which includes the payment result.
     */
    void onPaymentResult(@NonNull PaymentRequest paymentRequest, @NonNull PaymentRequestResult result);

}

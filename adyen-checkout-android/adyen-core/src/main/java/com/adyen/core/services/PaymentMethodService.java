package com.adyen.core.services;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.core.PaymentRequest;
import com.adyen.core.interfaces.PaymentMethodAvailabilityCallback;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.models.PaymentMethod;

import java.util.Map;

/**
 * Provides a unified method to process wallet .
 */
public interface PaymentMethodService {

    /**
     * The interface for notifying the details for the selected payment method.
     */
    interface PaymentDetailsListener {
        void onPaymentDetails(@NonNull Object paymentMethodData);
        void onPaymentDetailsError(@NonNull Throwable throwable);
    }

    /**
     * Checks which payment methods are available for the current merchant.
     *
     * @param context {@link Context} instance to check if the payment method is available.
     * @param paymentMethod {@link PaymentMethod} for which the availability will be checked.
     * @param callback {@link PaymentMethodAvailabilityCallback} for notifying the caller.
     */
    void checkAvailability(@NonNull Context context, @NonNull PaymentMethod paymentMethod,
                           @NonNull PaymentMethodAvailabilityCallback callback);

    /**
     * Starts the payment process for the payment request provided.
     *
     * @param context {@link Context} instance to check.
     * @param paymentRequest {@link PaymentRequest} to be processed.
     * @param paymentRequestListener {@link PaymentRequestListener} instance for communicating with the caller.
     * @param paymentRequestDetailsListener {@link PaymentRequestDetailsListener} instance for communicating
     *                                                                           with the caller.
     */
    void process(@NonNull Context context, @NonNull PaymentRequest paymentRequest,
                 @NonNull PaymentRequestListener paymentRequestListener,
                 @Nullable PaymentRequestDetailsListener paymentRequestDetailsListener);

    /**
     * Get payment method details for the selected module.
     * @param paymentData
     * @param paymentRequest
     * @param paymentDetailsListener
     * @throws Throwable in case of error.
     */
    void getPaymentDetails(@NonNull Map<String, Object> paymentData,
                           @NonNull PaymentRequest paymentRequest,
                           @NonNull PaymentDetailsListener paymentDetailsListener) throws Throwable;

}

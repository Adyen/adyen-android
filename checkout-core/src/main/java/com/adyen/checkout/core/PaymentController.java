package com.adyen.checkout.core;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.handler.PaymentSetupParametersHandler;
import com.adyen.checkout.core.handler.StartPaymentParametersHandler;
import com.adyen.checkout.core.internal.PaymentHandlerImpl;
import com.adyen.checkout.core.internal.PaymentReferenceImpl;
import com.adyen.checkout.core.internal.PaymentSetupParametersImpl;
import com.adyen.checkout.core.internal.StartPaymentParametersImpl;
import com.adyen.checkout.core.internal.model.PaymentSessionImpl;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/05/2018.
 */
public final class PaymentController {
    /**
     * Starts the payment.
     *
     * @param activity The current {@link Activity}.
     * @param handler The {@link StartPaymentParametersHandler} that will receive the result callback.
     *
     * @see PaymentSetupParametersHandler#onRequestPaymentSession(PaymentSetupParameters)
     * @see StartPaymentParametersHandler#onError(CheckoutException)
     */
    public static void startPayment(@NonNull Activity activity, @NonNull PaymentSetupParametersHandler handler) {
        try {
            PaymentSetupParametersImpl paymentSetupParameters = new PaymentSetupParametersImpl(activity);
            handler.onRequestPaymentSession(paymentSetupParameters);
        } catch (CheckoutException e) {
            handler.onError(e);
        }
    }

    /**
     * Handles a payment session response.
     *
     * @param activity The current {@link Activity}.
     * @param encodedPaymentSession The Base64-encoded payment session.
     * @param handler The {@link StartPaymentParametersHandler} that will receive the result callback.
     *
     * @see StartPaymentParametersHandler#onPaymentInitialized(StartPaymentParameters)
     * @see StartPaymentParametersHandler#onError(CheckoutException)
     */
    public static void handlePaymentSessionResponse(
            @NonNull Activity activity,
            @NonNull String encodedPaymentSession,
            @NonNull StartPaymentParametersHandler handler
    ) {
        try {
            PaymentSessionImpl paymentSession = PaymentSessionImpl.decode(encodedPaymentSession);
            PaymentReferenceImpl paymentReference = PaymentHandlerImpl.createPaymentReference(activity, paymentSession);
            StartPaymentParametersImpl startPaymentParameters = new StartPaymentParametersImpl(paymentReference, paymentSession);
            handler.onPaymentInitialized(startPaymentParameters);
        } catch (CheckoutException e) {
            handler.onError(e);
        }
    }

    private PaymentController() {
        throw new IllegalStateException("No instances.");
    }
}

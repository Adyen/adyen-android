package com.adyen.checkout.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.PaymentController;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.StartPaymentParameters;
import com.adyen.checkout.core.handler.StartPaymentParametersHandler;
import com.adyen.checkout.core.internal.model.DeviceFingerprint;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.googlewallet.GoogleWalletHandler;
import com.adyen.checkout.ui.internal.CheckoutHandler;
import com.adyen.checkout.ui.internal.card.CardHandler;
import com.adyen.checkout.ui.internal.doku.DokuHandler;
import com.adyen.checkout.ui.internal.giropay.GiroPayHandler;
import com.adyen.checkout.ui.internal.issuer.IssuerHandler;
import com.adyen.checkout.ui.internal.qiwiwallet.QiwiWalletHandler;
import com.adyen.checkout.ui.internal.sepadirectdebit.SepaDirectDebitHandler;
import com.adyen.checkout.wechatpay.WeChatPayHandler;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 24/05/2018.
 */
public final class CheckoutController {
    /**
     * Starts the payment.
     *
     * @param activity The current {@link Activity}.
     * @param handler The {@link StartPaymentParametersHandler} that will receive the result callback.
     *
     * @see CheckoutSetupParametersHandler#onRequestPaymentSession(CheckoutSetupParameters)
     * @see StartPaymentParametersHandler#onError(CheckoutException)
     */
    public static void startPayment(@NonNull Activity activity, @NonNull CheckoutSetupParametersHandler handler) {
        try {
            CheckoutSetupParameters checkoutSetupParameters = new CheckoutSetupParametersImpl(activity);
            handler.onRequestPaymentSession(checkoutSetupParameters);
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
        PaymentController.handlePaymentSessionResponse(activity, encodedPaymentSession, handler);
    }

    /**
     * Get a {@link PaymentMethodHandler} that is capable of handling the whole checkout process.
     *
     * @param paymentParameters The {@link StartPaymentParameters} to handle.
     * @return The {@link PaymentMethodHandler}.
     */
    @NonNull
    public static PaymentMethodHandler getCheckoutHandler(@NonNull StartPaymentParameters paymentParameters) {
        return new CheckoutHandler(paymentParameters);
    }

    /**
     * Get a {@link PaymentMethodHandler} that is capable of handling a given {@link PaymentMethod}.
     *
     * @param activity The current {@link Activity}.
     * @param paymentReference The {@link PaymentReference} to handle.
     * @param paymentMethod The {@link PaymentMethod} to handle.
     * @return The {@link PaymentMethodHandler}, or {@code null} if non can handle the given {@link PaymentMethod}.
     */
    @Nullable
    public static PaymentMethodHandler getPaymentMethodHandler(
            @NonNull Activity activity,
            @NonNull PaymentReference paymentReference,
            @NonNull PaymentMethod paymentMethod
    ) {
        Application application = activity.getApplication();

        if (CardHandler.FACTORY.supports(application, paymentMethod)) {
            return new CardHandler(paymentReference, paymentMethod);
        } else if (DokuHandler.FACTORY.supports(application, paymentMethod)) {
            return new DokuHandler(paymentReference, paymentMethod);
        } else if (GiroPayHandler.FACTORY.supports(application, paymentMethod)) {
            return new GiroPayHandler(paymentReference, paymentMethod);
        } else if (GoogleWalletHandler.FACTORY.supports(application, paymentMethod)) {
            return new GoogleWalletHandler(paymentReference, paymentMethod);
        } else if (IssuerHandler.FACTORY.supports(application, paymentMethod)) {
            return new IssuerHandler(paymentReference, paymentMethod);
        } else if (QiwiWalletHandler.FACTORY.supports(application, paymentMethod)) {
            return new QiwiWalletHandler(paymentReference, paymentMethod);
        } else if (SepaDirectDebitHandler.FACTORY.supports(application, paymentMethod)) {
            return new SepaDirectDebitHandler(paymentReference, paymentMethod);
        } else if (WeChatPayHandler.FACTORY.supports(application, paymentMethod)) {
            return new WeChatPayHandler(paymentReference, paymentMethod);
        } else {
            return null;
        }
    }

    private CheckoutController() {
        throw new IllegalStateException("No instances.");
    }

    private static final class CheckoutSetupParametersImpl implements CheckoutSetupParameters {
        private final String mSdkToken;

        private final String mReturnUrl;

        private CheckoutSetupParametersImpl(@NonNull Context context) throws CheckoutException {
            mSdkToken = DeviceFingerprint.generateSdkToken(context, "quick");
            mReturnUrl = "checkout://" + context.getPackageName();
        }

        @NonNull
        @Override
        public String getSdkToken() {
            return mSdkToken;
        }

        @NonNull
        @Override
        public String getReturnUrl() {
            return mReturnUrl;
        }
    }
}

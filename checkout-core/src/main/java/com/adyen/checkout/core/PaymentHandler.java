/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 15/05/2018.
 */

package com.adyen.checkout.core;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.core.handler.AdditionalDetailsHandler;
import com.adyen.checkout.core.handler.ErrorHandler;
import com.adyen.checkout.core.handler.RedirectHandler;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentMethodDetails;
import com.adyen.checkout.core.model.PaymentSession;

/**
 * The {@link PaymentHandler} interface provides means to perform payment related tasks.
 */
public interface PaymentHandler {
    /**
     * @return An instance of the {@link LogoApi} to retrieve payment method logos with.
     */
    @NonNull
    LogoApi getLogoApi();

    /**
     * @return An {@link Observable} object holding the {@link NetworkingState}.
     */
    @NonNull
    Observable<NetworkingState> getNetworkingStateObservable();

    /**
     * @return An {@link Observable} object holding the current {@link PaymentSession}.
     */
    @NonNull
    Observable<PaymentSession> getPaymentSessionObservable();

    /**
     * @return An {@link Observable} object holding the {@link PaymentResult} when the payment has been completed.
     */
    @NonNull
    Observable<PaymentResult> getPaymentResultObservable();

    /**
     * Sets an {@link Activity} scoped {@link RedirectHandler} for this {@link PaymentHandler}. Setting this {@link RedirectHandler} is required for
     * {@link PaymentMethod PaymentMethods} that require a redirect to an external party to complete the payment.
     *
     * @param activity The current {@link Activity}.
     * @param redirectHandler The {@link RedirectHandler} responsible for handling {@link RedirectDetails}.
     */
    void setRedirectHandler(@NonNull Activity activity, @NonNull RedirectHandler redirectHandler);

    /**
     * Sets an {@link Activity} scoped {@link AdditionalDetailsHandler} for this {@link PaymentHandler}. Setting this {@link AdditionalDetailsHandler}
     * is required for {@link PaymentMethod PaymentMethods} that might require {@link AdditionalDetails} after calling
     * {@link #initiatePayment(PaymentMethod, PaymentMethodDetails)} the first time.
     *
     * @param activity The current {@link Activity}.
     * @param additionalDetailsHandler The {@link AdditionalDetailsHandler} responsible for handling {@link AdditionalDetails}.
     */
    void setAdditionalDetailsHandler(@NonNull Activity activity, @NonNull AdditionalDetailsHandler additionalDetailsHandler);

    /**
     * Sets an {@link Activity} scoped {@link ErrorHandler} for this {@link PaymentHandler}.
     *
     * @param activity The current {@link Activity}.
     * @param errorHandler The {@link ErrorHandler} responsible for handling {@link CheckoutException CheckoutExceptions}.
     */
    void setErrorHandler(@NonNull Activity activity, @NonNull ErrorHandler errorHandler);

    /**
     * Initiates a payment with a given {@link PaymentMethod} and optional {@link PaymentMethodDetails}. Whether you have to submit
     * {@link PaymentMethodDetails} depends on the {@link PaymentMethod PaymentMethod's}
     * {@link com.adyen.checkout.core.model.InputDetail InputDetails}.
     *
     * @param paymentMethod The {@link PaymentMethod} to initiate the payment with.
     * @param paymentMethodDetails The {@link PaymentMethodDetails} to initiate the payment with.
     */
    void initiatePayment(@NonNull PaymentMethod paymentMethod, @Nullable PaymentMethodDetails paymentMethodDetails);

    /**
     * Submits additional details for a payment that was previously initiated.
     *
     * @param paymentMethodDetails The {@link PaymentMethodDetails} containing the additional details needed for the payment.
     */
    void submitAdditionalDetails(@NonNull PaymentMethodDetails paymentMethodDetails);

    /**
     * Handles the result for a payment for which a redirect was previously performed.
     *
     * @param redirectResult The redirect result {@link Uri}.
     */
    void handleRedirectResult(@NonNull Uri redirectResult);

    /**
     * Deletes a one-click {@link PaymentMethod}.
     *
     * @param paymentMethod The one-click {@link PaymentMethod} to be deleted.
     */
    void deleteOneClickPaymentMethod(@NonNull PaymentMethod paymentMethod);
}

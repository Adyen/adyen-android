/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 31/10/2018.
 */

package com.adyen.checkout.googlepay.internal;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.GooglePayConfiguration;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.util.PaymentMethodTypes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class IsReadyToPayCallable implements Callable<Boolean> {
    private static final int WAIT_TIME = 5;

    private final Context mApplicationContext;

    private final PaymentSession mPaymentSession;

    private final PaymentMethod mPaymentMethod;

    public IsReadyToPayCallable(@NonNull Context context, @NonNull PaymentSession paymentSession, @NonNull PaymentMethod paymentMethod) {
        mApplicationContext = context.getApplicationContext();
        mPaymentSession = paymentSession;
        mPaymentMethod = paymentMethod;
    }

    @NonNull
    @Override
    public Boolean call() {
        if (!PaymentMethodTypes.GOOGLE_PAY.equals(mPaymentMethod.getType())) {
            return false;
        }

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mApplicationContext) != ConnectionResult.SUCCESS) {
            return false;
        }

        JSONArray allowedCardNetworks = GooglePayUtil.getAllowedCardNetworks(mPaymentSession);
        if (allowedCardNetworks.length() < 1) {
            return false;
        }

        GooglePayConfiguration configuration;
        try {
            configuration = mPaymentMethod.getConfiguration(GooglePayConfiguration.class);
        } catch (CheckoutException e) {
            // Invalid configuration.
            return false;
        }

        Task<Boolean> readyToPayTask = GooglePayUtil.getIsReadyToPayTask(mApplicationContext, mPaymentSession, configuration);

        try {
            return Tasks.await(readyToPayTask, WAIT_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }
}

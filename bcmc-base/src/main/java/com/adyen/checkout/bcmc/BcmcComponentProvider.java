/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.adyen.checkout.base.ComponentAvailableCallback;
import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.GenericPaymentMethodDelegate;
import com.adyen.checkout.base.component.lifecycle.PaymentComponentViewModelFactory;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public class BcmcComponentProvider implements PaymentComponentProvider<BcmcComponent, BcmcConfiguration> {

    @SuppressWarnings("LambdaLast")
    @Override
    @NonNull
    public BcmcComponent get(
            @NonNull ViewModelStoreOwner viewModelStoreOwner,
            @NonNull PaymentMethod paymentMethod,
            @NonNull BcmcConfiguration configuration) {
        final PaymentComponentViewModelFactory factory =
                new PaymentComponentViewModelFactory(new GenericPaymentMethodDelegate(paymentMethod), configuration);
        return new ViewModelProvider(viewModelStoreOwner, factory).get(BcmcComponent.class);
    }

    @Override
    public void isAvailable(
            @NonNull Application applicationContext,
            @NonNull PaymentMethod paymentMethod,
            @NonNull BcmcConfiguration configuration,
            @NonNull ComponentAvailableCallback<BcmcConfiguration> callback) {

        final boolean isPubKeyAvailable = !TextUtils.isEmpty(configuration.getPublicKey());
        callback.onAvailabilityResult(isPubKeyAvailable, paymentMethod, configuration);
    }
}

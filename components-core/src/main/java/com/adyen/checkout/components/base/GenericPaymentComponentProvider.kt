/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.components.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.adyen.checkout.components.ComponentAvailableCallback;
import com.adyen.checkout.components.PaymentComponentProvider;
import com.adyen.checkout.components.base.lifecycle.PaymentComponentViewModelFactory;
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod;

public final class GenericPaymentComponentProvider<BaseComponentT extends BasePaymentComponent, ConfigurationT extends Configuration>
        implements PaymentComponentProvider<BaseComponentT, ConfigurationT> {

    private final Class<BaseComponentT> mComponentClass;

    public GenericPaymentComponentProvider(@NonNull Class<BaseComponentT> modelClass) {
        mComponentClass = modelClass;
    }

    @Override
    @NonNull
    @SuppressWarnings("LambdaLast")
    public BaseComponentT get(
            @NonNull ViewModelStoreOwner viewModelStoreOwner,
            @NonNull PaymentMethod paymentMethod,
            @NonNull ConfigurationT configuration) {
        final PaymentComponentViewModelFactory factory =
                new PaymentComponentViewModelFactory(new GenericPaymentMethodDelegate(paymentMethod), configuration);
        return new ViewModelProvider(viewModelStoreOwner, factory).get(mComponentClass);
    }

    @Override
    public void isAvailable(
            @NonNull Application applicationContext,
            @NonNull PaymentMethod paymentMethod,
            @NonNull ConfigurationT config,
            @NonNull ComponentAvailableCallback<ConfigurationT> callback) {
        callback.onAvailabilityResult(true, paymentMethod, config);
    }
}

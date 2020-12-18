/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.components;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStoreOwner;

import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.core.exception.CheckoutException;

/**
 * Provides an instance of te associated Component linked to provided lifecycle and config.
 *
 * @param <ComponentT> The Component to be provided
 * @param <ConfigurationT> The Configuration for the Component to be provided
 */
public interface PaymentComponentProvider<ComponentT extends PaymentComponent, ConfigurationT extends Configuration>
        extends ComponentProvider<ComponentT> {

    @SuppressWarnings("LambdaLast")
    @NonNull
    ComponentT get(
            @NonNull ViewModelStoreOwner viewModelStoreOwner,
            @NonNull PaymentMethod paymentMethod,
            @NonNull ConfigurationT configuration
    ) throws CheckoutException;

    void isAvailable(
            @NonNull Application applicationContext,
            @NonNull PaymentMethod paymentMethod,
            @NonNull ConfigurationT configuration,
            @NonNull ComponentAvailableCallback<ConfigurationT> callback);
}

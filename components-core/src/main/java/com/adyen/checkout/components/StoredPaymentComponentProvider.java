/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */

package com.adyen.checkout.components;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistryOwner;

import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod;
import com.adyen.checkout.core.exception.CheckoutException;


public interface StoredPaymentComponentProvider<ComponentT extends PaymentComponent, ConfigurationT extends Configuration>
        extends PaymentComponentProvider<ComponentT, ConfigurationT> {
    /**
     * Get a {@link PaymentComponent} with a stored payment method.
     *
     * @param owner               The Activity or Fragment to associate the lifecycle.
     * @param storedPaymentMethod The corresponding  {@link StoredPaymentMethod} object.
     * @param configuration       The Configuration of the component.
     * @return The Component
     */
    @SuppressWarnings("LambdaLast")
    @NonNull
    <T extends SavedStateRegistryOwner & ViewModelStoreOwner> ComponentT get(
            @NonNull T owner,
            @NonNull StoredPaymentMethod storedPaymentMethod,
            @NonNull ConfigurationT configuration
    ) throws CheckoutException;

    /**
     * Get a {@link PaymentComponent} with a stored payment method.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param storedPaymentMethod     The corresponding  {@link StoredPaymentMethod} object.
     * @param configuration           The Configuration of the component.
     * @param defaultArgs             Values from this {@code Bundle} will be used as defaults by {@link SavedStateHandle} passed in {@link ViewModel
     *                                ViewModels} if there is no previously saved state or previously saved state misses a value by such key
     * @return The Component
     */
    @SuppressWarnings("LambdaLast")
    @NonNull
    ComponentT get(
            @NonNull SavedStateRegistryOwner savedStateRegistryOwner,
            @NonNull ViewModelStoreOwner viewModelStoreOwner,
            @NonNull StoredPaymentMethod storedPaymentMethod,
            @NonNull ConfigurationT configuration,
            @Nullable Bundle defaultArgs
    ) throws CheckoutException;
}

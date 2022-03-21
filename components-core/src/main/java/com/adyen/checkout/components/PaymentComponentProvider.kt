/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
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
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.core.exception.CheckoutException;

/**
 * Provides an instance of the associated Component linked to provided lifecycle and config.
 *
 * @param <ComponentT>     The Component to be provided
 * @param <ConfigurationT> The Configuration for the Component to be provided
 */
public interface PaymentComponentProvider<ComponentT extends PaymentComponent, ConfigurationT extends Configuration>
        extends ComponentProvider<ComponentT> {
    /**
     * Get a {@link PaymentComponent}.
     *
     * @param owner         The Activity or Fragment to associate the lifecycle.
     * @param paymentMethod The corresponding  {@link PaymentMethod} object.
     * @param configuration The Configuration of the component.
     * @return The Component
     */
    @SuppressWarnings("LambdaLast")
    @NonNull
    <T extends SavedStateRegistryOwner & ViewModelStoreOwner> ComponentT get(
            @NonNull T owner,
            @NonNull PaymentMethod paymentMethod,
            @NonNull ConfigurationT configuration
    ) throws CheckoutException;

    /**
     * Get a {@link PaymentComponent}.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param paymentMethod           The corresponding  {@link PaymentMethod} object.
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
            @NonNull PaymentMethod paymentMethod,
            @NonNull ConfigurationT configuration,
            @Nullable Bundle defaultArgs
    ) throws CheckoutException;
}

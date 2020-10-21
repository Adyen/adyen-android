/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.base;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.Configuration;

/**
 * A {@link Component} is a class that helps to retrieve or format data related to a part of the Checkout API payment.
 *
 * @param <ComponentResultT> The main parameter that notifies changes on this component.
 * @param <ConfigurationT> The Configuration object associated with this Component.
 */
public interface Component<ComponentResultT, ConfigurationT extends Configuration> {

    /**
     * Observe changes on the result of this component.
     * A valid results contains data that can be sent to the payments API to make a payment.
     *
     * @param lifecycleOwner The lifecycle for which the observer will be active.
     * @param observer The observer that will receive the updates.
     */
    void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ComponentResultT> observer);

    /**
     * Observe if an unexpected error happens during the processing of the Component.
     * Error handling might need to fail the payment process, retry or show UI feedback.
     *
     * @param lifecycleOwner The lifecycle for which the observer will be active.
     * @param observer The observer that will receive the updates.
     */
    void observeErrors(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ComponentError> observer);

    /**
     * @return The {@link Configuration} object used to initialize this Component.
     */
    @Nullable
    ConfigurationT getConfiguration();
}

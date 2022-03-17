/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.components

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.base.Configuration

/**
 * A [Component] is a class that helps to retrieve or format data related to a part of the Checkout API payment.
 *
 * @param <ComponentResultT> The main parameter that notifies changes on this component.
 * @param <ConfigurationT> The Configuration object associated with this Component.
 */
interface Component<ComponentResultT, ConfigurationT : Configuration> {
    /**
     * Observe changes on the result of this component.
     * A valid result contains data that can be sent to the payments API to make a payment.
     *
     * @param lifecycleOwner The lifecycle for which the observer will be active.
     * @param observer The observer that will receive the updates.
     */
    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ComponentResultT>)

    /**
     * Remove all observers attached to this component using [.observe].
     *
     * @param lifecycleOwner The lifecycle for which the observer is active.
     */
    fun removeObservers(lifecycleOwner: LifecycleOwner)

    /**
     * Remove a specific observer attached to this component using [.observe].
     *
     * @param observer The observer to be removed.
     */
    fun removeObserver(observer: Observer<ComponentResultT>)

    /**
     * Observe if an unexpected error happens during the processing of the Component.
     * Error handling might need to fail the payment process, retry or show UI feedback.
     *
     * @param lifecycleOwner The lifecycle for which the observer will be active.
     * @param observer The observer that will receive the updates.
     */
    fun observeErrors(lifecycleOwner: LifecycleOwner, observer: Observer<ComponentError>)

    /**
     * Remove all error observers attached to this component using [.observeErrors].
     *
     * @param lifecycleOwner The lifecycle for which the observer is active.
     */
    fun removeErrorObservers(lifecycleOwner: LifecycleOwner)

    /**
     * Remove a specific error observer attached to this component using [.observeErrors].
     *
     * @param observer The observer to be removed.
     */
    fun removeErrorObserver(observer: Observer<ComponentError>)

    /**
     * @return The [Configuration] object used to initialize this Component.
     */
    val configuration: ConfigurationT
}

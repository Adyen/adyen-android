/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */
package com.adyen.checkout.components

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.core.exception.CheckoutException

/**
 * Provides an instance of the associated Component linked to provided lifecycle and config.
 *
 * @param <ComponentT>     The Component to be provided
 * @param <ConfigurationT> The Configuration for the Component to be provided
 */
interface PaymentComponentProvider<ComponentT : PaymentComponent<*>, ConfigurationT : Configuration> :
    ComponentProvider<ComponentT> {

    /**
     * Get a [PaymentComponent].
     *
     * @param owner         The Activity or Fragment to associate the lifecycle.
     * @param paymentMethod The corresponding  [PaymentMethod] object.
     * @param configuration The Configuration of the component.
     * @param key           The key to use to identify the [PaymentComponent].
     * @param application   The [Application] instance used to handle actions with.
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Throws(CheckoutException::class)
    fun <T> get(
        owner: T, // TODO sessions: change this into fragment and activity to get the correct lifecycle in the fragment
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        key: String? = null,
    ): ComponentT where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, paymentMethod, configuration, application, null, key)
    }

    /**
     * Get a [PaymentComponent].
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param paymentMethod           The corresponding  [PaymentMethod] object.
     * @param configuration           The Configuration of the component.
     * @param defaultArgs             Values from this `Bundle` will be used as defaults by [SavedStateHandle] passed in
     *                                [ViewModel] if there is no previously saved state or previously saved state misses
     *                                a value by such key
     * @param key                     The key to use to identify the [PaymentComponent].
     * @param application   The [Application] instance used to handle actions with.
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    @Throws(CheckoutException::class)
    fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        // TODO sessions: add lifecycle owner
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): ComponentT

    // TODO docs
    fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean
}

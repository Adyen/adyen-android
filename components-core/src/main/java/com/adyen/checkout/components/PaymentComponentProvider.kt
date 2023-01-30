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
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.base.ComponentCallback
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.Order

// TODO SESSIONS docs
/**
 * Provides an instance of the associated Component linked to provided lifecycle and config.
 *
 * @param <ComponentT>     The Component to be provided
 * @param <ConfigurationT> The Configuration for the Component to be provided
 */
interface PaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>
    > :
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
    @Suppress("LongParameterList")
    fun get(
        fragment: Fragment,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            paymentMethod = paymentMethod,
            configuration = configuration,
            application = application,
            defaultArgs = null,
            key = key,
            componentCallback = componentCallback,
            order = order,
        )
    }

    @Suppress("LongParameterList")
    fun get(
        activity: ComponentActivity,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            paymentMethod = paymentMethod,
            configuration = configuration,
            application = application,
            defaultArgs = null,
            key = key,
            componentCallback = componentCallback,
            order = order,
        )
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
    fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order?,
        key: String?,
    ): ComponentT

    fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean
}

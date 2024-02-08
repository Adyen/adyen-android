/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */
package com.adyen.checkout.components.core.internal.provider

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.util.requireApplication

interface PaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>,
    ComponentCallbackT : ComponentCallback<ComponentStateT>
    > :
    ComponentProvider<ComponentT> {

    //region CheckoutConfiguration

    /**
     * Get a [PaymentComponent].
     *
     * @param fragment                The Fragment to associate the lifecycle.
     * @param paymentMethod           The corresponding  [PaymentMethod] object.
     * @param checkoutConfiguration   The [CheckoutConfiguration].
     * @param callback                The callback to handle events from the [PaymentComponent].
     * @param order                   An [Order] in case of an ongoing partial payment flow.
     * @param key                     The key to use to identify the [PaymentComponent].
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
        checkoutConfiguration: CheckoutConfiguration,
        callback: ComponentCallbackT,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            paymentMethod = paymentMethod,
            checkoutConfiguration = checkoutConfiguration,
            application = fragment.requireApplication(),
            componentCallback = callback,
            order = order,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent].
     *
     * @param activity                The Activity to associate the lifecycle.
     * @param paymentMethod           The corresponding  [PaymentMethod] object.
     * @param checkoutConfiguration   The [CheckoutConfiguration].
     * @param callback                The callback to handle events from the [PaymentComponent].
     * @param order                   An [Order] in case of an ongoing partial payment flow.
     * @param key                     The key to use to identify the [PaymentComponent].
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        activity: ComponentActivity,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ComponentCallbackT,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            paymentMethod = paymentMethod,
            checkoutConfiguration = checkoutConfiguration,
            application = activity.application,
            componentCallback = callback,
            order = order,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent].
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param lifecycleOwner          The lifecycle owner, normally an Activity or Fragment.
     * @param paymentMethod           The corresponding  [PaymentMethod] object.
     * @param checkoutConfiguration   The [CheckoutConfiguration].
     * @param application             Your main application class.
     * @param componentCallback       The callback to handle events from the [PaymentComponent].
     * @param order                   An [Order] in case of an ongoing partial payment flow.
     * @param key                     The key to use to identify the [PaymentComponent].
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
        checkoutConfiguration: CheckoutConfiguration,
        application: Application,
        componentCallback: ComponentCallbackT,
        order: Order?,
        key: String?,
    ): ComponentT

    //endregion

    //region Payment method specific configuration

    /**
     * Get a [PaymentComponent].
     *
     * @param fragment      The Fragment to associate the lifecycle.
     * @param paymentMethod The corresponding  [PaymentMethod] object.
     * @param configuration The Configuration of the component.
     * @param callback      The callback to handle events from the [PaymentComponent].
     * @param order         An [Order] in case of an ongoing partial payment flow.
     * @param key           The key to use to identify the [PaymentComponent].
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
        callback: ComponentCallbackT,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            paymentMethod = paymentMethod,
            configuration = configuration,
            application = fragment.requireApplication(),
            componentCallback = callback,
            order = order,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent].
     *
     * @param activity      The Activity to associate the lifecycle.
     * @param paymentMethod The corresponding  [PaymentMethod] object.
     * @param configuration The Configuration of the component.
     * @param callback      The callback to handle events from the [PaymentComponent].
     * @param order         An [Order] in case of an ongoing partial payment flow.
     * @param key           The key to use to identify the [PaymentComponent].
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        activity: ComponentActivity,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        callback: ComponentCallbackT,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            paymentMethod = paymentMethod,
            configuration = configuration,
            application = activity.application,
            componentCallback = callback,
            order = order,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent].
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param lifecycleOwner          The lifecycle owner, normally an Activity or Fragment.
     * @param paymentMethod           The corresponding  [PaymentMethod] object.
     * @param configuration           The Configuration of the component.
     * @param application             Your main application class.
     * @param componentCallback       The callback to handle events from the [PaymentComponent].
     * @param order                   An [Order] in case of an ongoing partial payment flow.
     * @param key                     The key to use to identify the [PaymentComponent].
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
        componentCallback: ComponentCallbackT,
        order: Order?,
        key: String?,
    ): ComponentT

    //endregion

    /**
     * Checks if the provided component can handle a given payment method.
     */
    fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean
}

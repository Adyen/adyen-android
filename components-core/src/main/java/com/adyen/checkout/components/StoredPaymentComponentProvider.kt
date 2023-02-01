/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
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
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.util.requireApplication

interface StoredPaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>
    > :
    PaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT> {

    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param owner               The Activity or Fragment to associate the lifecycle.
     * @param storedPaymentMethod The corresponding  [StoredPaymentMethod] object.
     * @param configuration       The Configuration of the component.
     * @param key                 Key
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        fragment: Fragment,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            storedPaymentMethod = storedPaymentMethod,
            configuration = configuration,
            application = fragment.requireApplication(),
            defaultArgs = null,
            key = key,
            componentCallback = componentCallback,
            order = order,
        )
    }

    @Suppress("LongParameterList")
    fun get(
        activity: ComponentActivity,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order? = null,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            storedPaymentMethod = storedPaymentMethod,
            configuration = configuration,
            application = activity.application,
            defaultArgs = null,
            key = key,
            componentCallback = componentCallback,
            order = order,
        )
    }

    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param storedPaymentMethod     The corresponding  [StoredPaymentMethod] object.
     * @param configuration           The Configuration of the component.
     * @param defaultArgs             Values from this `Bundle` will be used as defaults by [SavedStateHandle] passed in
     *                                [ViewModel] if there is no previously saved state or previously saved state misses
     *                                a value by such key
     * @param key                     Key
     * @param application             The [Application] instance used to handle actions with.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        defaultArgs: Bundle?,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order?,
        key: String?,
    ): ComponentT

    // TODO docs
    fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean
}

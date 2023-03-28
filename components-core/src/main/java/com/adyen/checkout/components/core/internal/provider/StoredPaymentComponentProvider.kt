/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */
package com.adyen.checkout.components.core.internal.provider

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.util.requireApplication

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StoredPaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>
    > {

    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param fragment              The Fragment to associate the lifecycle.
     * @param storedPaymentMethod   The corresponding  [StoredPaymentMethod] object.
     * @param configuration         The Configuration of the component.
     * @param callback              The callback to handle events from the [PaymentComponent].
     * @param order                 An [Order] in case of an ongoing partial payment flow.
     * @param key                   The key to use to identify the [PaymentComponent].
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        fragment: Fragment,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        callback: ComponentCallback<ComponentStateT>,
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
            componentCallback = callback,
            order = order,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param activity              The Activity to associate the lifecycle.
     * @param storedPaymentMethod   The corresponding  [StoredPaymentMethod] object.
     * @param configuration         The Configuration of the component.
     * @param callback              The callback to handle events from the [PaymentComponent].
     * @param order                 An [Order] in case of an ongoing partial payment flow.
     * @param key                   The key to use to identify the [PaymentComponent].
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        activity: ComponentActivity,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        callback: ComponentCallback<ComponentStateT>,
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
            componentCallback = callback,
            order = order,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param lifecycleOwner          The lifecycle owner, normally an Activity or Fragment.
     * @param storedPaymentMethod     The corresponding  [StoredPaymentMethod] object.
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
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order?,
        key: String?,
    ): ComponentT

    /**
     * Checks if the provided component can handle a given stored payment method.
     */
    fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean
}

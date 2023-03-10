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
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.util.requireApplication

// TODO SESSIONS docs
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StoredPaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>
    > {

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
            componentCallback = componentCallback,
            order = order,
            key = key,
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
            componentCallback = componentCallback,
            order = order,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent] with a stored payment method.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param storedPaymentMethod     The corresponding  [StoredPaymentMethod] object.
     * @param configuration           The Configuration of the component.
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
        componentCallback: ComponentCallback<ComponentStateT>,
        order: Order?,
        key: String?,
    ): ComponentT

    // TODO docs
    fun isPaymentMethodSupported(storedPaymentMethod: StoredPaymentMethod): Boolean
}

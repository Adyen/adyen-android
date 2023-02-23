/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/1/2023.
 */

package com.adyen.checkout.sessions.internal.provider

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.util.requireApplication
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback

// TODO SESSIONS docs
/**
 * Provides an instance of the associated Component linked to provided lifecycle and config.
 *
 * @param <ComponentT>     The Component to be provided
 * @param <ConfigurationT> The Configuration for the Component to be provided
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SessionPaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>
    > {

    @Suppress("LongParameterList")
    fun get(
        fragment: Fragment,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        componentCallback: SessionComponentCallback<ComponentStateT>,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            configuration = configuration,
            application = fragment.requireApplication(),
            componentCallback = componentCallback,
            key = key,
        )
    }

    @Suppress("LongParameterList")
    fun get(
        activity: ComponentActivity,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        componentCallback: SessionComponentCallback<ComponentStateT>,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            configuration = configuration,
            application = activity.application,
            componentCallback = componentCallback,
            key = key,
        )
    }

    @Suppress("LongParameterList")
    fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        checkoutSession: CheckoutSession,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: SessionComponentCallback<ComponentStateT>,
        key: String?,
    ): ComponentT
}

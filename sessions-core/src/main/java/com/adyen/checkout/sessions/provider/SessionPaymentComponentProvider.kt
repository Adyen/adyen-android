/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/1/2023.
 */

package com.adyen.checkout.sessions.provider

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.util.requireApplication
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback

// TODO SESSIONS docs
/**
 * Provides an instance of the associated Component linked to provided lifecycle and config.
 *
 * @param <ComponentT>     The Component to be provided
 * @param <ConfigurationT> The Configuration for the Component to be provided
 */
interface SessionPaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>
    > :
    PaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT> {

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
            defaultArgs = null,
            key = key,
            componentCallback = componentCallback,
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
            defaultArgs = null,
            key = key,
            componentCallback = componentCallback,
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
        defaultArgs: Bundle?,
        componentCallback: SessionComponentCallback<ComponentStateT>,
        key: String?,
    ): ComponentT
}

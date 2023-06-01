/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/5/2023.
 */

package com.adyen.checkout.components.compose

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.Component
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.provider.PaymentComponentProvider
import com.adyen.checkout.components.core.internal.provider.StoredPaymentComponentProvider
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.internal.provider.SessionPaymentComponentProvider
import com.adyen.checkout.sessions.core.internal.provider.SessionStoredPaymentComponentProvider
import com.adyen.checkout.ui.core.AdyenComponentView
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent

// TODO this file is in drop-in for now to make testing easier, it should be split into multiple files and moved into
//  2 different modules: one for components and another for drop-in

// TODO test different instances of the same component
// TODO docs
@Composable
fun <
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>,
    ComponentCallbackT : ComponentCallback<ComponentStateT>
    > PaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT, ComponentCallbackT>.get(
    paymentMethod: PaymentMethod,
    configuration: ConfigurationT,
    callback: ComponentCallbackT,
    order: Order? = null,
    // TODO check if we should make this key mandatory to bring awareness to the lifecycle/view model issue
    key: String? = null,
): ComponentT {
    return get(
        savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
        viewModelStoreOwner = LocalViewModelStoreOwner.current
            ?: throw ComponentException("Cannot find current LocalViewModelStoreOwner"),
        lifecycleOwner = LocalLifecycleOwner.current,
        paymentMethod = paymentMethod,
        configuration = configuration,
        application = LocalContext.current.applicationContext as Application,
        componentCallback = callback,
        order = order,
        key = key,
    )
}

// TODO docs
@Composable
fun <
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>,
    ComponentCallbackT : ComponentCallback<ComponentStateT>
    > StoredPaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT, ComponentCallbackT>.get(
    storedPaymentMethod: StoredPaymentMethod,
    configuration: ConfigurationT,
    callback: ComponentCallbackT,
    order: Order? = null,
    key: String? = null,
): ComponentT {
    return get(
        savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
        viewModelStoreOwner = LocalViewModelStoreOwner.current
            ?: throw ComponentException("Cannot find current LocalViewModelStoreOwner"),
        lifecycleOwner = LocalLifecycleOwner.current,
        storedPaymentMethod = storedPaymentMethod,
        configuration = configuration,
        application = LocalContext.current.applicationContext as Application,
        componentCallback = callback,
        order = order,
        key = key,
    )
}

// TODO docs
@Composable
fun <
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>,
    ComponentCallbackT : SessionComponentCallback<ComponentStateT>
    > SessionPaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT, ComponentCallbackT>.get(
    checkoutSession: CheckoutSession,
    paymentMethod: PaymentMethod,
    configuration: ConfigurationT,
    callback: ComponentCallbackT,
    key: String? = null,
): ComponentT {
    return get(
        savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
        viewModelStoreOwner = LocalViewModelStoreOwner.current
            ?: throw ComponentException("Cannot find current LocalViewModelStoreOwner"),
        lifecycleOwner = LocalLifecycleOwner.current,
        checkoutSession = checkoutSession,
        paymentMethod = paymentMethod,
        configuration = configuration,
        application = LocalContext.current.applicationContext as Application,
        componentCallback = callback,
        key = key,
    )
}

// TODO docs
@Composable
fun <
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>,
    ComponentCallbackT : SessionComponentCallback<ComponentStateT>
    > SessionStoredPaymentComponentProvider<ComponentT, ConfigurationT, ComponentStateT, ComponentCallbackT>.get(
    checkoutSession: CheckoutSession,
    storedPaymentMethod: StoredPaymentMethod,
    configuration: ConfigurationT,
    callback: ComponentCallbackT,
    key: String? = null,
): ComponentT {
    return get(
        savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
        viewModelStoreOwner = LocalViewModelStoreOwner.current
            ?: throw ComponentException("Cannot find current LocalViewModelStoreOwner"),
        lifecycleOwner = LocalLifecycleOwner.current,
        checkoutSession = checkoutSession,
        storedPaymentMethod = storedPaymentMethod,
        configuration = configuration,
        application = LocalContext.current.applicationContext as Application,
        componentCallback = callback,
        key = key,
    )
}

// TODO docs
@Composable
fun <T> AdyenComponentViewComposable(component: T) where T : ViewableComponent, T : Component {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory = {
        AdyenComponentView(it).apply {
            attach(component, lifecycleOwner)
        }
    })
}

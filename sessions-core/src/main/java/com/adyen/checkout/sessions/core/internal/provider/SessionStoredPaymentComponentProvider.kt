/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/1/2023.
 */

package com.adyen.checkout.sessions.core.internal.provider

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.util.requireApplication
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionComponentCallback

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SessionStoredPaymentComponentProvider<
    ComponentT : PaymentComponent,
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<*>,
    ComponentCallbackT : SessionComponentCallback<ComponentStateT>
    > {

    /**
     * Get a [PaymentComponent]  with a stored payment method and a checkout session. You only need to integrate with
     * the /sessions endpoint to create a session and the component will automatically handle the rest of the payment
     * flow.
     *
     * @param fragment          The Fragment to associate the lifecycle.
     * @param checkoutSession   The [CheckoutSession] object to launch this component.
     * @param storedPaymentMethod   The corresponding  [StoredPaymentMethod] object.
     * @param configuration     The Configuration of the component.
     * @param componentCallback The callback to handle events from the [PaymentComponent].
     * @param key               The key to use to identify the [PaymentComponent].
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        fragment: Fragment,
        checkoutSession: CheckoutSession,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        componentCallback: ComponentCallbackT,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            checkoutSession = checkoutSession,
            storedPaymentMethod = storedPaymentMethod,
            configuration = configuration,
            application = fragment.requireApplication(),
            componentCallback = componentCallback,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent]  with a stored payment method and a checkout session. You only need to integrate with
     * the /sessions endpoint to create a session and the component will automatically handle the rest of the payment
     * flow.
     *
     * @param activity      The Activity to associate the lifecycle.
     * @param checkoutSession   The [CheckoutSession] object to launch this component.
     * @param storedPaymentMethod   The corresponding  [StoredPaymentMethod] object.
     * @param configuration     The Configuration of the component.
     * @param componentCallback The callback to handle events from the [PaymentComponent].
     * @param key               The key to use to identify the [PaymentComponent].
     *
     * NOTE: By default only one [PaymentComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [PaymentComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        activity: ComponentActivity,
        checkoutSession: CheckoutSession,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        componentCallback: ComponentCallbackT,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            checkoutSession = checkoutSession,
            storedPaymentMethod = storedPaymentMethod,
            configuration = configuration,
            application = activity.application,
            componentCallback = componentCallback,
            key = key,
        )
    }

    /**
     * Get a [PaymentComponent]  with a stored payment method and a checkout session. You only need to integrate with
     * the /sessions endpoint to create a session and the component will automatically handle the rest of the payment
     * flow.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param lifecycleOwner          The lifecycle owner, normally an Activity or Fragment.
     * @param checkoutSession         The [CheckoutSession] object to launch this component.
     * @param storedPaymentMethod     The corresponding  [StoredPaymentMethod] object.
     * @param configuration           The Configuration of the component.
     * @param application             Your main application class.
     * @param componentCallback       The callback to handle events from the [PaymentComponent].
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
        checkoutSession: CheckoutSession,
        storedPaymentMethod: StoredPaymentMethod,
        configuration: ConfigurationT,
        application: Application,
        componentCallback: ComponentCallbackT,
        key: String?,
    ): ComponentT
}

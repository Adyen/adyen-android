/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */
package com.adyen.checkout.components.core.internal.provider

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.util.requireApplication

interface ActionComponentProvider<
    ComponentT : ActionComponent,
    ConfigurationT : Configuration,
    DelegateT : ActionDelegate
    > : ComponentProvider<ComponentT> {

    //region CheckoutConfiguration

    /**
     * Get an [ActionComponent].
     *
     * @param fragment              The Fragment to associate the lifecycle.
     * @param checkoutConfiguration The [CheckoutConfiguration].
     * @param callback              The callback to handle events from the [ActionComponent].
     * @param key                   The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    fun get(
        fragment: Fragment,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            application = fragment.requireApplication(),
            checkoutConfiguration = checkoutConfiguration,
            callback = callback,
            key = key,
        )
    }

    /**
     * Get an [ActionComponent].
     *
     * @param activity              The Activity to associate the lifecycle.
     * @param checkoutConfiguration The [CheckoutConfiguration].
     * @param callback              The callback to handle events from the [ActionComponent].
     * @param key                   The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    fun get(
        activity: ComponentActivity,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            application = activity.application,
            checkoutConfiguration = checkoutConfiguration,
            callback = callback,
            key = key,
        )
    }

    /**
     * Get an [ActionComponent].
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param lifecycleOwner          The lifecycle owner, normally an Activity or Fragment.
     * @param application             Your main application class.
     * @param checkoutConfiguration   The [CheckoutConfiguration].
     * @param callback                The callback to handle events from the [ActionComponent].
     * @param key                     The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getDelegate(
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application
    ): DelegateT

    //endregion

    //region Component specific configuration

    /**
     * Get an [ActionComponent].
     *
     * @param fragment      The Fragment to associate the lifecycle.
     * @param configuration The Configuration of the component.
     * @param callback      The callback to handle events from the [ActionComponent].
     * @param key           The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    fun get(
        fragment: Fragment,
        configuration: ConfigurationT,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            application = fragment.requireApplication(),
            configuration = configuration,
            callback = callback,
            key = key,
        )
    }

    /**
     * Get an [ActionComponent].
     *
     * @param activity      The Activity to associate the lifecycle.
     * @param configuration The Configuration of the component.
     * @param callback      The callback to handle events from the [ActionComponent].
     * @param key           The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    fun get(
        activity: ComponentActivity,
        configuration: ConfigurationT,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            application = activity.application,
            configuration = configuration,
            callback = callback,
            key = key,
        )
    }

    /**
     * Get an [ActionComponent].
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param lifecycleOwner          The lifecycle owner, normally an Activity or Fragment.
     * @param application             Your main application class.
     * @param configuration           The Configuration of the component.
     * @param callback                The callback to handle events from the [ActionComponent].
     * @param key                     The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: ConfigurationT,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT

    // TODO: is this still needed?
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getDelegate(
        configuration: ConfigurationT,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): DelegateT

    //endregion

    /**
     * Checks if the provided component can handle a given action.
     *
     * @param action The Action object from the API response.
     * @return If the action can be handled by this component.
     */
    fun canHandleAction(action: Action): Boolean

    /**
     * @return the list of supported action types
     */
    val supportedActionTypes: List<String>

    /**
     * Checks if the provided component will trigger any updates through the [ActionComponentCallback] or not.
     * If false, no events will be fired and you don't need to make a /payments/details API call. The component only
     * serves to present some final data to the shopper.
     *
     * @return If the provided component provides details to make an API call to the /payments/details endpoint.
     */
    fun providesDetails(action: Action): Boolean
}

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
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.ActionComponentCallback
import com.adyen.checkout.components.core.internal.Component
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.ui.ActionDelegate

// TODO SESSIONS: DOCS
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionComponentProvider<
    ComponentT : ActionComponent,
    ConfigurationT : Configuration,
    DelegateT : ActionDelegate
    > : ComponentProvider<ComponentT> {

    /**
     * Get an [ActionComponent].
     *
     * @param fragment      The Fragment to associate the lifecycle.
     * @param application   Your main application class.
     * @param configuration The Configuration of the component.
     * @param callback      Th callback to handle events from the [ActionComponent].
     * @param key           The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    operator fun get(
        fragment: Fragment,
        application: Application,
        configuration: ConfigurationT,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = fragment,
            viewModelStoreOwner = fragment,
            lifecycleOwner = fragment.viewLifecycleOwner,
            application = application,
            configuration = configuration,
            callback = callback,
            key = key
        )
    }

    /**
     * Get an [ActionComponent].
     *
     * @param activity      The Activity to associate the lifecycle.
     * @param application   Your main application class.
     * @param configuration The Configuration of the component.
     * @param callback      The callback to handle events from the [ActionComponent].
     * @param key           The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    operator fun <T> get(
        activity: ComponentActivity,
        application: Application,
        configuration: ConfigurationT,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT {
        return get(
            savedStateRegistryOwner = activity,
            viewModelStoreOwner = activity,
            lifecycleOwner = activity,
            application = application,
            configuration = configuration,
            callback = callback,
            key = key
        )
    }

    /**
     * Get an [ActionComponent].
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param application             Your main application class.
     * @param configuration           The Configuration of the component.
     * @param key                     The key to use to identify the [ActionComponent].
     *
     * NOTE: By default only one [ActionComponent] will be created per lifecycle. Use [key] in case you need to
     * instantiate multiple [ActionComponent]s in the same lifecycle.
     *
     * @return The Component
     */
    @Suppress("LongParameterList")
    operator fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleOwner: LifecycleOwner,
        application: Application,
        configuration: ConfigurationT,
        callback: ActionComponentCallback,
        key: String? = null,
    ): ComponentT

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getDelegate(
        configuration: ConfigurationT,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): DelegateT

    /**
     * Checks if the provided component can handle the specific action type.
     *
     * @param action The Action object from the API response.
     * @return If the action can be handled by this component.
     */
    fun canHandleAction(action: Action): Boolean

    /**
     * @return the list of supported action types
     */
    val supportedActionTypes: List<String>

    // TODO check docs
    /**
     * Checks if the provided component will trigger updates that can be observed using
     * [Component.observe]. If returns false, no events will be fired.
     *
     * @return If the provided component provides details to make an API call to /payments/details end point.
     */
    // TODO move to component?
    fun providesDetails(action: Action): Boolean
}

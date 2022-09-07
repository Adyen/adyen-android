/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */
package com.adyen.checkout.components

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.response.Action

interface ActionComponentProvider<
    ComponentT : ActionComponent<out Configuration>,
    ConfigurationT : Configuration,
    DelegateT : ActionDelegate<*>
    > : ComponentProvider<ComponentT> {

    /**
     * Get an [ActionComponent].
     *
     * @param owner         The Activity or Fragment to associate the lifecycle.
     * @param application   Your main application class.
     * @param configuration The Configuration of the component.
     * @return The Component
     */
    operator fun <T> get(
        owner: T,
        application: Application,
        configuration: ConfigurationT
    ): ComponentT where T : SavedStateRegistryOwner, T : ViewModelStoreOwner

    /**
     * Get an [ActionComponent].
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param application             Your main application class.
     * @param configuration           The Configuration of the component.
     * @param defaultArgs             Values from this `Bundle` will be used as defaults by [SavedStateHandle] passed in [ViewModel]
     *                                if there is no previously saved state or previously saved state misses a value by such key.
     * @return The Component
     */
    operator fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: ConfigurationT,
        defaultArgs: Bundle?
    ): ComponentT

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getDelegate(
        configuration: ConfigurationT,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): DelegateT

    /**
     * @return If the Configuration is required for this Component.
     */
    @Deprecated(
        """You can safely remove this method, it will always return true as all action components require
            | a configuration."""
    )
    fun requiresConfiguration(): Boolean

    /**
     * Checks if the provided component can handle the specific action type.
     *
     * @param action The Action object from the API response.
     * @return If the action can be handled by this component.
     */
    fun canHandleAction(action: Action): Boolean

    /**
     * @return If a view is required to handle this action.
     */
    fun requiresView(action: Action): Boolean

    /**
     * @return the list of supported action types
     */
    val supportedActionTypes: List<String>

    /**
     * Checks if the provided component will trigger updates that can be observed using
     * [Component.observe]. If returns false, no events will be fired.
     *
     * @return If the provided component provides details to make an API call to /payments/details end point.
     */
    fun providesDetails(): Boolean
}

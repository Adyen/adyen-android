/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.components;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistryOwner;

import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.components.model.payments.response.Action;

import java.util.List;

public interface ActionComponentProvider<ComponentT extends ActionComponent, ConfigurationT extends Configuration>
        extends ComponentProvider<ComponentT> {
    /**
     * Get an {@link ActionComponent}.
     *
     * @param owner         The Activity or Fragment to associate the lifecycle.
     * @param application   Your main application class.
     * @param configuration The Configuration of the component.
     * @return The Component
     */
    @SuppressWarnings("LambdaLast")
    @NonNull
    <T extends SavedStateRegistryOwner & ViewModelStoreOwner> ComponentT get(
            @NonNull T owner,
            @NonNull Application application,
            @NonNull ConfigurationT configuration
    );

    /**
     * Get an {@link ActionComponent}.
     *
     * @param savedStateRegistryOwner The owner of the SavedStateRegistry, normally an Activity or Fragment.
     * @param viewModelStoreOwner     A scope that owns ViewModelStore, normally an Activity or Fragment.
     * @param application             Your main application class.
     * @param configuration           The Configuration of the component.
     * @param defaultArgs             Values from this {@code Bundle} will be used as defaults by {@link SavedStateHandle} passed in {@link ViewModel
     *                                ViewModels} if there is no previously saved state or previously saved state misses a value by such key
     * @return The Component
     */
    @SuppressWarnings("LambdaLast")
    @NonNull
    ComponentT get(
            @NonNull SavedStateRegistryOwner savedStateRegistryOwner,
            @NonNull ViewModelStoreOwner viewModelStoreOwner,
            @NonNull Application application,
            @NonNull ConfigurationT configuration,
            @Nullable Bundle defaultArgs
    );

    /**
     * @return If the Configuration is required for this Component.
     * @deprecated You can safely remove this method, it will always return true as all action components require a configuration.
     */
    @Deprecated
    boolean requiresConfiguration();

    /**
     * Checks if the provided component can handle the specific action type.
     *
     * @param action The Action object from the API response.
     * @return If the action can be handled by this component.
     */
    boolean canHandleAction(@NonNull Action action);

    /**
     * @return If a view is required to handle this action.
     */
    boolean requiresView(@NonNull Action action);

    /**
     * @return the list of supported action types
     */
    @NonNull
    List<String> getSupportedActionTypes();

    /**
     * Checks if the provided component will trigger updates that can be observed using
     * {@link Component#observe(LifecycleOwner, Observer)}. If returns false, no events will be fired.
     *
     * @return If the provided component provides details to make an API call to /payments/details end point.
     */
    boolean providesDetails();
}

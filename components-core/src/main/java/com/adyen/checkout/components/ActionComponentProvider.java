/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.components;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStoreOwner;

import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.components.model.payments.response.Action;

import java.util.List;

public interface ActionComponentProvider<ComponentT extends ActionComponent, ConfigurationT extends Configuration>
        extends ComponentProvider<ComponentT> {
    /**
     * Get an {@link ActionComponent}.
     *
     * @param viewModelStoreOwner The Activity or Fragment to associate the lifecycle.
     * @param configuration       The Configuration of the component. Can be null in most cases.
     * @return The Component
     */
    @SuppressWarnings("LambdaLast")
    @NonNull
    ComponentT get(@NonNull ViewModelStoreOwner viewModelStoreOwner, @NonNull Application application, @NonNull ConfigurationT configuration);

    /**
     * @return If the Configuration is required for this Component.
     */
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
}

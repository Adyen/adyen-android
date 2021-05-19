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

public interface ActionComponentProvider<ComponentT extends ActionComponent, ConfigurationT extends Configuration>
        extends ComponentProvider<ComponentT> {
    /**
     * Get an {@link ActionComponent}.
     *
     * @param viewModelStoreOwner The Activity or Fragment to associate the lifecycle.
     * @param configuration The Configuration of the component. Can be null in most cases.
     * @return The Component
     */
    @SuppressWarnings("LambdaLast")
    @NonNull
    ComponentT get(@NonNull ViewModelStoreOwner viewModelStoreOwner, @NonNull Application application, @NonNull ConfigurationT configuration);

    /**
     * @return If the Configuration is required for this Component.
     */
    boolean requiresConfiguration();
}

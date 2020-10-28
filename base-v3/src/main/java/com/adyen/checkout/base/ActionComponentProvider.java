/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.adyen.checkout.base.component.Configuration;

public interface ActionComponentProvider<ComponentT extends ActionComponent> extends ComponentProvider<ComponentT> {

    /**
     * Get an {@link ActionComponent}.
     *
     * @param activity The Activity to associate lifecycle.
     * @return The Component
     * @deprecated in favor of{@link #get(FragmentActivity, Configuration)} with a possible configuration.
     */
    @Deprecated
    @NonNull
    ComponentT get(@NonNull FragmentActivity activity);

    /**
     * Get an {@link ActionComponent}.
     *
     * @param fragment The Fragment to associate lifecycle.
     * @return The Component
     * @deprecated in favor of{@link #get(Fragment, Configuration)} with a possible configuration.
     */
    @Deprecated
    @NonNull
    ComponentT get(@NonNull Fragment fragment);

    /**
     * Get an {@link ActionComponent}.
     *
     * @param activity The Activity to associate lifecycle.
     * @param configuration The Configuration of the component. Can be null in most cases.
     * @return The Component
     */
    @NonNull
    ComponentT get(@NonNull FragmentActivity activity, @Nullable Configuration configuration);


    /**
     * Get an {@link ActionComponent}.
     *
     * @param fragment The Fragment to associate lifecycle.
     * @param configuration The Configuration of the component. Can be null in most cases.
     * @return The Component
     */
    @NonNull
    ComponentT get(@NonNull Fragment fragment, @Nullable Configuration configuration);

    /**
     * @return If the Configuration is required for this Component.
     */
    boolean requiresConfiguration();
}

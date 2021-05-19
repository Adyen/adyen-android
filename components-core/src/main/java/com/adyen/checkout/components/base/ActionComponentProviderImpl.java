/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.components.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.adyen.checkout.components.ActionComponentProvider;
import com.adyen.checkout.components.base.lifecycle.ActionComponentViewModel;
import com.adyen.checkout.components.base.lifecycle.ActionComponentViewModelFactory;

public class ActionComponentProviderImpl<ConfigurationT extends Configuration, ComponentT extends ActionComponentViewModel<ConfigurationT>>
        implements ActionComponentProvider<ComponentT, ConfigurationT> {

    private final Class<ComponentT> mComponentClass;
    private final Class<ConfigurationT> mConfigurationClass;
    private final boolean mRequiresConfiguration;

    public ActionComponentProviderImpl(
            @NonNull Class<ComponentT> componentClass,
            @NonNull Class<ConfigurationT> configurationClass
    ) {
        this(componentClass, configurationClass, false);
    }

    /**
     * Default Constructor.
     *
     * @param componentClass The Class of the Component
     * @param configurationClass The Class of the Configuration
     * @param requiresConfiguration If this Component requires a Configuration to be initialized.
     */
    public ActionComponentProviderImpl(
            @NonNull Class<ComponentT> componentClass,
            @NonNull Class<ConfigurationT> configurationClass,
            boolean requiresConfiguration
    ) {
        mComponentClass = componentClass;
        mConfigurationClass = configurationClass;
        mRequiresConfiguration = requiresConfiguration;
    }

    @NonNull
    @Override
    @SuppressWarnings("LambdaLast")
    public ComponentT get(@NonNull ViewModelStoreOwner viewModelStoreOwner, @NonNull Application application, @NonNull Configuration configuration) {
        final ActionComponentViewModelFactory factory = new ActionComponentViewModelFactory(application, mConfigurationClass, configuration);
        return new ViewModelProvider(viewModelStoreOwner, factory).get(mComponentClass);
    }

    @Override
    public boolean requiresConfiguration() {
        return mRequiresConfiguration;
    }

}

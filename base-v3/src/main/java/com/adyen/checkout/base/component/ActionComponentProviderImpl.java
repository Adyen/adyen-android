/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.base.component;

import android.app.Activity;
import android.app.Application;
import androidx.lifecycle.ViewModelProviders;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.adyen.checkout.base.ActionComponentProvider;
import com.adyen.checkout.base.component.lifecycle.ActionComponentViewModel;
import com.adyen.checkout.base.component.lifecycle.ActionComponentViewModelFactory;
import com.adyen.checkout.core.exception.ComponentException;

public class ActionComponentProviderImpl<ConfigurationT extends Configuration, ComponentT extends ActionComponentViewModel<ConfigurationT>>
        implements ActionComponentProvider<ComponentT> {

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
    public ComponentT get(@NonNull FragmentActivity activity) {
        if (requiresConfiguration()) {
            throw new ComponentException("This Component requires a Configuration object to be initialized.");
        }

        return get(activity, null);
    }

    @NonNull
    @Override
    public ComponentT get(@NonNull Fragment fragment) {
        if (requiresConfiguration()) {
            throw new ComponentException("This Component requires a Configuration object to be initialized.");
        }

        return get(fragment, null);
    }

    @NonNull
    @Override
    public ComponentT get(@NonNull FragmentActivity activity, @Nullable Configuration configuration) {
        final ActionComponentViewModelFactory factory =
                new ActionComponentViewModelFactory(checkApplication(activity), mConfigurationClass, configuration);
        return ViewModelProviders.of(activity, factory).get(mComponentClass);
    }

    @NonNull
    @Override
    public ComponentT get(@NonNull Fragment fragment, @Nullable Configuration configuration) {
        final ActionComponentViewModelFactory factory =
                new ActionComponentViewModelFactory(checkApplication(fragment.getActivity()), mConfigurationClass, configuration);
        return ViewModelProviders.of(fragment, factory).get(mComponentClass);
    }

    @Override
    public boolean requiresConfiguration() {
        return mRequiresConfiguration;
    }

    @NonNull
    private static Application checkApplication(@Nullable Activity activity) {
        if (activity != null) {
            final Application application = activity.getApplication();
            if (application != null) {
                return application;
            }
        }
        throw new IllegalStateException("Your activity/fragment is not yet attached to "
                + "Application. You can't request ViewModel before onCreate call.");
    }
}

/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/8/2020.
 */

package com.adyen.checkout.base.component.lifecycle;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.Configuration;

public final class ActionComponentViewModelFactory implements ViewModelProvider.Factory {

    private final  Class<?> mConfigurationClass;
    private final Configuration mConfiguration;
    private final Application mApplication;

    /**
     * Creates a {@code AndroidViewModelFactory}.
     *
     * @param configuration a {@link Configuration} to pass in {@link PaymentComponentViewModel}
     */
    public ActionComponentViewModelFactory(
            @NonNull Application application,
            @NonNull Class<?> configurationClass,
            @Nullable Configuration configuration
    ) {
        mApplication = application;
        mConfigurationClass = configurationClass;
        mConfiguration = configuration;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            return modelClass.getConstructor(Application.class, mConfigurationClass).newInstance(mApplication, mConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of component: " + modelClass, e);
        }
    }
}

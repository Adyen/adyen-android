/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/8/2020.
 */

package com.adyen.checkout.components.base.lifecycle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.ActionComponent;
import com.adyen.checkout.components.base.Configuration;

/**
 * Base class of an ActionComponent as a ViewModel.
 *
 * @param <ConfigurationT> A Configuration object although optional is required to construct a Component.
 */
public abstract class ActionComponentViewModel<ConfigurationT extends Configuration>
        extends AndroidViewModel
        implements ActionComponent<ConfigurationT> {

    private final ConfigurationT mConfiguration;
    private final SavedStateHandle mSavedStateHandle;

    public ActionComponentViewModel(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull Application application,
            @NonNull ConfigurationT configuration
    ) {
        super(application);
        mConfiguration = configuration;
        mSavedStateHandle = savedStateHandle;
    }

    @NonNull
    public ConfigurationT getConfiguration() {
        return mConfiguration;
    }

    @NonNull
    public SavedStateHandle getSavedStateHandle() {
        return mSavedStateHandle;
    }
}

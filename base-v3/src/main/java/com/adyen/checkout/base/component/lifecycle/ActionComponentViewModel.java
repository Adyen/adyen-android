/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/8/2020.
 */

package com.adyen.checkout.base.component.lifecycle;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.ActionComponent;
import com.adyen.checkout.base.component.Configuration;

/**
 * Base class of an ActionComponent as a ViewModel.
 *
 * @param <ConfigurationT> A Configuration object although optional is required to construct a Component.
 */
public abstract class ActionComponentViewModel<ConfigurationT extends Configuration>
        extends AndroidViewModel
        implements ActionComponent<ConfigurationT> {

    private final ConfigurationT mConfiguration;

    public ActionComponentViewModel(@NonNull Application application, @Nullable ConfigurationT configuration) {
        super(application);
        mConfiguration = configuration;
    }

    @Nullable
    public ConfigurationT getConfiguration() {
        return mConfiguration;
    }
}

/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */

package com.adyen.checkout.base;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.OutputData;

/**
 * A {@link Component} that has an associated View to show or interact with the shopper.
 *
 * @param <OutputDataT> The OutputData that holds the View state data.
 * @param <ConfigurationT> The Configuration used in the base Component.
 * @param <ComponentStateT> The ComponentState used in the base Component.
 */
public interface ViewableComponent<OutputDataT extends OutputData, ConfigurationT extends Configuration, ComponentStateT>
        extends Component<ComponentStateT, ConfigurationT> {

    /**
     * Observe changes in the UI state of this Component.
     * <p/>
     * WARNING: Do not use this method. This method was only made public as part of code refactoring and is treated as internal code
     *     that might change without notice.
     *
     * @param lifecycleOwner The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    void observeOutputData(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<OutputDataT> observer);

    /**
     * WARNING: Do not use this method. This method was only made public as part of code refactoring and is treated as internal code
     *          that might change without notice.
     *
     * @return The current UI state of this Component.
     */
    @Nullable
    OutputDataT getOutputData();

    /**
     * Send an analytic event about the Component being shown to the user.
     *
     * @param context The context where the component is.
     */
    void sendAnalyticsEvent(@NonNull Context context);
}

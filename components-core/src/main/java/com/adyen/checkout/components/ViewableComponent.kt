/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.components

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.OutputData

/**
 * A [Component] that has an associated View to show or interact with the shopper.
 *
 * @param <OutputDataT> The OutputData that holds the View state data.
 * @param <ConfigurationT> The Configuration used in the base Component.
 * @param <ComponentStateT> The ComponentState used in the base Component.
 */
interface ViewableComponent<OutputDataT : OutputData, ConfigurationT : Configuration, ComponentStateT> :
    Component<ComponentStateT, ConfigurationT> {
    /**
     * Observe changes in the UI state of this Component.
     *
     *
     * WARNING: Do not use this method. This method was only made public as part of code refactoring and is treated as internal code
     * that might change without notice.
     *
     * @param lifecycleOwner The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<OutputDataT>)

    /**
     * WARNING: Do not use this method. This method was only made public as part of code refactoring and is treated as internal code
     * that might change without notice.
     *
     * @return The current UI state of this Component.
     */
    val outputData: OutputDataT?

    /**
     * Send an analytic event about the Component being shown to the user.
     *
     * @param context The context where the component is.
     */
    fun sendAnalyticsEvent(context: Context)
}

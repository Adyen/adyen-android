/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.components

import android.content.Context
import com.adyen.checkout.components.base.Configuration

/**
 * A [Component] that has an associated View to show or interact with the shopper.
 *
 * @param <ConfigurationT> The Configuration used in the base Component.
 * @param <ComponentStateT> The ComponentState used in the base Component.
 */
interface ViewableComponent<ConfigurationT : Configuration, ComponentStateT> :
    Component<ComponentStateT, ConfigurationT> {

    /**
     * Send an analytic event about the Component being shown to the user.
     *
     * @param context The context where the component is.
     */
    fun sendAnalyticsEvent(context: Context)
}

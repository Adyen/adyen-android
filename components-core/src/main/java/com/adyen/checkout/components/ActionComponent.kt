/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */
package com.adyen.checkout.components

import android.app.Activity
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.response.Action

/**
 * A component that handles an "action" to be taken from the payments/ API result.
 *
 *
 *
 * Result on the [ActionComponentData] if populated can be considered valid to be sent back to the payments/details/ API
 */
interface ActionComponent<ConfigurationT : Configuration> : Component<ActionComponentData, ConfigurationT> {
    /**
     * Provide the action from the API response that needs to be handled.
     *
     * @param activity The Activity starting the action.
     * @param action The parsed object from the API of the action to be taken.
     */
    fun handleAction(activity: Activity, action: Action)

    /**
     * Checks if this component can handle the specific action type.
     *
     * @param action The Action object from the API response.
     * @return If the action can be handled by this component.
     */
    fun canHandleAction(action: Action): Boolean
}

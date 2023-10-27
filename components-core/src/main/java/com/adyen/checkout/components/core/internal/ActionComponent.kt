/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */
package com.adyen.checkout.components.core.internal

import android.app.Activity
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action

/**
 * A component that handles an "action" to be taken from the result of the /payments API call.
 *
 * If an [ActionComponentData] is emitted from this component, it should be sent back through the /payments/details API
 * call.
 */
interface ActionComponent : Component {

    /**
     * Provide the action from the API response that needs to be handled.
     *
     * @param action The parsed object from the API of the action to be taken.
     * @param activity The Activity starting the action.
     */
    fun handleAction(action: Action, activity: Activity)

    /**
     * Checks if this component can handle the specific action type.
     *
     * @param action The Action object from the API response.
     * @return If the action can be handled by this component.
     */
    fun canHandleAction(action: Action): Boolean
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2022.
 */

package com.adyen.checkout.action.core.internal

import android.app.Activity
import android.content.Intent
import com.adyen.checkout.components.core.action.Action

interface ActionHandlingComponent {

    /**
     * Checks if this component can handle the specific action type.
     *
     * @param action The Action object from the API response.
     * @return If the action can be handled by this component.
     */
    fun canHandleAction(action: Action): Boolean

    /**
     * Provide the action from the API response that needs to be handled.
     *
     * @param action The parsed object from the API of the action to be taken.
     * @param activity The Activity starting the action.
     */
    fun handleAction(action: Action, activity: Activity)

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    fun handleIntent(intent: Intent)

    /**
     * Set a callback that will be called when a redirect is opened.
     *
     * @param listener The callback that will be called on redirect.
     */
    fun setOnRedirectListener(listener: () -> Unit)
}

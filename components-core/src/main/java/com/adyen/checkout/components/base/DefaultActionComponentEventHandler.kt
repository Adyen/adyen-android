/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.components.base

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultActionComponentEventHandler(
    private val actionComponentCallback: ActionComponentCallback
) : ActionComponentEventHandler {

    override fun onActionComponentEvent(event: ActionComponentEvent) {
        Logger.v(TAG, "Event received $event")
        when (event) {
            is ActionComponentEvent.ActionDetails -> actionComponentCallback.onAdditionalDetails(event.data)
            is ActionComponentEvent.Error -> actionComponentCallback.onError(event.error)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}

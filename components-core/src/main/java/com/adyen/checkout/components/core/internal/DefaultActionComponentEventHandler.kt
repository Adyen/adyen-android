/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultActionComponentEventHandler(
    private val actionComponentCallback: ActionComponentCallback
) : ActionComponentEventHandler {

    override fun onActionComponentEvent(event: ActionComponentEvent) {
        adyenLog(AdyenLogLevel.VERBOSE) { "Event received $event" }
        when (event) {
            is ActionComponentEvent.ActionDetails -> actionComponentCallback.onAdditionalDetails(event.data)
            is ActionComponentEvent.Error -> actionComponentCallback.onError(event.error)
            is ActionComponentEvent.PermissionRequest -> actionComponentCallback.onPermissionRequest(
                event.requiredPermission,
                event.permissionCallback,
            )
        }
    }
}

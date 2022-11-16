/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2022.
 */

package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ActionHandlingComponent
import com.adyen.checkout.components.ActionHandlingDelegate
import com.adyen.checkout.components.model.payments.response.Action
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultActionHandlingComponent(
    private val actionDelegate: ActionHandlingDelegate
) : ActionHandlingComponent {

    override fun handleAction(action: Action, activity: Activity) {
        actionDelegate.handleAction(action, activity)
    }

    override fun handleIntent(intent: Intent) {
        actionDelegate.handleIntent(intent)
    }

    override fun observeAction(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit,
    ) {
        actionDelegate.observeAction(lifecycleOwner, coroutineScope, callback)
    }

    override fun removeActionObserver() {
        actionDelegate.removeActionObserver()
    }
}

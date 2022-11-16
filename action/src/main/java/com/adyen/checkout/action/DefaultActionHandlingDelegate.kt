/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2022.
 */

package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ActionHandlingDelegate
import com.adyen.checkout.components.ComponentViewType
import com.adyen.checkout.components.lifecycle.repeatOnResume
import com.adyen.checkout.components.model.payments.response.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultActionHandlingDelegate(
    override val actionDelegate: GenericActionDelegate,
) : ActionHandlingDelegate {

    override val viewFlow: Flow<ComponentViewType?> = actionDelegate.viewFlow

    override fun initializeActionHandling(coroutineScope: CoroutineScope) {
        actionDelegate.initialize(coroutineScope)
    }

    override fun handleAction(action: Action, activity: Activity) {
        actionDelegate.handleAction(action, activity)
    }

    override fun handleIntent(intent: Intent) {
        actionDelegate.handleIntent(intent)
    }

    override fun observeAction(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        actionDelegate.observe(lifecycleOwner, coroutineScope, callback)

        lifecycleOwner.repeatOnResume { actionDelegate.refreshStatus() }
    }

    override fun removeActionObserver() {
        actionDelegate.removeObserver()
    }

    override fun onClearedActionHandling() {
        actionDelegate.onCleared()
    }
}

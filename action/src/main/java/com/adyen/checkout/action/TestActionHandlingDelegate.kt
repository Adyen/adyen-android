/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/11/2022.
 */

package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ActionHandlingDelegate
import com.adyen.checkout.components.ComponentViewType
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.model.payments.response.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@RestrictTo(RestrictTo.Scope.TESTS)
class TestActionHandlingDelegate : ActionHandlingDelegate {

    override val actionDelegate: ActionDelegate
        get() {
            throw NotImplementedError("This should not be called in tests.")
        }

    override val viewFlow: Flow<ComponentViewType?> = flowOf(null)

    override fun initializeActionHandling(coroutineScope: CoroutineScope) = Unit

    override fun handleAction(action: Action, activity: Activity) = Unit

    override fun handleIntent(intent: Intent) = Unit

    override fun observeAction(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) = Unit

    override fun removeActionObserver() = Unit

    override fun onClearedActionHandling() = Unit
}

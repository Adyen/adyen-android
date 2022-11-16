/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2022.
 */

package com.adyen.checkout.components

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.model.payments.response.Action
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionHandlingComponent {

    fun handleAction(action: Action, activity: Activity)

    fun handleIntent(intent: Intent)

    fun observeAction(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit,
    )

    fun removeActionObserver()
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/8/2022.
 */

package com.adyen.checkout.components.core.internal.ui

import android.app.Activity
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.core.old.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionDelegate : ComponentDelegate {

    val exceptionFlow: Flow<CheckoutException>

    fun handleAction(action: Action, activity: Activity)

    /**
     * Override this method if you need to emit to the [exceptionFlow] from outside of this class.
     */
    fun onError(e: CheckoutException) = Unit

    fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    )

    fun removeObserver()
}

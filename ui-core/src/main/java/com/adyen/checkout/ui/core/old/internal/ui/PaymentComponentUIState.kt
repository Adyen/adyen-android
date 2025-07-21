/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class PaymentComponentUIState {
    /**
     * User interaction is blocked because the component is being submitted.
     */
    object Blocked : PaymentComponentUIState()

    /**
     * Intermediate state after a submit was requested but the component is not yet ready.
     */
    object PendingSubmit : PaymentComponentUIState()

    /**
     * User can interact with the component and fill the input fields.
     */
    object Idle : PaymentComponentUIState()

    fun isInteractionBlocked(): Boolean {
        return this is Blocked || this is PendingSubmit
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class PaymentComponentUIEvent {
    object InvalidUI : PaymentComponentUIEvent()
}

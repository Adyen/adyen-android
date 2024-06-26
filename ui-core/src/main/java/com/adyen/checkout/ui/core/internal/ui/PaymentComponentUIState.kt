/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.ui.core.internal.ui

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
abstract class PaymentComponentUIEvent {
    object InvalidUI : PaymentComponentUIEvent()

    object StateUpdated : PaymentComponentUIEvent()
}

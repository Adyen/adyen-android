/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.components.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class PaymentComponentUIState {
    object Loading : PaymentComponentUIState()
    object Idle : PaymentComponentUIState()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class PaymentComponentUIEvent {
    object InvalidUI : PaymentComponentUIEvent()
}

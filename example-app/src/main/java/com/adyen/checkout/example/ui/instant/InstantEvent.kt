/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/1/2023.
 */

package com.adyen.checkout.example.ui.instant

import com.adyen.checkout.components.core.action.Action

internal sealed class InstantEvent {

    data class PaymentResult(val result: String) : InstantEvent()

    data class AdditionalAction(val action: Action) : InstantEvent()
}

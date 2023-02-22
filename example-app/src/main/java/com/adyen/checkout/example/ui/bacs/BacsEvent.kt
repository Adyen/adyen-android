/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/1/2023.
 */

package com.adyen.checkout.example.ui.bacs

import com.adyen.checkout.components.core.action.Action

internal sealed class BacsEvent {

    data class PaymentResult(val result: String) : BacsEvent()

    data class AdditionalAction(val action: Action) : BacsEvent()
}

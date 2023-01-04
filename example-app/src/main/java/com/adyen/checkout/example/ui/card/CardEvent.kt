/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/11/2022.
 */

package com.adyen.checkout.example.ui.card

import com.adyen.checkout.components.model.payments.response.Action

internal sealed class CardEvent {

    data class PaymentResult(val result: String) : CardEvent()

    data class AdditionalAction(val action: Action) : CardEvent()

    object Invalid : CardEvent()
}

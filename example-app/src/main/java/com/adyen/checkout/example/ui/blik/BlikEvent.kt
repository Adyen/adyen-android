/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 18/11/2022.
 */

package com.adyen.checkout.example.ui.blik

sealed class BlikEvent {

    data class PaymentResult(val result: String) : BlikEvent()

    data class AdditionalAction(val action: BlikAction) : BlikEvent()

    object Invalid : BlikEvent()
}

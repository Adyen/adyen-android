/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/11/2022.
 */

package com.adyen.checkout.example.ui.mobilesummercard

internal sealed class MSCardEvent {

    data class PaymentFinished(val result: String) : MSCardEvent()

    data class Error(val result: String) : MSCardEvent()
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/8/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod

internal object PaymentMethodFormatter {

    private const val CARD_LOGO = "card"

    fun getIcon(paymentMethod: PaymentMethod): String {
        return when (paymentMethod) {
            is CardPaymentMethod -> CARD_LOGO
            is GiftCardPaymentMethod -> paymentMethod.brand
            else -> paymentMethod.type
        }
    }
}

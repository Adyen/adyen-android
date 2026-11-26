/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/8/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.common.internal.ui.CARD_LOGO_TX_VARIANT
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.GiftCardPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.RegularPaymentMethodResponse

internal object PaymentMethodFormatter {

    fun getIcon(paymentMethod: RegularPaymentMethodResponse): String {
        return when (paymentMethod) {
            is CardPaymentMethodResponse -> CARD_LOGO_TX_VARIANT
            is GiftCardPaymentMethodResponse -> paymentMethod.brand
            else -> paymentMethod.type
        }
    }
}

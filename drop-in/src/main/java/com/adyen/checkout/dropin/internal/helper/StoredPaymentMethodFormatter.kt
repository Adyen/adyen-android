/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/2/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

internal object StoredPaymentMethodFormatter {

    fun getIcon(storedPaymentMethod: StoredPaymentMethod): String {
        return with(storedPaymentMethod) {
            when (type) {
                PaymentMethodTypes.SCHEME -> brand.orEmpty()
                else -> type
            }
        }
    }

    fun getTitle(storedPaymentMethod: StoredPaymentMethod): String {
        return with(storedPaymentMethod) {
            when (type) {
                PaymentMethodTypes.CASH_APP_PAY -> cashtag.orEmpty()
                PaymentMethodTypes.PAY_BY_BANK_US,
                PaymentMethodTypes.PAY_TO -> label.orEmpty()

                PaymentMethodTypes.PAYPAL -> shopperEmail.orEmpty()

                PaymentMethodTypes.ACH,
                PaymentMethodTypes.SCHEME -> "•••• ${lastFour.orEmpty()}"

                else -> name
            }
        }
    }

    fun getSubtitle(storedPaymentMethod: StoredPaymentMethod): String? {
        return with(storedPaymentMethod) {
            when (type) {
                PaymentMethodTypes.ACH,
                PaymentMethodTypes.CASH_APP_PAY,
                PaymentMethodTypes.PAYPAL,
                PaymentMethodTypes.PAY_BY_BANK_US,
                PaymentMethodTypes.PAY_TO,
                PaymentMethodTypes.SCHEME -> name

                else -> null
            }
        }
    }
}

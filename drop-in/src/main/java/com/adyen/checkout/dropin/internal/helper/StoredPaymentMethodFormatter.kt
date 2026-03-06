/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/2/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.paymentmethod.StoredACHDirectDebitPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCashAppPayPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayByBankUSPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayPalPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayToPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod

internal object StoredPaymentMethodFormatter {

    private const val LAST_DIGITS_COUNT = 4

    fun getIcon(storedPaymentMethod: StoredPaymentMethod): String {
        return when (storedPaymentMethod) {
            is StoredCardPaymentMethod -> storedPaymentMethod.brand
            else -> storedPaymentMethod.type
        }
    }

    fun getTitle(storedPaymentMethod: StoredPaymentMethod): String {
        return when (storedPaymentMethod) {
            is StoredCashAppPayPaymentMethod -> storedPaymentMethod.cashtag
            is StoredPayByBankUSPaymentMethod -> storedPaymentMethod.label.orEmpty()
            is StoredPayToPaymentMethod -> storedPaymentMethod.label
            is StoredACHDirectDebitPaymentMethod -> {
                "•••• ${storedPaymentMethod.bankAccountNumber.takeLast(LAST_DIGITS_COUNT)}"
            }
            is StoredCardPaymentMethod -> "•••• ${storedPaymentMethod.lastFour}"
            is StoredPayPalPaymentMethod -> storedPaymentMethod.shopperEmail
            else -> storedPaymentMethod.name
        }
    }

    fun getSubtitle(storedPaymentMethod: StoredPaymentMethod): String? {
        return when (storedPaymentMethod) {
            is StoredACHDirectDebitPaymentMethod,
            is StoredCashAppPayPaymentMethod,
            is StoredPayByBankUSPaymentMethod,
            is StoredPayToPaymentMethod,
            is StoredPayPalPaymentMethod,
            is StoredCardPaymentMethod -> storedPaymentMethod.name

            else -> null
        }
    }
}

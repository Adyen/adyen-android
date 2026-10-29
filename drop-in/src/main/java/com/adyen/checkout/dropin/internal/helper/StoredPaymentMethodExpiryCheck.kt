/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/10/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import java.util.Calendar

/**
 * A stored card is expired from the first day of the month after its expiry month, the same as on iOS. Unlike
 * [com.adyen.checkout.core.common.helper.CardExpiryDateValidator], there is no grace period, because this only labels
 * the card and doesn't block paying with it.
 */
internal class StoredPaymentMethodExpiryCheck(
    private val currentCalendar: () -> Calendar = { Calendar.getInstance() },
) {

    fun isExpired(storedPaymentMethod: StoredPaymentMethod): Boolean {
        // TODO - Check StoredBCMCPaymentMethod too once stored Bancontact is supported, like iOS
        val card = storedPaymentMethod as? StoredCardPaymentMethod
        val expiryMonth = card?.expiryMonth
            ?.takeIf { it.length == MONTH_DIGITS }
            ?.toIntOrNull()
            ?.takeIf { it in FIRST_MONTH..LAST_MONTH }
        val expiryYear = card?.expiryYear?.let { year ->
            when (year.length) {
                SHORT_YEAR_DIGITS -> year.toIntOrNull()?.plus(CENTURY)
                LONG_YEAR_DIGITS -> year.toIntOrNull()
                else -> null
            }
        }
        if (expiryMonth == null || expiryYear == null) return false

        val now = currentCalendar()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1
        return currentYear > expiryYear || (currentYear == expiryYear && currentMonth > expiryMonth)
    }

    private companion object {
        private const val MONTH_DIGITS = 2
        private const val SHORT_YEAR_DIGITS = 2
        private const val LONG_YEAR_DIGITS = 4
        private const val CENTURY = 2000
        private const val FIRST_MONTH = 1
        private const val LAST_MONTH = 12
    }
}

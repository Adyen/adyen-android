/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/7/2019.
 */
package com.adyen.checkout.components.util

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency.Companion.find
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

object AmountFormat {
    private val TAG = LogUtil.getTag()

    /**
     * Convert an [Amount] to its corresponding [BigDecimal] value.
     *
     * @param amount The [Amount] to be converted.
     * @return A [BigDecimal] representation of the [Amount].
     */
    @JvmStatic
    fun toBigDecimal(amount: Amount): BigDecimal {
        return toBigDecimal(amount.value, amount.currency!!)
    }

    /**
     * Convert a value in minor units with the corresponding currency code to its [BigDecimal] representation.
     *
     * @param value The value in minor units.
     * @param currencyCode The currency code of the value.
     * @return A [BigDecimal] representation.
     */
    private fun toBigDecimal(value: Long, currencyCode: String): BigDecimal {
        val fractionDigits = getFractionDigits(currencyCode)
        return BigDecimal.valueOf(value, fractionDigits)
    }

    private fun getFractionDigits(currencyCode: String): Int {
        val normalizedCurrencyCode = currencyCode.replace("[^A-Z]".toRegex(), "").uppercase(Locale.ROOT)
        try {
            val checkoutCurrency = find(normalizedCurrencyCode)
            return checkoutCurrency.fractionDigits
        } catch (e: CheckoutException) {
            Logger.e(
                tag = TAG,
                msg = "$normalizedCurrencyCode is an unsupported currency. Falling back to information from " +
                    "java.util.Currency.",
                tr = e
            )
        }
        return try {
            val currency = Currency.getInstance(normalizedCurrencyCode)
            currency.defaultFractionDigits.coerceAtLeast(0)
        } catch (e: IllegalArgumentException) {
            Logger.e(TAG, "Could not determine fraction digits for $normalizedCurrencyCode", e)
            0
        }
    }
}

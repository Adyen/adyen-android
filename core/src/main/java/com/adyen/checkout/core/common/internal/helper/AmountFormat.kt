/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/7/2019.
 */
package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutCurrency
import com.adyen.checkout.core.components.data.model.Amount
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object AmountFormat {

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

    @Suppress("TooGenericExceptionCaught")
    private fun getFractionDigits(currencyCode: String): Int {
        val normalizedCurrencyCode = currencyCode.replace("[^A-Z]".toRegex(), "").uppercase(Locale.ROOT)
        try {
            val checkoutCurrency = CheckoutCurrency.find(normalizedCurrencyCode)
            return checkoutCurrency.fractionDigits
        } catch (e: RuntimeException) {
            // TODO - Change RuntimeException into a clearer error once we update CheckoutCurrency.
            //  Also remove the suppresion.
//        } catch (e: CheckoutException) {
            adyenLog(AdyenLogLevel.ERROR, e) {
                "$normalizedCurrencyCode is an unsupported currency. Falling back to information from " +
                    "java.util.Currency."
            }
        }
        return try {
            val currency = Currency.getInstance(normalizedCurrencyCode)
            currency.defaultFractionDigits.coerceAtLeast(0)
        } catch (e: IllegalArgumentException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Could not determine fraction digits for $normalizedCurrencyCode" }
            0
        }
    }
}

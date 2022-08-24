/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/9/2019.
 */
package com.adyen.checkout.components.util

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency.Companion.find
import com.adyen.checkout.components.util.CheckoutCurrency.Companion.isSupported
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    private val TAG = LogUtil.getTag()

    /**
     * Format the [Amount] to be displayed to the user based on the Locale.
     *
     * @param amount The amount with currency and value.
     * @param locale The locale the amount will be formatted with.
     * @return A formatted string displaying currency and value.
     */
    fun formatAmount(amount: Amount, locale: Locale): String {
        val currencyCode = amount.currency
        val checkoutCurrency = find(currencyCode.orEmpty())
        val currency = Currency.getInstance(currencyCode)
        val currencyFormat = DecimalFormat.getCurrencyInstance(locale)
        currencyFormat.currency = currency
        currencyFormat.minimumFractionDigits = checkoutCurrency.fractionDigits
        currencyFormat.maximumFractionDigits = checkoutCurrency.fractionDigits
        val value = BigDecimal.valueOf(amount.value, checkoutCurrency.fractionDigits)
        return currencyFormat.format(value)
    }

    fun assertCurrency(currencyCode: String?) {
        if (!isSupported(currencyCode)) {
            throw CheckoutException("Currency $currencyCode not supported")
        }
    }
}

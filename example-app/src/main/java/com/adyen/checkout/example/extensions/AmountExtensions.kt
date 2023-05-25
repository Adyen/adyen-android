/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/5/2023.
 */

package com.adyen.checkout.example.extensions

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutCurrency
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

fun Amount.formatAmount(locale: Locale): String {
    val currencyCode = this.currency
    val checkoutCurrency = CheckoutCurrency.find(currencyCode.orEmpty())
    val currency = Currency.getInstance(currencyCode)
    val currencyFormat = DecimalFormat.getCurrencyInstance(locale)
    currencyFormat.currency = currency
    currencyFormat.minimumFractionDigits = checkoutCurrency.fractionDigits
    currencyFormat.maximumFractionDigits = checkoutCurrency.fractionDigits
    val value = BigDecimal.valueOf(this.value, checkoutCurrency.fractionDigits)
    return currencyFormat.format(value)
}

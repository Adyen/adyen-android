/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/2/2024.
 */

package com.adyen.checkout.demo.data.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutCurrency
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

data class StoreItem(
    val title: String,
    val imageUrl: String,
    val price: Amount,
) {
    val priceText
        get() = price.formatAmount(Locale.US)
}

private fun Amount.formatAmount(locale: Locale): String {
    val currencyCode = currency
    val checkoutCurrency = CheckoutCurrency.find(currencyCode.orEmpty())
    val currency = Currency.getInstance(currencyCode)
    val currencyFormat = DecimalFormat.getCurrencyInstance(locale)
    currencyFormat.currency = currency
    currencyFormat.minimumFractionDigits = checkoutCurrency.fractionDigits
    currencyFormat.maximumFractionDigits = checkoutCurrency.fractionDigits
    val value = BigDecimal.valueOf(value, checkoutCurrency.fractionDigits)
    return currencyFormat.format(value)
}

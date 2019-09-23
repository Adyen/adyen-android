/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 16/8/2019.
 */

package com.adyen.checkout.example.api.model

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.R
import com.adyen.checkout.redirect.RedirectComponent

private const val DEFAULT_COUNTRY = "NL"
private const val DEFAULT_LOCALE = "en_US"
private const val DEFAULT_VALUE = 1337
private const val DEFAULT_CURRENCY = "EUR"

fun createAmount(value: Int, currency: String): Amount {
    val amount = Amount()
    amount.currency = currency
    amount.value = value
    return amount
}

fun createPaymentMethodsRequest(context: Context): PaymentMethodsRequest {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    val merchantAccount =
        preferences.getString(context.getString(R.string.merchant_account_key), BuildConfig.MERCHANT_ACCOUNT) ?: BuildConfig.MERCHANT_ACCOUNT
    val shopperReference =
        preferences.getString(context.getString(R.string.shopper_reference_key), BuildConfig.SHOPPER_REFERENCE) ?: BuildConfig.SHOPPER_REFERENCE
    val amount = getAmount(context, preferences)
    val countryCode = preferences.getString(context.getString(R.string.shopper_country_key), DEFAULT_COUNTRY) ?: DEFAULT_COUNTRY
    val shopperLocale = preferences.getString(context.getString(R.string.shopper_locale_key), DEFAULT_LOCALE) ?: DEFAULT_LOCALE

    return PaymentMethodsRequest(merchantAccount, shopperReference, amount, countryCode, shopperLocale)
}

fun createPaymentsRequest(context: Context, paymentComponentData: PaymentComponentData<out PaymentMethodDetails>): PaymentsRequest {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Suppress("UsePropertyAccessSyntax")
    return PaymentsRequest(
        paymentComponentData.getPaymentMethod() as PaymentMethodDetails,
        paymentComponentData.getShopperReference()
            ?: preferences.getString(context.getString(R.string.shopper_reference_key), BuildConfig.SHOPPER_REFERENCE)
            ?: BuildConfig.SHOPPER_REFERENCE,
        paymentComponentData.isStorePaymentMethodEnable,
        getAmount(context, preferences),
        preferences.getString(context.getString(R.string.merchant_account_key), BuildConfig.MERCHANT_ACCOUNT) ?: BuildConfig.MERCHANT_ACCOUNT,
        RedirectComponent.getReturnUrl(context),
        additionalData = AdditionalData(preferences.getBoolean(context.getString(R.string.threeds2_key), false).toString())
    )
}

private fun getAmount(context: Context, preferences: SharedPreferences): Amount {
    val amountValue = preferences.getInt(context.getString(R.string.value_key), DEFAULT_VALUE)
    val amountCurrency = preferences.getString(context.getString(R.string.currency_key), DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    return createAmount(amountValue, amountCurrency)
}

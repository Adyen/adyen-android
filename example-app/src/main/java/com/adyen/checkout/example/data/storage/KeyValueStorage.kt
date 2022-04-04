/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.R
import com.adyen.checkout.example.extensions.get

interface KeyValueStorage {
    fun getShopperReference(): String
    fun getAmount(): Amount
    fun getCountry(): String
    fun getShopperLocale(): String
    fun isThreeds2Enable(): Boolean
    fun isExecuteThreeD(): Boolean
    fun getShopperEmail(): String
    fun getMerchantAccount(): String
    fun isSplitCardFundingSources(): Boolean
}

class KeyValueStorageImpl(
    private val appContext: Context,
    private val sharedPreferences: SharedPreferences
) : KeyValueStorage {

    companion object {
        private const val DEFAULT_COUNTRY = "NL"
        private const val DEFAULT_LOCALE = "en-US"
        private const val DEFAULT_VALUE = "1337"
        private const val DEFAULT_CURRENCY = "EUR"
        private const val DEFAULT_THREEDS2_ENABLE = true
        private const val DEFAULT_EXECUTE_3D = false
        private const val DEFAULT_SPLIT_CARD_FUNDING_SOURCES = false
    }

    override fun getShopperReference(): String {
        return sharedPreferences.get(appContext, R.string.shopper_reference_key, BuildConfig.SHOPPER_REFERENCE)
    }

    override fun getAmount(): Amount {

        val amountValue = sharedPreferences.get(appContext, R.string.value_key, DEFAULT_VALUE)
        val amountCurrency = sharedPreferences.get(appContext, R.string.currency_key, DEFAULT_CURRENCY)

        val amount = Amount()
        amount.currency = amountCurrency
        amount.value = amountValue.toLong()

        return amount
    }

    override fun getCountry(): String {
        return sharedPreferences.get(appContext, R.string.shopper_country_key, DEFAULT_COUNTRY)
    }

    override fun getShopperLocale(): String {
        return sharedPreferences.get(appContext, R.string.shopper_locale_key, DEFAULT_LOCALE)
    }

    override fun isThreeds2Enable(): Boolean {
        return sharedPreferences.get(appContext, R.string.threeds2_key, DEFAULT_THREEDS2_ENABLE)
    }

    override fun isExecuteThreeD(): Boolean {
        return sharedPreferences.get(appContext, R.string.execute3D_key, DEFAULT_EXECUTE_3D)
    }

    override fun getShopperEmail(): String {
        return sharedPreferences.get(appContext, R.string.shopper_email_key, "")
    }

    override fun getMerchantAccount(): String {
        return sharedPreferences.get(appContext, R.string.merchant_account_key, BuildConfig.MERCHANT_ACCOUNT)
    }

    override fun isSplitCardFundingSources(): Boolean {
        return sharedPreferences.get(
            appContext,
            R.string.split_card_funding_sources_key,
            DEFAULT_SPLIT_CARD_FUNDING_SOURCES
        )
    }
}

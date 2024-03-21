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
import androidx.core.content.edit
import com.adyen.checkout.core.Amount
import com.adyen.checkout.core.AnalyticsLevel
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.R
import com.adyen.checkout.example.extensions.getBoolean
import com.adyen.checkout.example.extensions.getString

@Suppress("TooManyFunctions")
interface KeyValueStorage {
    fun getShopperReference(): String
    fun getAmount(): Amount
    fun getCountry(): String
    fun getShopperLocale(): String?
    fun getThreeDSMode(): ThreeDSMode
    fun getShopperEmail(): String
    fun getMerchantAccount(): String
    fun isSplitCardFundingSources(): Boolean
    fun getCardAddressMode(): CardAddressMode
    fun isRemoveStoredPaymentMethodEnabled(): Boolean
    fun getInstantPaymentMethodType(): String
    fun getInstallmentOptionsMode(): CardInstallmentOptionsMode
    fun isInstallmentAmountShown(): Boolean
    fun useSessions(): Boolean
    fun setUseSessions(useSessions: Boolean)
    fun getAnalyticsLevel(): AnalyticsLevel
}

@Suppress("TooManyFunctions")
internal class DefaultKeyValueStorage(
    private val appContext: Context,
    private val sharedPreferences: SharedPreferences
) : KeyValueStorage {

    override fun getShopperReference(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.shopper_reference_key,
            defaultStringRes = R.string.preferences_default_shopper_reference,
        )
    }

    override fun getAmount(): Amount {
        return Amount(
            currency = sharedPreferences.getString(
                appContext = appContext,
                stringRes = R.string.currency_key,
                defaultStringRes = R.string.preferences_default_amount_currency,
            ),
            value = sharedPreferences.getString(
                appContext = appContext,
                stringRes = R.string.amount_value_key,
                defaultStringRes = R.string.preferences_default_amount_value,
            ).toLong(),
        )
    }

    override fun getCountry(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.shopper_country_key,
            defaultStringRes = R.string.preferences_default_country,
        )
    }

    override fun getShopperLocale(): String? {
        return sharedPreferences.getString(appContext.getString(R.string.shopper_locale_key), null)
    }

    override fun getThreeDSMode(): ThreeDSMode {
        return ThreeDSMode.valueOf(
            sharedPreferences.getString(
                appContext = appContext,
                stringRes = R.string.threeds_mode_key,
                defaultStringRes = R.string.preferences_default_threeds_mode,
            ),
        )
    }

    override fun getShopperEmail(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.shopper_email_key,
            defaultValue = "",
        )
    }

    override fun getMerchantAccount(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.merchant_account_key,
            defaultValue = BuildConfig.MERCHANT_ACCOUNT,
        )
    }

    override fun isSplitCardFundingSources(): Boolean {
        return sharedPreferences.getBoolean(
            appContext = appContext,
            stringRes = R.string.split_card_funding_sources_key,
            defaultStringRes = R.string.preferences_default_split_card_funding_sources,
        )
    }

    override fun getCardAddressMode(): CardAddressMode {
        return CardAddressMode.valueOf(
            sharedPreferences.getString(
                appContext = appContext,
                stringRes = R.string.card_address_form_mode_key,
                defaultStringRes = R.string.preferences_default_address_form_mode,
            ),
        )
    }

    override fun isRemoveStoredPaymentMethodEnabled() = sharedPreferences.getBoolean(
        appContext = appContext,
        stringRes = R.string.remove_stored_payment_method_key,
        defaultStringRes = R.string.preferences_default_remove_stored_payment_method,
    )

    override fun getInstantPaymentMethodType(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.instant_payment_method_type_key,
            defaultStringRes = R.string.preferences_default_instant_payment_method,
        )
    }

    override fun getInstallmentOptionsMode(): CardInstallmentOptionsMode {
        return CardInstallmentOptionsMode.valueOf(
            sharedPreferences.getString(
                appContext = appContext,
                stringRes = R.string.card_installment_options_mode_key,
                defaultStringRes = R.string.preferences_default_installment_options_mode,
            ),
        )
    }

    override fun isInstallmentAmountShown() = sharedPreferences.getBoolean(
        appContext = appContext,
        stringRes = R.string.card_installment_show_amount_key,
        defaultStringRes = R.string.preferences_default_installment_amount_shown,
    )

    override fun useSessions(): Boolean {
        return sharedPreferences.getBoolean(
            appContext = appContext,
            stringRes = R.string.use_sessions_key,
            defaultStringRes = R.string.preferences_default_use_sessions,
        )
    }

    override fun setUseSessions(useSessions: Boolean) {
        sharedPreferences.edit {
            putBoolean(appContext.getString(R.string.use_sessions_key), useSessions)
        }
    }

    override fun getAnalyticsLevel(): AnalyticsLevel {
        return AnalyticsLevel.valueOf(
            sharedPreferences.getString(
                appContext = appContext,
                stringRes = R.string.analytics_level_key,
                defaultStringRes = R.string.preferences_default_analytics_level,
            ),
        )
    }
}

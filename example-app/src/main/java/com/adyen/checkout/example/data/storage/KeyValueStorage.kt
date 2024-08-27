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
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.R
import com.adyen.checkout.example.extensions.getBoolean
import com.adyen.checkout.example.extensions.getString

@Suppress("TooManyFunctions")
interface KeyValueStorage {
    fun getShopperReference(): String
    fun setShopperReference(shopperReference: String?)
    fun getAmount(): Amount
    fun setAmount(amount: Long?)
    fun setCurrency(currency: String?)
    fun getCountry(): String
    fun setCountry(country: String?)
    fun getShopperLocale(): String?
    fun setShopperLocale(shopperLocale: String?)
    fun getThreeDSMode(): ThreeDSMode
    fun setThreeDSMode(threeDSMode: ThreeDSMode)
    fun getShopperEmail(): String?
    fun setShopperEmail(shopperEmail: String?)
    fun getMerchantAccount(): String
    fun setMerchantAccount(merchantAccount: String?)
    fun isSplitCardFundingSources(): Boolean
    fun setSplitCardFundingSources(isSplitCardFundingSources: Boolean)
    fun getCardAddressMode(): CardAddressMode
    fun setCardAddressMode(cardAddressMode: CardAddressMode)
    fun isRemoveStoredPaymentMethodEnabled(): Boolean
    fun setRemoveStoredPaymentMethodEnabled(isRemoveStoredPaymentMethodEnabled: Boolean)
    fun getInstantPaymentMethodType(): String
    fun setInstantPaymentMethodType(instantPaymentMethodType: String?)
    fun getInstallmentOptionsMode(): CardInstallmentOptionsMode
    fun setInstallmentOptionsMode(cardInstallmentOptionsMode: CardInstallmentOptionsMode)
    fun isInstallmentAmountShown(): Boolean
    fun setInstallmentAmountShown(isInstallmentAmountShown: Boolean)
    fun useSessions(): Boolean
    fun setUseSessions(useSessions: Boolean)
    fun getAnalyticsLevel(): AnalyticsLevel
    fun setAnalyticsLevel(analyticsLevel: AnalyticsLevel)
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

    override fun setShopperReference(shopperReference: String?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.shopper_reference_key), shopperReference)
        }
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

    override fun setAmount(amount: Long?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.amount_value_key), amount?.toString())
        }
    }

    override fun setCurrency(currency: String?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.currency_key), currency)
        }
    }

    override fun getCountry(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.shopper_country_key,
            defaultStringRes = R.string.preferences_default_country,
        )
    }

    override fun setCountry(country: String?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.shopper_country_key), country)
        }
    }

    override fun getShopperLocale(): String? {
        return sharedPreferences.getString(appContext.getString(R.string.shopper_locale_key), null)
    }

    override fun setShopperLocale(shopperLocale: String?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.shopper_locale_key), shopperLocale)
        }
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

    override fun setThreeDSMode(threeDSMode: ThreeDSMode) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.threeds_mode_key), threeDSMode.toString())
        }
    }

    override fun getShopperEmail(): String? {
        return sharedPreferences.getString(appContext.getString(R.string.shopper_email_key), null)
    }

    override fun setShopperEmail(shopperEmail: String?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.shopper_email_key), shopperEmail)
        }
    }

    override fun getMerchantAccount(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.merchant_account_key,
            defaultValue = BuildConfig.MERCHANT_ACCOUNT,
        )
    }

    override fun setMerchantAccount(merchantAccount: String?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.merchant_account_key), merchantAccount)
        }
    }

    override fun isSplitCardFundingSources(): Boolean {
        return sharedPreferences.getBoolean(
            appContext = appContext,
            stringRes = R.string.split_card_funding_sources_key,
            defaultStringRes = R.string.preferences_default_split_card_funding_sources,
        )
    }

    override fun setSplitCardFundingSources(isSplitCardFundingSources: Boolean) {
        sharedPreferences.edit {
            putBoolean(appContext.getString(R.string.split_card_funding_sources_key), isSplitCardFundingSources)
        }
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

    override fun setCardAddressMode(cardAddressMode: CardAddressMode) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.card_address_form_mode_key), cardAddressMode.toString())
        }
    }

    override fun isRemoveStoredPaymentMethodEnabled() = sharedPreferences.getBoolean(
        appContext = appContext,
        stringRes = R.string.remove_stored_payment_method_key,
        defaultStringRes = R.string.preferences_default_remove_stored_payment_method,
    )

    override fun setRemoveStoredPaymentMethodEnabled(isRemoveStoredPaymentMethodEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(
                appContext.getString(R.string.remove_stored_payment_method_key),
                isRemoveStoredPaymentMethodEnabled,
            )
        }
    }

    override fun getInstantPaymentMethodType(): String {
        return sharedPreferences.getString(
            appContext = appContext,
            stringRes = R.string.instant_payment_method_type_key,
            defaultStringRes = R.string.preferences_default_instant_payment_method,
        )
    }

    override fun setInstantPaymentMethodType(instantPaymentMethodType: String?) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.instant_payment_method_type_key), instantPaymentMethodType)
        }
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

    override fun setInstallmentOptionsMode(cardInstallmentOptionsMode: CardInstallmentOptionsMode) {
        sharedPreferences.edit {
            putString(
                appContext.getString(R.string.card_installment_options_mode_key),
                cardInstallmentOptionsMode.toString(),
            )
        }
    }

    override fun isInstallmentAmountShown() = sharedPreferences.getBoolean(
        appContext = appContext,
        stringRes = R.string.card_installment_show_amount_key,
        defaultStringRes = R.string.preferences_default_installment_amount_shown,
    )

    override fun setInstallmentAmountShown(isInstallmentAmountShown: Boolean) {
        sharedPreferences.edit {
            putBoolean(appContext.getString(R.string.card_installment_show_amount_key), isInstallmentAmountShown)
        }
    }

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

    override fun setAnalyticsLevel(analyticsLevel: AnalyticsLevel) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.analytics_level_key), analyticsLevel.toString())
        }
    }
}

/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2024.
 */

package com.adyen.checkout.example.ui.settings

import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.ui.theme.NightThemeRepository
import javax.inject.Inject

internal class SettingsUIMapper @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    private val nightThemeRepository: NightThemeRepository,
) {

    fun getSettingsCategories(): List<SettingsCategory> {
        return listOf(
            SettingsCategory(
                R.string.merchant_information,
                listOf(
                    getMerchantAccount(),
                ),
            ),
            SettingsCategory(
                R.string.payment_information,
                listOf(
                    getAmount(),
                    getCurrency(),
                    getThreeDSMode(),
                ),
            ),
            SettingsCategory(
                R.string.shopper_information,
                listOf(
                    getShopperReference(),
                    getCountry(),
                    getShopperLocale(),
                    getShopperEmail(),
                ),
            ),
            SettingsCategory(
                R.string.card_settings_title,
                listOf(
                    getAddressMode(),
                    getInstallmentOptionsMode(),
                    getInstallmentAmountShown(),
                    getSplitCardFundingSources(),
                ),
            ),
            SettingsCategory(
                R.string.other_payment_methods_settings_title,
                listOf(
                    getRemoveStoredPaymentMethodEnabled(),
                    getInstantPaymentMethodType(),
                ),
            ),
            SettingsCategory(
                R.string.analytics_title,
                listOf(
                    getAnalyticsLevel(),
                ),
            ),
            SettingsCategory(
                R.string.app_title,
                listOf(
                    getDisplayTheme(),
                ),
            ),
        )
    }

    private fun getMerchantAccount(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.MERCHANT_ACCOUNT,
            titleResId = R.string.merchant_account_title,
            subtitle = keyValueStorage.getMerchantAccount(),
        )
    }

    private fun getAmount(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.AMOUNT,
            titleResId = R.string.amount_value_title,
            subtitle = keyValueStorage.getAmount().value.toString(),
        )
    }

    private fun getCurrency(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.CURRENCY,
            titleResId = R.string.currency_title,
            subtitle = keyValueStorage.getAmount().currency ?: "Not set", // TODO: localisation
        )
    }

    private fun getThreeDSMode(): SettingsItem {
        val threeDSMode = keyValueStorage.getThreeDSMode()

        val displayValue = requireNotNull(SettingsLists.threeDSModes[threeDSMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.THREE_DS_MODE,
            titleResId = R.string.threeds_mode_title,
            subtitle = displayValue,
        )
    }

    private fun getShopperReference(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_REFERENCE,
            titleResId = R.string.shopper_reference_title,
            subtitle = keyValueStorage.getShopperReference(),
        )
    }

    private fun getCountry(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.COUNTRY,
            titleResId = R.string.shopper_country_title,
            subtitle = keyValueStorage.getCountry(),
        )
    }

    private fun getShopperLocale(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_LOCALE,
            titleResId = R.string.shopper_locale_title,
            subtitle = keyValueStorage.getShopperLocale() ?: "Not set", // TODO: localisation
        )
    }

    private fun getShopperEmail(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_EMAIL,
            titleResId = R.string.shopper_email_title,
            subtitle = keyValueStorage.getShopperEmail(), // TODO: fix empty/null default values
        )
    }

    private fun getAddressMode(): SettingsItem {
        val cardAddressMode = keyValueStorage.getCardAddressMode()
        val displayValue = requireNotNull(SettingsLists.cardAddressModes[cardAddressMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.ADDRESS_MODE,
            titleResId = R.string.card_address_form_title,
            subtitle = displayValue,
        )
    }

    private fun getInstallmentOptionsMode(): SettingsItem {
        val installmentOptionsMode = keyValueStorage.getInstallmentOptionsMode()
        val displayValue = requireNotNull(SettingsLists.cardInstallmentOptionsModes[installmentOptionsMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.INSTALLMENTS_MODE,
            titleResId = R.string.card_installment_options_mode_title,
            subtitle = displayValue,
        )
    }

    private fun getInstallmentAmountShown(): SettingsItem {
        return SettingsItem.Switch(
            identifier = SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT,
            titleResId = R.string.card_installment_show_amount_title,
            checked = keyValueStorage.isInstallmentAmountShown(),
        )
    }

    private fun getSplitCardFundingSources(): SettingsItem {
        return SettingsItem.Switch(
            identifier = SettingsIdentifier.SPLIT_CARD_FUNDING_SOURCES,
            titleResId = R.string.split_card_funding_sources_title,
            checked = keyValueStorage.isSplitCardFundingSources(),
        )
    }

    private fun getRemoveStoredPaymentMethodEnabled(): SettingsItem {
        return SettingsItem.Switch(
            identifier = SettingsIdentifier.REMOVE_STORED_PAYMENT_METHOD,
            titleResId = R.string.remove_stored_payment_method_title,
            checked = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
        )
    }

    private fun getInstantPaymentMethodType(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.INSTANT_PAYMENT_METHOD_TYPE,
            titleResId = R.string.instant_payment_method_type_title,
            subtitle = keyValueStorage.getInstantPaymentMethodType(),
        )
    }

    private fun getAnalyticsLevel(): SettingsItem {
        val analyticsLevel = keyValueStorage.getAnalyticsLevel()
        val displayValue = requireNotNull(SettingsLists.analyticsLevels[analyticsLevel])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.ANALYTICS_LEVEL,
            titleResId = R.string.analytics_level_title,
            subtitle = displayValue,
        )
    }

    private fun getDisplayTheme(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.DISPLAY_THEME,
            titleResId = R.string.analytics_level_title,
            subtitle = nightThemeRepository.theme.preferenceValue,
        )
    }
}

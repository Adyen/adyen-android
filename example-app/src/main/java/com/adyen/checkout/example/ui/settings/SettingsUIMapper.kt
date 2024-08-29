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
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import javax.inject.Inject

internal class SettingsUIMapper @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    private val uiThemeRepository: UIThemeRepository,
) {

    fun getSettingsCategories(): List<SettingsCategory> {
        return listOf(
            SettingsCategory(
                R.string.settings_category_merchant_information,
                listOf(
                    getMerchantAccount(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_payment_information,
                listOf(
                    getAmount(),
                    getCurrency(),
                    getThreeDSMode(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_shopper_information,
                listOf(
                    getShopperReference(),
                    getCountry(),
                    getShopperLocale(),
                    getShopperEmail(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_card,
                listOf(
                    getAddressMode(),
                    getInstallmentOptionsMode(),
                    getInstallmentAmountShown(),
                    getSplitCardFundingSources(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_other_payment_methods,
                listOf(
                    getRemoveStoredPaymentMethodEnabled(),
                    getInstantPaymentMethodType(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_analytics,
                listOf(
                    getAnalyticsLevel(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_app,
                listOf(
                    getUITheme(),
                ),
            ),
        )
    }

    private fun getMerchantAccount(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.MERCHANT_ACCOUNT,
            titleResId = R.string.settings_title_merchant_account,
            subtitle = keyValueStorage.getMerchantAccount(),
        )
    }

    private fun getAmount(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.AMOUNT,
            titleResId = R.string.settings_title_amount,
            subtitle = keyValueStorage.getAmount().value.toString(),
        )
    }

    private fun getCurrency(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.CURRENCY,
            titleResId = R.string.settings_title_currency,
            subtitle = keyValueStorage.getAmount().currency ?: "Not set", // TODO: localisation
        )
    }

    private fun getThreeDSMode(): SettingsItem {
        val threeDSMode = keyValueStorage.getThreeDSMode()

        val displayValue = requireNotNull(SettingsLists.threeDSModes[threeDSMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.THREE_DS_MODE,
            titleResId = R.string.settings_title_threeds_mode,
            subtitle = displayValue,
        )
    }

    private fun getShopperReference(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_REFERENCE,
            titleResId = R.string.settings_title_shopper_reference,
            subtitle = keyValueStorage.getShopperReference(),
        )
    }

    private fun getCountry(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.COUNTRY,
            titleResId = R.string.settings_title_country,
            subtitle = keyValueStorage.getCountry(),
        )
    }

    private fun getShopperLocale(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_LOCALE,
            titleResId = R.string.settings_title_shopper_locale,
            subtitle = keyValueStorage.getShopperLocale() ?: "Not set", // TODO: localisation
        )
    }

    private fun getShopperEmail(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_EMAIL,
            titleResId = R.string.settings_title_shopper_email,
            subtitle = keyValueStorage.getShopperEmail() ?: "Not set", // TODO: localisation
        )
    }

    private fun getAddressMode(): SettingsItem {
        val cardAddressMode = keyValueStorage.getCardAddressMode()
        val displayValue = requireNotNull(SettingsLists.cardAddressModes[cardAddressMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.ADDRESS_MODE,
            titleResId = R.string.settings_title_address_mode,
            subtitle = displayValue,
        )
    }

    private fun getInstallmentOptionsMode(): SettingsItem {
        val installmentOptionsMode = keyValueStorage.getInstallmentOptionsMode()
        val displayValue = requireNotNull(SettingsLists.cardInstallmentOptionsModes[installmentOptionsMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.INSTALLMENTS_MODE,
            titleResId = R.string.settings_title_card_installment_options_mode,
            subtitle = displayValue,
        )
    }

    private fun getInstallmentAmountShown(): SettingsItem {
        return SettingsItem.Switch(
            identifier = SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT,
            titleResId = R.string.settings_title_card_installment_show_amount,
            checked = keyValueStorage.isInstallmentAmountShown(),
        )
    }

    private fun getSplitCardFundingSources(): SettingsItem {
        return SettingsItem.Switch(
            identifier = SettingsIdentifier.SPLIT_CARD_FUNDING_SOURCES,
            titleResId = R.string.settings_title_split_card_funding_sources,
            checked = keyValueStorage.isSplitCardFundingSources(),
        )
    }

    private fun getRemoveStoredPaymentMethodEnabled(): SettingsItem {
        return SettingsItem.Switch(
            identifier = SettingsIdentifier.REMOVE_STORED_PAYMENT_METHOD,
            titleResId = R.string.settings_title_remove_stored_payment_method,
            checked = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
        )
    }

    private fun getInstantPaymentMethodType(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.INSTANT_PAYMENT_METHOD_TYPE,
            titleResId = R.string.settings_title_instant_payment_method_type,
            subtitle = keyValueStorage.getInstantPaymentMethodType(),
        )
    }

    private fun getAnalyticsLevel(): SettingsItem {
        val analyticsLevel = keyValueStorage.getAnalyticsLevel()
        val displayValue = requireNotNull(SettingsLists.analyticsLevels[analyticsLevel])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.ANALYTICS_LEVEL,
            titleResId = R.string.settings_title_analytics_level,
            subtitle = displayValue,
        )
    }

    private fun getUITheme(): SettingsItem {
        val theme = uiThemeRepository.theme
        val displayValue = requireNotNull(SettingsLists.uiThemes[theme])
        return SettingsItem.Text(
            identifier = SettingsIdentifier.UI_THEME,
            titleResId = R.string.settings_title_ui_theme,
            subtitle = displayValue,
        )
    }
}

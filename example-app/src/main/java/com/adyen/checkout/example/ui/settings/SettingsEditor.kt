/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings

import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.ui.theme.NightThemeRepository
import javax.inject.Inject

internal class SettingsEditor @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    private val nightThemeRepository: NightThemeRepository,
) {
    fun getEditSettingsData(settingsItem: SettingsItem): EditSettingsData {
        return when (settingsItem.identifier) {
            SettingsIdentifier.MERCHANT_ACCOUNT -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.merchant_account_title,
                    text = keyValueStorage.getMerchantAccount(),
                )
            }

            SettingsIdentifier.AMOUNT -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.amount_value_title,
                    text = keyValueStorage.getAmount().value.toString(),
                )
            }

            SettingsIdentifier.CURRENCY -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.currency_title,
                    text = keyValueStorage.getAmount().currency.orEmpty(),
                )
            }

            SettingsIdentifier.THREE_DS_MODE -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.threeds_mode_title,
                    items = SettingsLists.threeDSModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.SHOPPER_REFERENCE -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.shopper_reference_title,
                    text = keyValueStorage.getShopperReference(),
                )
            }

            SettingsIdentifier.COUNTRY -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.shopper_country_title,
                    text = keyValueStorage.getCountry(),
                )
            }

            SettingsIdentifier.SHOPPER_LOCALE -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.shopper_locale_title,
                    text = keyValueStorage.getShopperLocale().orEmpty(),
                )
            }

            SettingsIdentifier.SHOPPER_EMAIL -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.shopper_email_title,
                    text = keyValueStorage.getShopperEmail(),
                )
            }

            SettingsIdentifier.ADDRESS_MODE -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.card_address_form_title,
                    items = SettingsLists.cardAddressModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.INSTALLMENTS_MODE -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.card_installment_options_mode_title,
                    items = SettingsLists.cardInstallmentOptionsModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.INSTANT_PAYMENT_METHOD_TYPE -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.instant_payment_method_type_title,
                    text = keyValueStorage.getInstantPaymentMethodType(),
                )
            }

            SettingsIdentifier.ANALYTICS_LEVEL -> {
                EditSettingsData.SingleSelectList(
                    settingsItem.identifier,
                    titleResId = R.string.analytics_level_title,
                    SettingsLists.analyticsLevels.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.DISPLAY_THEME -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.analytics_level_title,
                    items = SettingsLists.displayThemes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT,
            SettingsIdentifier.SPLIT_CARD_FUNDING_SOURCES,
            SettingsIdentifier.REMOVE_STORED_PAYMENT_METHOD -> {
                error("Edit mode is not applicable with boolean settings")
            }
        }
    }

    fun editSetting(identifier: SettingsIdentifier, newValue: String) {
        // TODO: implement
    }

    fun editSetting(identifier: SettingsIdentifier, newValue: EditSettingsData.SingleSelectList.Item) {
        // TODO: implement
    }
}

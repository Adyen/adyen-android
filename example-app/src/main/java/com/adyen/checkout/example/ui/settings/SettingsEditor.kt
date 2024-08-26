/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings

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
                EditSettingsData.Text(keyValueStorage.getMerchantAccount())
            }

            SettingsIdentifier.AMOUNT -> {
                EditSettingsData.Text(keyValueStorage.getAmount().value.toString())
            }

            SettingsIdentifier.CURRENCY -> {
                EditSettingsData.Text(keyValueStorage.getAmount().currency.orEmpty())
            }

            SettingsIdentifier.THREE_DS_MODE -> {
                EditSettingsData.SingleSelectList(
                    SettingsLists.threeDSModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.SHOPPER_REFERENCE -> {
                EditSettingsData.Text(keyValueStorage.getShopperReference())
            }

            SettingsIdentifier.COUNTRY -> {
                EditSettingsData.Text(keyValueStorage.getCountry())
            }

            SettingsIdentifier.SHOPPER_LOCALE -> {
                EditSettingsData.Text(keyValueStorage.getShopperLocale().orEmpty())
            }

            SettingsIdentifier.SHOPPER_EMAIL -> {
                EditSettingsData.Text(keyValueStorage.getShopperEmail())
            }

            SettingsIdentifier.ADDRESS_MODE -> {
                EditSettingsData.SingleSelectList(
                    SettingsLists.cardAddressModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.INSTALLMENTS_MODE -> {
                EditSettingsData.SingleSelectList(
                    SettingsLists.cardInstallmentOptionsModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT -> {
                EditSettingsData.Switch(keyValueStorage.isInstallmentAmountShown())
            }

            SettingsIdentifier.SPLIT_CARD_FUNDING_SOURCES -> {
                EditSettingsData.Switch(keyValueStorage.isSplitCardFundingSources())
            }

            SettingsIdentifier.REMOVE_STORED_PAYMENT_METHOD -> {
                EditSettingsData.Switch(keyValueStorage.isRemoveStoredPaymentMethodEnabled())
            }

            SettingsIdentifier.INSTANT_PAYMENT_METHOD_TYPE -> {
                EditSettingsData.Text(keyValueStorage.getInstantPaymentMethodType())
            }

            SettingsIdentifier.ANALYTICS_LEVEL -> {
                EditSettingsData.SingleSelectList(
                    SettingsLists.analyticsLevels.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }

            SettingsIdentifier.DISPLAY_THEME -> {
                EditSettingsData.SingleSelectList(
                    SettingsLists.displayThemes.entries.map {
                        EditSettingsData.SingleSelectList.Item(text = it.value, value = it.key.toString())
                    },
                )
            }
        }
    }
}

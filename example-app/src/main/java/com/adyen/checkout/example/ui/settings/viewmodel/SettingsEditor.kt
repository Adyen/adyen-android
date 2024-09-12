/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings.viewmodel

import com.adyen.checkout.example.data.storage.AnalyticsMode
import com.adyen.checkout.example.data.storage.CardAddressMode
import com.adyen.checkout.example.data.storage.CardInstallmentOptionsMode
import com.adyen.checkout.example.data.storage.IntegrationFlow
import com.adyen.checkout.example.data.storage.IntegrationRegion
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.data.storage.ThreeDSMode
import com.adyen.checkout.example.ui.settings.model.EditSettingData
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.theme.UITheme
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import javax.inject.Inject

internal class SettingsEditor @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    private val uiThemeRepository: UIThemeRepository,
) {

    fun editSetting(editSettingData: EditSettingData) {
        when (editSettingData) {
            is EditSettingData.Text -> editSetting(editSettingData.identifier, editSettingData.value)
            is EditSettingData.ListItem -> editSetting(editSettingData.identifier, editSettingData.value)
            is EditSettingData.Switch -> editSetting(editSettingData.identifier, editSettingData.value)
        }
    }

    private fun editSetting(identifier: SettingsIdentifier, newValue: String) {
        val formattedValue = newValue.ifBlank { null }
        when (identifier) {
            SettingsIdentifier.MERCHANT_ACCOUNT -> {
                keyValueStorage.setMerchantAccount(formattedValue)
            }

            SettingsIdentifier.AMOUNT -> {
                keyValueStorage.setAmount(formattedValue?.toLong())
            }

            SettingsIdentifier.SHOPPER_REFERENCE -> {
                keyValueStorage.setShopperReference(formattedValue)
            }

            SettingsIdentifier.SHOPPER_LOCALE -> {
                keyValueStorage.setShopperLocale(formattedValue)
            }

            SettingsIdentifier.SHOPPER_EMAIL -> {
                keyValueStorage.setShopperEmail(formattedValue)
            }

            SettingsIdentifier.INSTANT_PAYMENT_METHOD_TYPE -> {
                keyValueStorage.setInstantPaymentMethodType(formattedValue)
            }

            else -> error("This edit mode is only supported with text type settings")
        }
    }

    private fun editSetting(identifier: SettingsIdentifier, newValue: Enum<*>) {
        when (identifier) {
            SettingsIdentifier.THREE_DS_MODE -> {
                val threeDSMode = newValue as ThreeDSMode
                keyValueStorage.setThreeDSMode(threeDSMode)
            }

            SettingsIdentifier.ADDRESS_MODE -> {
                val cardAddressMode = newValue as CardAddressMode
                keyValueStorage.setCardAddressMode(cardAddressMode)
            }

            SettingsIdentifier.INSTALLMENTS_MODE -> {
                val cardInstallmentOptionsMode = newValue as CardInstallmentOptionsMode
                keyValueStorage.setInstallmentOptionsMode(cardInstallmentOptionsMode)
            }

            SettingsIdentifier.ANALYTICS_MODE -> {
                val analyticsMode = newValue as AnalyticsMode
                keyValueStorage.setAnalyticsMode(analyticsMode)
            }

            SettingsIdentifier.UI_THEME -> {
                val uiTheme = newValue as UITheme
                uiThemeRepository.theme = uiTheme
            }

            SettingsIdentifier.INTEGRATION_FLOW -> {
                val integrationFlow = newValue as IntegrationFlow
                keyValueStorage.setIntegrationFlow(integrationFlow)
            }

            SettingsIdentifier.INTEGRATION_REGION -> {
                val integrationRegion = newValue as IntegrationRegion
                keyValueStorage.setCountry(integrationRegion.countryCode)
                keyValueStorage.setCurrency(integrationRegion.currency)
            }

            else -> error("This edit mode is only supported with list type settings")
        }
    }

    private fun editSetting(identifier: SettingsIdentifier, newValue: Boolean) {
        when (identifier) {
            SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT -> {
                keyValueStorage.setInstallmentAmountShown(newValue)
            }

            SettingsIdentifier.SPLIT_CARD_FUNDING_SOURCES -> {
                keyValueStorage.setSplitCardFundingSources(newValue)
            }

            SettingsIdentifier.REMOVE_STORED_PAYMENT_METHOD -> {
                keyValueStorage.setRemoveStoredPaymentMethodEnabled(newValue)
            }

            else -> error("This edit mode is only supported with boolean type settings")
        }
    }
}

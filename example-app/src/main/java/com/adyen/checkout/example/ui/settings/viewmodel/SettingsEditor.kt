/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings.viewmodel

import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.example.data.storage.CardAddressMode
import com.adyen.checkout.example.data.storage.CardInstallmentOptionsMode
import com.adyen.checkout.example.data.storage.IntegrationFlow
import com.adyen.checkout.example.data.storage.IntegrationRegion
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.data.storage.ThreeDSMode
import com.adyen.checkout.example.ui.settings.model.EditSettingsData
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.theme.UITheme
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import javax.inject.Inject

internal class SettingsEditor @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    private val uiThemeRepository: UIThemeRepository,
) {

    fun editSetting(identifier: SettingsIdentifier, newValue: String) {
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

    fun editSetting(identifier: SettingsIdentifier, newValue: EditSettingsData.SingleSelectList.Item) {
        when (identifier) {
            SettingsIdentifier.THREE_DS_MODE -> {
                val threeDSMode = ThreeDSMode.valueOf(newValue.value)
                keyValueStorage.setThreeDSMode(threeDSMode)
            }

            SettingsIdentifier.ADDRESS_MODE -> {
                val cardAddressMode = CardAddressMode.valueOf(newValue.value)
                keyValueStorage.setCardAddressMode(cardAddressMode)
            }

            SettingsIdentifier.INSTALLMENTS_MODE -> {
                val cardInstallmentOptionsMode = CardInstallmentOptionsMode.valueOf(newValue.value)
                keyValueStorage.setInstallmentOptionsMode(cardInstallmentOptionsMode)
            }

            SettingsIdentifier.ANALYTICS_LEVEL -> {
                val analyticsLevel = AnalyticsLevel.valueOf(newValue.value)
                keyValueStorage.setAnalyticsLevel(analyticsLevel)
            }

            SettingsIdentifier.UI_THEME -> {
                val uiTheme = UITheme.valueOf(newValue.value)
                uiThemeRepository.theme = uiTheme
            }

            SettingsIdentifier.INTEGRATION_FLOW -> {
                val integrationFlow = IntegrationFlow.valueOf(newValue.value)
                keyValueStorage.setIntegrationFlow(integrationFlow)
            }

            SettingsIdentifier.INTEGRATION_REGION -> {
                val integrationRegion = IntegrationRegion.valueOf(newValue.value)
                keyValueStorage.setCountry(integrationRegion.countryCode)
                keyValueStorage.setCurrency(integrationRegion.currency)
            }

            else -> error("This edit mode is only supported with list type settings")
        }
    }

    fun editSetting(identifier: SettingsIdentifier, newValue: Boolean) {
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

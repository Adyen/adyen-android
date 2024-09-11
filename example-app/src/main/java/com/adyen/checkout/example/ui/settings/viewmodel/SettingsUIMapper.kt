/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.viewmodel

import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.IntegrationFlow
import com.adyen.checkout.example.data.storage.IntegrationRegion
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.data.storage.SettingsDefaults
import com.adyen.checkout.example.provider.LocaleProvider
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.settings.model.EditSettingsData
import com.adyen.checkout.example.ui.settings.model.IntegrationRegionUIMapper
import com.adyen.checkout.example.ui.settings.model.SettingsCategory
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.settings.model.SettingsItem
import com.adyen.checkout.example.ui.settings.model.SettingsLists
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import javax.inject.Inject

@Suppress("TooManyFunctions")
internal class SettingsUIMapper @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    private val uiThemeRepository: UIThemeRepository,
    private val localeProvider: LocaleProvider,
    private val integrationRegionUIMapper: IntegrationRegionUIMapper,
) {

    fun getSettingsCategories(): List<SettingsCategory> {
        return listOf(
            SettingsCategory(
                R.string.settings_category_integration_parameters,
                listOf(
                    getAmount(),
                    getIntegrationRegion(),
                    getIntegrationFlow(),
                    getMerchantAccount(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_shopper_information,
                listOf(
                    getShopperLocale(),
                    getShopperReference(),
                    getShopperEmail(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_card,
                listOf(
                    getThreeDSMode(),
                    getAddressMode(),
                    getInstallmentOptionsMode(),
                    getInstallmentAmountShown(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_drop_in,
                listOf(
                    getRemoveStoredPaymentMethodEnabled(),
                    getSplitCardFundingSources(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_analytics,
                listOf(
                    getAnalyticsMode(),
                ),
            ),
            SettingsCategory(
                R.string.settings_category_app,
                listOf(
                    getInstantPaymentMethodType(),
                    getUITheme(),
                ),
            ),
        )
    }

    private fun getMerchantAccount(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.MERCHANT_ACCOUNT,
            titleResId = R.string.settings_title_merchant_account,
            subtitle = UIText.String(keyValueStorage.getMerchantAccount()),
        )
    }

    private fun getAmount(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.AMOUNT,
            titleResId = R.string.settings_title_amount,
            subtitle = UIText.String(keyValueStorage.getAmount().value.toString()),
        )
    }

    private fun getThreeDSMode(): SettingsItem {
        val threeDSMode = keyValueStorage.getThreeDSMode()
        val displayValue = requireNotNull(SettingsLists.threeDSModes[threeDSMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.THREE_DS_MODE,
            titleResId = R.string.settings_title_threeds_mode,
            subtitle = UIText.Resource(displayValue),
        )
    }

    private fun getShopperReference(): SettingsItem {
        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_REFERENCE,
            titleResId = R.string.settings_title_shopper_reference,
            subtitle = UIText.String(keyValueStorage.getShopperReference()),
        )
    }

    private fun getIntegrationRegion(): SettingsItem {
        val country = keyValueStorage.getCountry()
        val integrationRegion = IntegrationRegion.valueOf(country)
        val displayValue = integrationRegionUIMapper.getIntegrationRegionDisplayData(integrationRegion).uiText
        return SettingsItem.Text(
            identifier = SettingsIdentifier.INTEGRATION_REGION,
            titleResId = R.string.settings_title_integration_region,
            subtitle = displayValue,
        )
    }

    private fun getIntegrationFlow(): SettingsItem {
        val integrationFlow = keyValueStorage.getIntegrationFlow()
        val displayValue = requireNotNull(SettingsLists.integrationFlows[integrationFlow])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.INTEGRATION_FLOW,
            titleResId = R.string.settings_title_integration_flow,
            subtitle = UIText.Resource(displayValue),
        )
    }

    private fun getShopperLocale(): SettingsItem {
        val shopperLocale = keyValueStorage.getShopperLocale()
        val subtitle = if (shopperLocale != null) {
            UIText.String(shopperLocale)
        } else {
            when (keyValueStorage.getIntegrationFlow()) {
                IntegrationFlow.SESSIONS -> {
                    UIText.Resource(R.string.settings_shopper_locale_sessions_flow_placeholder)
                }

                IntegrationFlow.ADVANCED -> {
                    UIText.Resource(
                        R.string.settings_shopper_locale_advanced_flow_placeholder,
                        localeProvider.locale.toLanguageTag(),
                    )
                }
            }
        }

        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_LOCALE,
            titleResId = R.string.settings_title_shopper_locale,
            subtitle = subtitle,
        )
    }

    private fun getShopperEmail(): SettingsItem {
        val subtitle = keyValueStorage.getShopperEmail()?.let {
            UIText.String(it)
        } ?: UIText.Resource(R.string.settings_null_value_placeholder)

        return SettingsItem.Text(
            identifier = SettingsIdentifier.SHOPPER_EMAIL,
            titleResId = R.string.settings_title_shopper_email,
            subtitle = subtitle,
        )
    }

    private fun getAddressMode(): SettingsItem {
        val cardAddressMode = keyValueStorage.getCardAddressMode()
        val displayValue = requireNotNull(SettingsLists.cardAddressModes[cardAddressMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.ADDRESS_MODE,
            titleResId = R.string.settings_title_address_mode,
            subtitle = UIText.Resource(displayValue),
        )
    }

    private fun getInstallmentOptionsMode(): SettingsItem {
        val installmentOptionsMode = keyValueStorage.getInstallmentOptionsMode()
        val displayValue = requireNotNull(SettingsLists.cardInstallmentOptionsModes[installmentOptionsMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.INSTALLMENTS_MODE,
            titleResId = R.string.settings_title_card_installment_options_mode,
            subtitle = UIText.Resource(displayValue),
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
            subtitle = UIText.String(keyValueStorage.getInstantPaymentMethodType()),
        )
    }

    private fun getAnalyticsMode(): SettingsItem {
        val analyticsMode = keyValueStorage.getAnalyticsMode()
        val displayValue = requireNotNull(SettingsLists.analyticsModes[analyticsMode])

        return SettingsItem.Text(
            identifier = SettingsIdentifier.ANALYTICS_MODE,
            titleResId = R.string.settings_title_analytics_mode,
            subtitle = UIText.Resource(displayValue),
        )
    }

    private fun getUITheme(): SettingsItem {
        val theme = uiThemeRepository.theme
        val displayValue = requireNotNull(SettingsLists.uiThemes[theme])
        return SettingsItem.Text(
            identifier = SettingsIdentifier.UI_THEME,
            titleResId = R.string.settings_title_ui_theme,
            subtitle = UIText.Resource(displayValue),
        )
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    fun getEditSettingsData(settingsItem: SettingsItem): EditSettingsData {
        return when (settingsItem.identifier) {
            SettingsIdentifier.MERCHANT_ACCOUNT -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_merchant_account,
                    text = keyValueStorage.getMerchantAccount(),
                    placeholder = defaultValueText(SettingsDefaults.MERCHANT_ACCOUNT),
                )
            }

            SettingsIdentifier.AMOUNT -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_amount,
                    text = keyValueStorage.getAmount().value.toString(),
                    inputType = EditSettingsData.Text.InputType.INTEGER,
                    placeholder = defaultValueText(SettingsDefaults.AMOUNT.toString()),
                )
            }

            SettingsIdentifier.THREE_DS_MODE -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_threeds_mode,
                    items = SettingsLists.threeDSModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(
                            text = UIText.Resource(it.value),
                            value = it.key.toString(),
                        )
                    },
                )
            }

            SettingsIdentifier.SHOPPER_REFERENCE -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_shopper_reference,
                    text = keyValueStorage.getShopperReference(),
                    placeholder = defaultValueText(SettingsDefaults.SHOPPER_REFERENCE),
                )
            }

            SettingsIdentifier.INTEGRATION_REGION -> {
                val items = IntegrationRegion.entries.map { integrationRegion ->
                    integrationRegionUIMapper.getIntegrationRegionDisplayData(integrationRegion)
                }
                    .sortedBy { it.localizedCountryName }
                    .map { integrationRegion ->
                        EditSettingsData.SingleSelectList.Item(
                            text = integrationRegion.uiText,
                            value = integrationRegion.integrationRegion.countryCode,
                        )
                    }

                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_integration_region,
                    items = items,
                )
            }

            SettingsIdentifier.SHOPPER_LOCALE -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_shopper_locale,
                    text = keyValueStorage.getShopperLocale().orEmpty(),
                    placeholder = UIText.Resource(R.string.settings_format_helper_locale),
                )
            }

            SettingsIdentifier.SHOPPER_EMAIL -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_shopper_email,
                    text = keyValueStorage.getShopperEmail().orEmpty(),
                )
            }

            SettingsIdentifier.ADDRESS_MODE -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_address_mode,
                    items = SettingsLists.cardAddressModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(
                            text = UIText.Resource(it.value),
                            value = it.key.toString(),
                        )
                    },
                )
            }

            SettingsIdentifier.INSTALLMENTS_MODE -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_card_installment_options_mode,
                    items = SettingsLists.cardInstallmentOptionsModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(
                            text = UIText.Resource(it.value),
                            value = it.key.toString(),
                        )
                    },
                )
            }

            SettingsIdentifier.INSTANT_PAYMENT_METHOD_TYPE -> {
                EditSettingsData.Text(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_instant_payment_method_type,
                    text = keyValueStorage.getInstantPaymentMethodType(),
                    placeholder = defaultValueText(SettingsDefaults.INSTANT_PAYMENT_METHOD_TYPE),
                )
            }

            SettingsIdentifier.ANALYTICS_MODE -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_analytics_mode,
                    items = SettingsLists.analyticsModes.entries.map {
                        EditSettingsData.SingleSelectList.Item(
                            text = UIText.Resource(it.value),
                            value = it.key.toString(),
                        )
                    },
                )
            }

            SettingsIdentifier.UI_THEME -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_ui_theme,
                    items = SettingsLists.uiThemes.entries.map {
                        EditSettingsData.SingleSelectList.Item(
                            text = UIText.Resource(it.value),
                            value = it.key.toString(),
                        )
                    },
                )
            }

            SettingsIdentifier.INTEGRATION_FLOW -> {
                EditSettingsData.SingleSelectList(
                    identifier = settingsItem.identifier,
                    titleResId = R.string.settings_title_integration_flow,
                    items = SettingsLists.integrationFlows.entries.map {
                        EditSettingsData.SingleSelectList.Item(
                            text = UIText.Resource(it.value),
                            value = it.key.toString(),
                        )
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

    private fun defaultValueText(defaultValue: String): UIText.Resource {
        return UIText.Resource(R.string.settings_default_value, defaultValue)
    }
}

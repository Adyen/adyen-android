/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/8/2024.
 */

package com.adyen.checkout.example.data.storage

enum class SharedPreferencesEntry(val key: String, val defaultValue: Any?) {
    SHOPPER_REFERENCE(
        key = "shopper_reference",
        defaultValue = SettingsDefaults.SHOPPER_REFERENCE,
    ),
    AMOUNT(
        key = "amount_value",
        defaultValue = SettingsDefaults.AMOUNT,
    ),
    CURRENCY(
        key = "amount_currency",
        defaultValue = SettingsDefaults.CURRENCY,
    ),
    SHOPPER_COUNTRY(
        key = "shopper_country",
        defaultValue = SettingsDefaults.SHOPPER_COUNTRY,
    ),
    SHOPPER_LOCALE(
        key = "shopper_locale",
        defaultValue = SettingsDefaults.SHOPPER_LOCALE,
    ),
    THREEDS_MODE(
        key = "threeds_mode",
        defaultValue = SettingsDefaults.THREEDS_MODE,
    ),
    SHOPPER_EMAIL(
        key = "shopper_email",
        defaultValue = SettingsDefaults.SHOPPER_EMAIL,
    ),
    MERCHANT_ACCOUNT(
        key = "merchant_account",
        defaultValue = SettingsDefaults.MERCHANT_ACCOUNT,
    ),
    SPLIT_CARD_FUNDING_SOURCES(
        key = "split_card_funding_sources",
        defaultValue = SettingsDefaults.SPLIT_CARD_FUNDING_SOURCES,
    ),
    CARD_ADDRESS_FORM_MODE(
        key = "card_address_form_mode",
        defaultValue = SettingsDefaults.CARD_ADDRESS_FORM_MODE,
    ),
    REMOVE_STORED_PAYMENT_METHOD(
        key = "remove_stored_payment_method",
        defaultValue = SettingsDefaults.REMOVE_STORED_PAYMENT_METHOD,
    ),
    INSTANT_PAYMENT_METHOD_TYPE(
        key = "instant_payment_method_type",
        defaultValue = SettingsDefaults.INSTANT_PAYMENT_METHOD_TYPE,
    ),
    CARD_INSTALLMENT_OPTIONS_MODE(
        key = "card_installment_options_mode",
        defaultValue = SettingsDefaults.CARD_INSTALLMENT_OPTIONS_MODE,
    ),
    CARD_INSTALLMENT_SHOW_AMOUNT(
        key = "card_installment_show_amount",
        defaultValue = SettingsDefaults.CARD_INSTALLMENT_SHOW_AMOUNT,
    ),
    INTEGRATION_FLOW(
        key = "integration_flow",
        defaultValue = SettingsDefaults.INTEGRATION_FLOW,
    ),
    ANALYTICS_LEVEL(
        key = "analytics_level",
        defaultValue = SettingsDefaults.ANALYTICS_LEVEL,
    ),
    UI_THEME(
        key = "ui_theme",
        defaultValue = SettingsDefaults.UI_THEME,
    ),
}

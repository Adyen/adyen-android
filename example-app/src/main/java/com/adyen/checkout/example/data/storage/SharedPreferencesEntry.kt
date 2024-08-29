/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/8/2024.
 */

package com.adyen.checkout.example.data.storage

import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.ui.theme.UITheme

enum class SharedPreferencesEntry(val key: String, val defaultValue: Any?) {
    SHOPPER_REFERENCE(key = "shopper_reference", defaultValue = "test-android-components"),
    AMOUNT(key = "amount_value", defaultValue = 1337L),
    CURRENCY(key = "amount_currency", defaultValue = "EUR"),
    SHOPPER_COUNTRY(key = "shopper_country", defaultValue = "NL"),
    SHOPPER_LOCALE(key = "shopper_locale", defaultValue = null),
    THREEDS_MODE(key = "threeds_mode", defaultValue = ThreeDSMode.PREFER_NATIVE),
    SHOPPER_EMAIL(key = "shopper_email", defaultValue = null),
    MERCHANT_ACCOUNT(key = "merchant_account", defaultValue = BuildConfig.MERCHANT_ACCOUNT),
    SPLIT_CARD_FUNDING_SOURCES(key = "split_card_funding_sources", defaultValue = false),
    CARD_ADDRESS_FORM_MODE(key = "card_address_form_mode", defaultValue = CardAddressMode.NONE),
    REMOVE_STORED_PAYMENT_METHOD(key = "remove_stored_payment_method", defaultValue = true),
    INSTANT_PAYMENT_METHOD_TYPE(key = "instant_payment_method_type", defaultValue = "wechatpaySDK"),
    CARD_INSTALLMENT_OPTIONS_MODE(
        key = "card_installment_options_mode",
        defaultValue = CardInstallmentOptionsMode.NONE,
    ),
    CARD_INSTALLMENT_SHOW_AMOUNT(key = "card_installment_show_amount", defaultValue = false),
    USE_SESSIONS(key = "use_sessions", defaultValue = true),
    ANALYTICS_LEVEL(key = "analytics_level", defaultValue = AnalyticsLevel.ALL),
    UI_THEME(key = "ui_theme", defaultValue = UITheme.SYSTEM),
}

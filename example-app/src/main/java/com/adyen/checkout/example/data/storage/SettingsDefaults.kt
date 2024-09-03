/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/8/2024.
 */

package com.adyen.checkout.example.data.storage

import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.ui.theme.UITheme

object SettingsDefaults {
    const val SHOPPER_REFERENCE: String = "test-android-components"
    const val AMOUNT: Long = 1337L
    const val CURRENCY: String = "EUR"
    const val SHOPPER_COUNTRY: String = "NL"
    val SHOPPER_LOCALE: String? = null
    val THREEDS_MODE: ThreeDSMode = ThreeDSMode.PREFER_NATIVE
    val SHOPPER_EMAIL: String? = null
    const val MERCHANT_ACCOUNT: String = BuildConfig.MERCHANT_ACCOUNT
    const val SPLIT_CARD_FUNDING_SOURCES: Boolean = false
    val CARD_ADDRESS_FORM_MODE: CardAddressMode = CardAddressMode.NONE
    const val REMOVE_STORED_PAYMENT_METHOD: Boolean = true
    const val INSTANT_PAYMENT_METHOD_TYPE: String = "wechatpaySDK"
    val CARD_INSTALLMENT_OPTIONS_MODE: CardInstallmentOptionsMode = CardInstallmentOptionsMode.NONE
    const val CARD_INSTALLMENT_SHOW_AMOUNT: Boolean = false
    val INTEGRATION_FLOW: IntegrationFlow = IntegrationFlow.SESSIONS
    val ANALYTICS_LEVEL: AnalyticsLevel = AnalyticsLevel.ALL
    val UI_THEME: UITheme = UITheme.SYSTEM
}

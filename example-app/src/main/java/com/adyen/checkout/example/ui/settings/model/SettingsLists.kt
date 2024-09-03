/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings.model

import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.CardAddressMode
import com.adyen.checkout.example.data.storage.CardInstallmentOptionsMode
import com.adyen.checkout.example.data.storage.IntegrationFlow
import com.adyen.checkout.example.data.storage.ThreeDSMode
import com.adyen.checkout.example.ui.theme.UITheme

internal object SettingsLists {

    val threeDSModes = ThreeDSMode.entries.associateWith {
        when (it) {
            ThreeDSMode.PREFER_NATIVE -> R.string.settings_list_threeds_mode_prefer_native
            ThreeDSMode.REDIRECT -> R.string.settings_list_threeds_mode_redirect
            ThreeDSMode.DISABLED -> R.string.settings_list_threeds_mode_disabled
        }
    }

    val cardAddressModes = CardAddressMode.entries.associateWith {
        when (it) {
            CardAddressMode.NONE -> R.string.settings_list_card_address_none
            CardAddressMode.POSTAL_CODE -> R.string.settings_list_card_address_postal_code
            CardAddressMode.FULL_ADDRESS -> R.string.settings_list_card_address_full_address
            CardAddressMode.LOOKUP -> R.string.settings_list_card_address_lookup
        }
    }

    val cardInstallmentOptionsModes = CardInstallmentOptionsMode.entries.associateWith {
        when (it) {
            CardInstallmentOptionsMode.NONE -> R.string.settings_list_installment_option_none
            CardInstallmentOptionsMode.DEFAULT -> R.string.settings_list_installment_option_default
            CardInstallmentOptionsMode.DEFAULT_WITH_REVOLVING -> R.string.settings_list_installment_option_default_revolving
            CardInstallmentOptionsMode.CARD_BASED_VISA -> R.string.settings_list_installment_option_card_based
        }
    }

    val analyticsLevels = AnalyticsLevel.entries.associateWith {
        when (it) {
            AnalyticsLevel.ALL -> R.string.settings_list_analytics_level_all
            AnalyticsLevel.NONE -> R.string.settings_list_analytics_level_none
        }
    }

    val uiThemes = UITheme.entries.associateWith {
        when (it) {
            UITheme.LIGHT -> R.string.settings_list_ui_theme_light
            UITheme.DARK -> R.string.settings_list_ui_theme_dark
            UITheme.SYSTEM -> R.string.settings_list_ui_theme_system
        }
    }

    val integrationFlows = IntegrationFlow.entries.associateWith {
        when (it) {
            IntegrationFlow.SESSIONS -> R.string.settings_list_integration_flow_sessions
            IntegrationFlow.ADVANCED -> R.string.settings_list_integration_flow_advanced
        }
    }
}

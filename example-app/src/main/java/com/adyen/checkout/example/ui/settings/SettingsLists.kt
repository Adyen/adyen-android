/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2024.
 */

package com.adyen.checkout.example.ui.settings

import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.example.data.storage.CardAddressMode
import com.adyen.checkout.example.data.storage.CardInstallmentOptionsMode
import com.adyen.checkout.example.data.storage.ThreeDSMode
import com.adyen.checkout.example.ui.theme.NightTheme

// TODO: localisation
internal object SettingsLists {

    val threeDSModes = ThreeDSMode.entries.associateWith {
        when (it) {
            ThreeDSMode.PREFER_NATIVE -> "Prefer native"
            ThreeDSMode.REDIRECT -> "Redirect"
            ThreeDSMode.DISABLED -> "Disabled"
        }
    }

    val cardAddressModes = CardAddressMode.entries.associateWith {
        when (it) {
            CardAddressMode.NONE -> "None"
            CardAddressMode.POSTAL_CODE -> "Postal code"
            CardAddressMode.FULL_ADDRESS -> "Full address"
            CardAddressMode.LOOKUP -> "Lookup"
        }
    }

    val cardInstallmentOptionsModes = CardInstallmentOptionsMode.entries.associateWith {
        when (it) {
            CardInstallmentOptionsMode.NONE -> "None"
            CardInstallmentOptionsMode.DEFAULT -> "Default installment options"
            CardInstallmentOptionsMode.DEFAULT_WITH_REVOLVING -> "Default installment options with revolving"
            CardInstallmentOptionsMode.CARD_BASED_VISA -> "Card based installment options (VISA)"
        }
    }

    val analyticsLevels = AnalyticsLevel.entries.associateWith {
        when (it) {
            AnalyticsLevel.ALL -> "All"
            AnalyticsLevel.NONE -> "None"
        }
    }

    val displayThemes = NightTheme.entries.associateWith {
        it.preferenceValue
    }
}

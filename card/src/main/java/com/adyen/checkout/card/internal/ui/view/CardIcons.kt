/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/12/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.adyen.checkout.card.R
import com.adyen.checkout.ui.internal.theme.isDarkMode

@Composable
internal fun rememberCardIcons(): CardIcons {
    val isDark = isDarkMode()
    return remember(isDark) {
        if (isDark) CardIcons.Dark else CardIcons.Light
    }
}

internal enum class CardIcons(
    val placeholderResId: Int,
    val expiryDateResId: Int,
    val cvcBackResId: Int,
    val cvcFrontResId: Int,
) {
    Dark(
        placeholderResId = R.drawable.ic_card_placeholder_dark,
        expiryDateResId = R.drawable.ic_card_expiry_date_dark,
        cvcBackResId = R.drawable.ic_card_cvc_back_dark,
        cvcFrontResId = R.drawable.ic_card_cvc_front_dark,
    ),
    Light(
        placeholderResId = R.drawable.ic_card_placeholder_light,
        expiryDateResId = R.drawable.ic_card_expiry_date_light,
        cvcBackResId = R.drawable.ic_card_cvc_back_light,
        cvcFrontResId = R.drawable.ic_card_cvc_front_light,
    )
}

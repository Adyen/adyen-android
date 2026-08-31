/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
import com.adyen.checkout.card.internal.ui.model.ExpiryDateTrailingIcon
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

/**
 * Which icon each card field shows while it is not in an error state. Shared by the card and stored card producers,
 * which show the same security code icons.
 */
internal fun getCardNumberTrailingIcon(isCardScanButtonVisible: Boolean): CardNumberTrailingIcon {
    return if (isCardScanButtonVisible) {
        CardNumberTrailingIcon.ScanButton
    } else {
        CardNumberTrailingIcon.BrandLogos
    }
}

internal fun getExpiryDateTrailingIcon(expiryDate: TextInputComponentState): ExpiryDateTrailingIcon {
    return if (expiryDate.isValid && expiryDate.text.isNotEmpty()) {
        ExpiryDateTrailingIcon.Checkmark
    } else {
        ExpiryDateTrailingIcon.Placeholder
    }
}

internal fun getSecurityCodeTrailingIcon(
    securityCode: TextInputComponentState,
    cardNumberFormat: CardNumberFormat,
): SecurityCodeTrailingIcon {
    return when {
        securityCode.isValid && securityCode.text.isNotEmpty() -> SecurityCodeTrailingIcon.Checkmark
        cardNumberFormat == CardNumberFormat.AMEX -> SecurityCodeTrailingIcon.PlaceholderAmex
        else -> SecurityCodeTrailingIcon.PlaceholderDefault
    }
}

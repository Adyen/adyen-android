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

// Which icon each field shows while it is not in an error state. Out of the producers because the card and stored card
// screens show the same security code icons.

internal fun getCardNumberTrailingIcon(isCardScanButtonVisible: Boolean): CardNumberTrailingIcon {
    return if (isCardScanButtonVisible) {
        CardNumberTrailingIcon.ScanButton
    } else {
        CardNumberTrailingIcon.BrandLogos
    }
}

internal fun getExpiryDateTrailingIcon(isValid: Boolean, isEmpty: Boolean): ExpiryDateTrailingIcon {
    return if (isValid && !isEmpty) {
        ExpiryDateTrailingIcon.Checkmark
    } else {
        ExpiryDateTrailingIcon.Placeholder
    }
}

internal fun getSecurityCodeTrailingIcon(
    isValid: Boolean,
    isEmpty: Boolean,
    cardNumberFormat: CardNumberFormat,
): SecurityCodeTrailingIcon {
    return when {
        isValid && !isEmpty -> SecurityCodeTrailingIcon.Checkmark
        cardNumberFormat == CardNumberFormat.AMEX -> SecurityCodeTrailingIcon.PlaceholderAmex
        else -> SecurityCodeTrailingIcon.PlaceholderDefault
    }
}

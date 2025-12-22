/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/12/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.components.internal.ui.state.model.TrailingIcon

internal sealed class CardNumberTrailingIcon : TrailingIcon() {
    data object BrandLogos : CardNumberTrailingIcon()
    data object Warning : CardNumberTrailingIcon()
}

internal sealed class ExpiryDateTrailingIcon : TrailingIcon() {
    data object Placeholder : ExpiryDateTrailingIcon()
    data object Checkmark : ExpiryDateTrailingIcon()
    data object Warning : ExpiryDateTrailingIcon()
}

internal sealed class SecurityCodeTrailingIcon : TrailingIcon() {
    data object PlaceholderDefault : SecurityCodeTrailingIcon()
    data object PlaceholderAmex : SecurityCodeTrailingIcon()
    data object Checkmark : SecurityCodeTrailingIcon()
    data object Warning : SecurityCodeTrailingIcon()
}

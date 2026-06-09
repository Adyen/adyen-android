/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/6/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType

internal enum class CardNumberFormat {
    DEFAULT,
    AMEX,
}

internal fun CardBrand?.toCardNumberFormat(): CardNumberFormat {
    return if (this?.txVariant == CardType.AMERICAN_EXPRESS.txVariant) {
        CardNumberFormat.AMEX
    } else {
        CardNumberFormat.DEFAULT
    }
}

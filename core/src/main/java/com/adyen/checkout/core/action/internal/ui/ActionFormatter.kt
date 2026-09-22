/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/9/2026.
 */

package com.adyen.checkout.core.action.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.common.internal.ui.CARD_LOGO_TX_VARIANT
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ActionFormatter {

    fun getIcon(action: Action): String = when (val paymentMethodType = action.paymentMethodType) {
        PaymentMethodTypes.SCHEME -> CARD_LOGO_TX_VARIANT
        else -> paymentMethodType.orEmpty()
    }
}

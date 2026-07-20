/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/7/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.helper.LocalLocale
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.ui.internal.element.button.PrimaryButton

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun PayButton(
    amount: Amount?,
    onClick: () -> Unit,
    isLoading: Boolean = false,
) {
    val text = when {
        amount == null -> resolveString(CheckoutLocalizationKey.PAY_BUTTON_NO_AMOUNT)
        amount.value == 0L -> resolveString(CheckoutLocalizationKey.PAY_BUTTON_ZERO_AMOUNT)
        else -> resolveString(CheckoutLocalizationKey.PAY_BUTTON_WITH_AMOUNT, amount.format(LocalLocale.current))
    }
    PrimaryButton(
        onClick = onClick,
        text = text,
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth(),
    )
}

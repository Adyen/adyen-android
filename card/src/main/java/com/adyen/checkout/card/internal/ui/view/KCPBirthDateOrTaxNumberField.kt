/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField

@Composable
internal fun KCPBirthDateOrTaxNumberField(
    kcpBirthDateOrTaxNumberState: TextInputViewState,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingText = kcpBirthDateOrTaxNumberState.supportingText?.let { resolveString(it) }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumberFocus(focusState.isFocused))
            },
        label = resolveString(CheckoutLocalizationKey.CARD_KCP_BIRTH_DATE_OR_TAX_NUMBER),
        initialValue = kcpBirthDateOrTaxNumberState.text,
        isError = kcpBirthDateOrTaxNumberState.isError,
        supportingText = supportingText,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumber(value))
        },
        shouldFocus = kcpBirthDateOrTaxNumberState.isFocused,
    )
}

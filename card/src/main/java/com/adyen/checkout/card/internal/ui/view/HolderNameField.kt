/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField

@Composable
internal fun HolderNameField(
    holderNameState: TextInputComponentState,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showHolderNameError = holderNameState.errorMessage != null && holderNameState.showError
    val supportingTextHolderName = if (showHolderNameError) {
        holderNameState.errorMessage?.let { resolveString(it) }
    } else {
        null
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateHolderNameFocus(focusState.isFocused))
            },
        label = resolveString(CheckoutLocalizationKey.CARD_HOLDER_NAME),
        initialValue = holderNameState.text,
        isError = showHolderNameError,
        supportingText = supportingTextHolderName,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateHolderName(value))
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
        ),
        shouldFocus = holderNameState.isFocused,
    )
}

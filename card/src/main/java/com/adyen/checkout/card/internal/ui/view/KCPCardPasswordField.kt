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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.card.internal.ui.properties.KCPCardPasswordProperties.KCP_CARD_PASSWORD_MAX_LENGTH
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.TextFieldStylePreviewParameterProvider
import com.adyen.checkout.ui.internal.helper.CheckoutThemeWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun KCPCardPasswordField(
    kcpCardPasswordState: TextInputViewState,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingText = kcpCardPasswordState.supportingText?.let { resolveString(it) }

    val inputTransformation = remember {
        DigitOnlyInputTransformation(
            maxLengthWithoutSeparators = KCP_CARD_PASSWORD_MAX_LENGTH,
        )
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateKcpCardPasswordFocus(focusState.isFocused))
            },
        label = resolveString(CheckoutLocalizationKey.CARD_KCP_CARD_PASSWORD),
        initialValue = kcpCardPasswordState.text,
        isError = kcpCardPasswordState.isError,
        supportingText = supportingText,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateKcpCardPassword(value))
        },
        shouldFocus = kcpCardPasswordState.isFocused,
        inputTransformation = inputTransformation,
        isSecureField = true,
    )
}

@Preview
@Composable
private fun KCPCardPasswordFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemeWrapper(theme) {
        KCPCardPasswordField(
            kcpCardPasswordState = TextInputViewState(
                text = "12",
            ),
            onIntent = {},
        )
    }
}

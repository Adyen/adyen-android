/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.properties.KCPBirthDateOrTaxNumberProperties.KCP_BIRTH_DATE_OR_TAX_NUMBER_MAX_LENGTH
import com.adyen.checkout.card.internal.ui.properties.KCPBirthDateOrTaxNumberProperties.KCP_BIRTH_DATE_VALID_LENGTH
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.TextFieldStylePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.internal.theme.toCompose
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun KCPBirthDateOrTaxNumberField(
    kcpBirthDateOrTaxNumberState: TextInputViewState,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textLength = kcpBirthDateOrTaxNumberState.text.length

    val label = if (textLength > KCP_BIRTH_DATE_VALID_LENGTH) {
        resolveString(CheckoutLocalizationKey.CARD_KCP_TAX_NUMBER)
    } else {
        resolveString(CheckoutLocalizationKey.CARD_KCP_BIRTH_DATE_OR_TAX_NUMBER)
    }

    val supportingText = kcpBirthDateOrTaxNumberState.supportingText?.let { resolveString(it) }

    val inputTransformation = remember {
        DigitOnlyInputTransformation(
            maxLengthWithoutSeparators = KCP_BIRTH_DATE_OR_TAX_NUMBER_MAX_LENGTH,
        )
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumberFocus(focusState.isFocused))
            },
        label = label,
        initialValue = kcpBirthDateOrTaxNumberState.text,
        isError = kcpBirthDateOrTaxNumberState.isError,
        supportingText = supportingText,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateKcpBirthDateOrTaxNumber(value))
        },
        shouldFocus = kcpBirthDateOrTaxNumberState.isFocused,
        inputTransformation = inputTransformation,
    )
}

@Preview
@Composable
private fun KCPBirthDateOrTaxNumberFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Spacing.Large),
        ) {
            KCPBirthDateOrTaxNumberField(
                kcpBirthDateOrTaxNumberState = TextInputViewState(
                    text = "230704",
                ),
                onIntent = {},
            )

            KCPBirthDateOrTaxNumberField(
                kcpBirthDateOrTaxNumberState = TextInputViewState(
                    text = "1234567890",
                    isError = true,
                ),
                onIntent = {},
            )
        }
    }
}

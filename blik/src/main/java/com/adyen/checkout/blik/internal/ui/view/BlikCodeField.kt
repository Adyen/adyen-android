/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2026.
 */

package com.adyen.checkout.blik.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.blik.internal.ui.state.BlikIntent
import com.adyen.checkout.core.common.internal.properties.BlikCodeProperties.BLIK_CODE_MAX_LENGTH
import com.adyen.checkout.core.common.internal.properties.BlikCodeProperties.BLIK_CODE_SEPARATOR
import com.adyen.checkout.core.common.internal.properties.BlikCodeProperties.BLIK_CODE_SEPARATORS
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.SeparatorsOutputTransformation
import com.adyen.checkout.ui.internal.element.input.TextFieldStylePreviewParameterProvider
import com.adyen.checkout.ui.internal.helper.CheckoutThemeWrapper
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun BlikCodeField(
    blikCodeState: TextInputViewState,
    onIntent: (BlikIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingText = blikCodeState.supportingText?.let { resolveString(it) }

    val inputTransformation = remember {
        DigitOnlyInputTransformation(
            allowedSeparators = listOf(BLIK_CODE_SEPARATOR),
            maxLengthWithoutSeparators = BLIK_CODE_MAX_LENGTH,
        )
    }

    val outputTransformation = remember {
        SeparatorsOutputTransformation(BLIK_CODE_SEPARATORS)
    }
    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(BlikIntent.UpdateBlikCodeFocus(focusState.hasFocus))
            },
        label = resolveString(CheckoutLocalizationKey.BLIK_CODE),
        initialValue = blikCodeState.text,
        isError = blikCodeState.isError,
        supportingText = supportingText,
        onValueChange = { value ->
            onIntent(BlikIntent.UpdateBlikCode(value))
        },
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        shouldFocus = blikCodeState.isFocused,
    )
}

@Preview
@Composable
private fun BlikCodeFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemeWrapper(theme) {
        BlikCodeField(
            blikCodeState = TextInputViewState(
                text = "123456",
            ),
            onIntent = {},
        )
    }
}

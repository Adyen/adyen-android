/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2026.
 */

package com.adyen.checkout.mbway.internal.ui.view

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
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.TextFieldStylePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.internal.theme.toCompose
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun MBWayPhoneNumberField(
    mbWayPhoneNumberFieldState: TextInputViewState,
    countryCode: String,
    onIntent: (MBWayIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextPhoneNumber = mbWayPhoneNumberFieldState.supportingText?.let { resolveString(it) }
    val inputTransformation = remember { DigitOnlyInputTransformation() }
    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(MBWayIntent.UpdatePhoneNumberFocus(focusState.hasFocus))
            },
        label = resolveString(CheckoutLocalizationKey.MBWAY_PHONE_NUMBER),
        initialValue = mbWayPhoneNumberFieldState.text,
        isError = mbWayPhoneNumberFieldState.isError,
        supportingText = supportingTextPhoneNumber,
        prefix = countryCode,
        onValueChange = { value ->
            onIntent(MBWayIntent.UpdatePhoneNumber(value))
        },
        inputTransformation = inputTransformation,
        shouldFocus = mbWayPhoneNumberFieldState.isFocused,
    )
}

@Preview
@Composable
private fun MBWayPhoneNumberFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
            modifier = Modifier
                .background(theme.colors.background.toCompose())
                .padding(Dimensions.Spacing.Large),
        ) {
            MBWayPhoneNumberField(
                mbWayPhoneNumberFieldState = TextInputViewState(
                    text = "12345612",
                ),
                countryCode = "+31",
                onIntent = {},
            )
        }
    }
}

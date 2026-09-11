/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.card.internal.ui.state.CardNumberFormat
import com.adyen.checkout.core.common.internal.properties.SecurityCodeProperties.SECURITY_CODE_MAX_LENGTH_AMEX
import com.adyen.checkout.core.common.internal.properties.SecurityCodeProperties.SECURITY_CODE_MAX_LENGTH_DEFAULT
import com.adyen.checkout.core.common.internal.ui.CheckoutTextFieldTrailingIcon
import com.adyen.checkout.core.common.internal.ui.toImeAction
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TrailingIcon
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun SecurityCodeField(
    securityCodeState: TextInputViewState,
    cardNumberFormat: CardNumberFormat,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusRequestConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextSecurityCode = securityCodeState.supportingText?.let { resolveString(it) }
        ?: resolveString(
            when (cardNumberFormat) {
                CardNumberFormat.AMEX -> CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_4_DIGITS
                CardNumberFormat.DEFAULT -> CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_3_DIGITS
            },
        )

    val labelSuffix = if (securityCodeState.isOptional) {
        " ${resolveString(CheckoutLocalizationKey.GENERAL_OPTIONAL)}"
    } else {
        ""
    }

    val inputTransformation = remember(cardNumberFormat) {
        val maxLength = when (cardNumberFormat) {
            CardNumberFormat.AMEX -> SECURITY_CODE_MAX_LENGTH_AMEX
            CardNumberFormat.DEFAULT -> SECURITY_CODE_MAX_LENGTH_DEFAULT
        }
        DigitOnlyInputTransformation(
            maxLengthWithoutSeparators = maxLength,
        )
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        label = resolveString(key = CheckoutLocalizationKey.CARD_SECURITY_CODE) + labelSuffix,
        state = rememberTextFieldStateWithCurrentValue(securityCodeState.text),
        isError = securityCodeState.isError,
        supportingText = supportingTextSecurityCode,
        onValueChange = onValueChange,
        inputTransformation = inputTransformation,
        focusRequest = securityCodeState.focusRequest,
        onFocusRequestConsumed = onFocusRequestConsumed,
        imeAction = securityCodeState.keyboardAction.toImeAction(),
        trailingIcon = {
            SecurityCodeTrailingIcon(securityCodeState.trailingIcon)
        },
    )
}

@Composable
private fun SecurityCodeTrailingIcon(
    trailingIcon: TrailingIcon,
) {
    CheckoutTextFieldTrailingIcon(trailingIcon) { state ->
        // unexpected state - the view state producer should set the correct type
        if (state !is SecurityCodeTrailingIcon) return@CheckoutTextFieldTrailingIcon

        when (state) {
            SecurityCodeTrailingIcon.Checkmark -> SecurityCodeTrailingIcon(
                resourceId = com.adyen.checkout.test.R.drawable.ic_checkmark,
                tint = CheckoutThemeProvider.colors.primary,
            )

            SecurityCodeTrailingIcon.PlaceholderAmex -> SecurityCodeTrailingIcon(
                resourceId = getThemedIcon(
                    backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
                    lightDrawableId = R.drawable.ic_card_cvc_front_light,
                    darkDrawableId = R.drawable.ic_card_cvc_front_dark,
                ),
                tint = Color.Unspecified,
            )

            SecurityCodeTrailingIcon.PlaceholderDefault -> SecurityCodeTrailingIcon(
                resourceId = getThemedIcon(
                    backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
                    lightDrawableId = R.drawable.ic_card_cvc_back_light,
                    darkDrawableId = R.drawable.ic_card_cvc_back_dark,
                ),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun SecurityCodeTrailingIcon(
    resourceId: Int,
    tint: Color,
) {
    Icon(
        modifier = Modifier.size(Dimensions.LogoSize.small),
        imageVector = ImageVector.vectorResource(resourceId),
        contentDescription = null,
        tint = tint,
    )
}

@Preview
@Composable
private fun SecurityCodeFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        SecurityCodeField(
            securityCodeState = TextInputViewState(
                customTrailingIcon = SecurityCodeTrailingIcon.PlaceholderDefault,
            ),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )

        SecurityCodeField(
            securityCodeState = TextInputViewState(
                isOptional = true,
                customTrailingIcon = SecurityCodeTrailingIcon.PlaceholderAmex,
            ),
            cardNumberFormat = CardNumberFormat.AMEX,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )

        val focusRequester = remember { FocusRequester() }
        SecurityCodeField(
            securityCodeState = TextInputViewState(
                text = "123",
                customTrailingIcon = SecurityCodeTrailingIcon.Checkmark,
            ),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            modifier = Modifier.focusRequester(focusRequester),
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        SecurityCodeField(
            securityCodeState = TextInputViewState(
                text = "12",
                isError = true,
            ),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )
    }
}

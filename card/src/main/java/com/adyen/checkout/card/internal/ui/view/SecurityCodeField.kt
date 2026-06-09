/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.TextFieldStylePreviewParameterProvider
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemeWrapper
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
    modifier: Modifier = Modifier,
) {
    SecurityCodeFieldInternal(
        securityCodeState = securityCodeState,
        cardNumberFormat = cardNumberFormat,
        onSecurityCodeChanged = onValueChange,
        onSecurityCodeFocusChanged = onFocusChange,
        modifier = modifier,
    )
}

@Composable
internal fun StoredCardSecurityCodeField(
    securityCodeState: TextInputViewState,
    cardNumberFormat: CardNumberFormat,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityCodeFieldInternal(
        securityCodeState = securityCodeState,
        cardNumberFormat = cardNumberFormat,
        onSecurityCodeChanged = onValueChange,
        onSecurityCodeFocusChanged = onFocusChange,
        modifier = modifier,
    )
}

@Composable
private fun SecurityCodeFieldInternal(
    securityCodeState: TextInputViewState,
    cardNumberFormat: CardNumberFormat,
    onSecurityCodeChanged: (String) -> Unit,
    onSecurityCodeFocusChanged: (Boolean) -> Unit,
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
                onSecurityCodeFocusChanged(focusState.isFocused)
            },
        label = resolveString(key = CheckoutLocalizationKey.CARD_SECURITY_CODE) + labelSuffix,
        state = rememberTextFieldStateWithCurrentValue(securityCodeState.text),
        isError = securityCodeState.isError,
        supportingText = supportingTextSecurityCode,
        onValueChange = onSecurityCodeChanged,
        inputTransformation = inputTransformation,
        shouldFocus = securityCodeState.isFocused,
        trailingIcon = {
            SecurityCodeIcon(state = securityCodeState)
        },
    )
}

@Composable
private fun SecurityCodeIcon(
    state: TextInputViewState,
    modifier: Modifier = Modifier,
) {
    val resourceId: Int
    val tint: Color
    when (state.trailingIcon as? SecurityCodeTrailingIcon) {
        SecurityCodeTrailingIcon.Warning -> {
            resourceId = com.adyen.checkout.test.R.drawable.ic_warning
            tint = CheckoutThemeProvider.colors.destructive
        }

        SecurityCodeTrailingIcon.Checkmark -> {
            resourceId = com.adyen.checkout.test.R.drawable.ic_checkmark
            tint = CheckoutThemeProvider.colors.primary
        }

        SecurityCodeTrailingIcon.PlaceholderAmex -> {
            resourceId = getThemedIcon(
                backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
                lightDrawableId = R.drawable.ic_card_cvc_front_light,
                darkDrawableId = R.drawable.ic_card_cvc_front_dark,
            )
            tint = Color.Unspecified
        }

        else -> {
            resourceId = getThemedIcon(
                backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
                lightDrawableId = R.drawable.ic_card_cvc_back_light,
                darkDrawableId = R.drawable.ic_card_cvc_back_dark,
            )
            tint = Color.Unspecified
        }
    }

    AnimatedContent(
        targetState = resourceId,
        modifier = modifier,
        label = "SecurityCodeIcon",
    ) { targetResourceId ->
        Icon(
            modifier = Modifier.size(Dimensions.LogoSize.small),
            imageVector = ImageVector.vectorResource(targetResourceId),
            contentDescription = null,
            tint = tint,
        )
    }
}

@Preview
@Composable
private fun SecurityCodeFieldPreview(
    @PreviewParameter(TextFieldStylePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemeWrapper(theme) {
        SecurityCodeField(
            securityCodeState = TextInputViewState(
                text = "123",
            ),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
        )

        SecurityCodeField(
            securityCodeState = TextInputViewState(
                isOptional = true,
            ),
            cardNumberFormat = CardNumberFormat.AMEX,
            onValueChange = {},
            onFocusChange = {},
        )

        SecurityCodeField(
            securityCodeState = TextInputViewState(
                text = "123",
                isError = true,
                trailingIcon = SecurityCodeTrailingIcon.Warning,
            ),
            cardNumberFormat = CardNumberFormat.DEFAULT,
            onValueChange = {},
            onFocusChange = {},
        )
    }
}

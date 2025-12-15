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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun SecurityCodeField(
    securityCodeState: TextInputComponentState,
    isAmex: Boolean?,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showSecurityCodeError =
        securityCodeState.errorMessage != null && securityCodeState.showError
    val supportingTextSecurityCode = if (showSecurityCodeError) {
        securityCodeState.errorMessage?.let { resolveString(it) }
    } else {
        resolveString(
            if (isAmex == null || !isAmex) {
                CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_3_DIGITS
            } else {
                CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_4_DIGITS
            },
        )
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateSecurityCodeFocus(focusState.isFocused))
            },
        label = resolveString(CheckoutLocalizationKey.CARD_SECURITY_CODE),
        initialValue = securityCodeState.text,
        isError = showSecurityCodeError,
        supportingText = supportingTextSecurityCode,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateSecurityCode(value))
        },
        inputTransformation = DigitOnlyInputTransformation().maxLength(
            if (isAmex == false) {
                MAX_LENGTH_SECURITY_CODE_DEFAULT
            } else {
                MAX_LENGTH_SECURITY_CODE_AMEX
            },
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shouldFocus = securityCodeState.isFocused,
        trailingIcon = {
            SecurityCodeIcon(state = securityCodeState, isAmex = isAmex)
        },
    )
}

@Composable
private fun SecurityCodeIcon(
    state: TextInputComponentState,
    isAmex: Boolean?,
    modifier: Modifier = Modifier,
) {
    val isValid = state.errorMessage == null
    val isInvalid = state.errorMessage != null && state.showError
    val resourceId = when {
        isInvalid -> com.adyen.checkout.test.R.drawable.ic_warning
        isValid -> com.adyen.checkout.test.R.drawable.ic_checkmark
        isAmex == true -> getThemedIcon(
            backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
            lightDrawableId = R.drawable.ic_card_cvc_front_light,
            darkDrawableId = R.drawable.ic_card_cvc_front_dark,
        )

        else -> getThemedIcon(
            backgroundColor = CheckoutThemeProvider.elements.textField.backgroundColor,
            lightDrawableId = R.drawable.ic_card_cvc_back_light,
            darkDrawableId = R.drawable.ic_card_cvc_back_dark,
        )
    }

    AnimatedContent(
        targetState = resourceId,
        modifier = modifier,
        label = "ExpiryDateIcon",
    ) { targetResourceId ->
        val iconSize = remember(isInvalid) {
            if (isInvalid) {
                Dimensions.LogoSize.smallSquare
            } else {
                Dimensions.LogoSize.small
            }
        }

        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = ImageVector.vectorResource(targetResourceId),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}

private const val MAX_LENGTH_SECURITY_CODE_DEFAULT = 3
private const val MAX_LENGTH_SECURITY_CODE_AMEX = 4

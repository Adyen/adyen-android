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
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.StoredCardIntent
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun SecurityCodeField(
    securityCodeState: TextInputViewState,
    isAmex: Boolean?,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityCodeFieldInternal(
        securityCodeState = securityCodeState,
        isAmex = isAmex,
        onSecurityCodeChanged = { onIntent(CardIntent.UpdateSecurityCode(it)) },
        onSecurityCodeFocusChanged = { onIntent(CardIntent.UpdateSecurityCodeFocus(it)) },
        modifier = modifier,
    )
}

@Composable
internal fun StoredCardSecurityCodeField(
    securityCodeState: TextInputViewState,
    isAmex: Boolean?,
    onIntent: (StoredCardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecurityCodeFieldInternal(
        securityCodeState = securityCodeState,
        isAmex = isAmex,
        onSecurityCodeChanged = { onIntent(StoredCardIntent.UpdateSecurityCode(it)) },
        onSecurityCodeFocusChanged = { onIntent(StoredCardIntent.UpdateSecurityCodeFocus(it)) },
        modifier = modifier,
    )
}

@Composable
private fun SecurityCodeFieldInternal(
    securityCodeState: TextInputViewState,
    isAmex: Boolean?,
    onSecurityCodeChanged: (String) -> Unit,
    onSecurityCodeFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextSecurityCode = securityCodeState.supportingText?.let { resolveString(it) }
        ?: resolveString(
            if (isAmex == null || !isAmex) {
                CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_3_DIGITS
            } else {
                CheckoutLocalizationKey.CARD_SECURITY_CODE_HINT_4_DIGITS
            },
        )

    val labelSuffix = if (securityCodeState.requirementPolicy is RequirementPolicy.Optional) {
        " ${resolveString(CheckoutLocalizationKey.GENERAL_OPTIONAL)}"
    } else {
        ""
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onSecurityCodeFocusChanged(focusState.isFocused)
            },
        label = resolveString(key = CheckoutLocalizationKey.CARD_SECURITY_CODE) + labelSuffix,
        initialValue = securityCodeState.text,
        isError = securityCodeState.isError,
        supportingText = supportingTextSecurityCode,
        onValueChange = { value ->
            onSecurityCodeChanged(value)
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
            SecurityCodeIcon(state = securityCodeState)
        },
    )
}

@Composable
private fun SecurityCodeIcon(
    state: TextInputViewState,
    modifier: Modifier = Modifier,
) {
    val isInvalid = state.trailingIcon == SecurityCodeTrailingIcon.Warning
    val resourceId = when (state.trailingIcon as? SecurityCodeTrailingIcon) {
        SecurityCodeTrailingIcon.Warning -> com.adyen.checkout.test.R.drawable.ic_warning
        SecurityCodeTrailingIcon.Checkmark -> com.adyen.checkout.test.R.drawable.ic_checkmark
        SecurityCodeTrailingIcon.PlaceholderAmex -> getThemedIcon(
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

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/11/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.ui.internal.CheckoutTextField
import com.adyen.checkout.ui.internal.DigitOnlyInputTransformation

@Composable
internal fun SecurityCodeField(
    securityCodeState: TextInputState,
    onSecurityCodeChanged: (String) -> Unit,
    onSecurityCodeFocusChanged: (Boolean) -> Unit,
    isAmex: Boolean?,
    modifier: Modifier = Modifier,
) {
    val showSecurityCodeError =
        securityCodeState.errorMessage != null && securityCodeState.showError
    val supportingTextSecurityCode = if (showSecurityCodeError) {
        securityCodeState.errorMessage?.let { resolveString(it) }
    } else {
        null
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onSecurityCodeFocusChanged(focusState.isFocused)
            },
        label = resolveString(CheckoutLocalizationKey.CARD_SECURITY_CODE),
        initialValue = securityCodeState.text,
        isError = showSecurityCodeError,
        supportingText = supportingTextSecurityCode,
        onValueChange = { value ->
            onSecurityCodeChanged(value)
        },
        inputTransformation = DigitOnlyInputTransformation().maxLength(
            if (isAmex == false) {
                MAX_LENGTH_SECURITY_CODE_DEFAULT
            } else {
                MAX_LENGTH_SECURITY_CODE_AMEX
            }
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shouldFocus = securityCodeState.isFocused,
    )
}

private const val MAX_LENGTH_SECURITY_CODE_DEFAULT = 3
private const val MAX_LENGTH_SECURITY_CODE_AMEX = 4

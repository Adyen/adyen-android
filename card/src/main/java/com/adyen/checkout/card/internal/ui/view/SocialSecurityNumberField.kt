/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.core.common.internal.properties.SocialSecurityNumberProperties.SOCIAL_SECURITY_MAX_LENGTH
import com.adyen.checkout.core.common.internal.properties.SocialSecurityNumberProperties.SOCIAL_SECURITY_SEPARATORS
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation

@Composable
internal fun SocialSecurityNumberField(
    socialSecurityNumberState: TextInputViewState,
    onIntent: (CardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingTextSocialSecurityNumber = socialSecurityNumberState.supportingText?.let { resolveString(it) }

    val inputTransformation = remember {
        DigitOnlyInputTransformation(
            allowedSeparators = SOCIAL_SECURITY_SEPARATORS,
            maxLengthWithoutSeparators = SOCIAL_SECURITY_MAX_LENGTH,
        )
    }
    val outputTransformation = remember { SocialSecurityNumberOutputTransformation() }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onIntent(CardIntent.UpdateSocialSecurityNumberFocus(focusState.isFocused))
            },
        label = resolveString(CheckoutLocalizationKey.CARD_SOCIAL_SECURITY_NUMBER),
        initialValue = socialSecurityNumberState.text,
        isError = socialSecurityNumberState.isError,
        supportingText = supportingTextSocialSecurityNumber,
        onValueChange = { value ->
            onIntent(CardIntent.UpdateSocialSecurityNumber(value))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shouldFocus = socialSecurityNumberState.isFocused,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
    )
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2026.
 */

package com.adyen.checkout.mbway.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation

@Composable
internal fun MBWayPhoneNumberField(
    mbWayPhoneNumberFieldState: TextInputViewState,
    country: CountryModel,
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
        prefix = country.callingCode,
        onValueChange = { value ->
            onIntent(MBWayIntent.UpdatePhoneNumber(value))
        },
        inputTransformation = inputTransformation,
        shouldFocus = mbWayPhoneNumberFieldState.isFocused,
    )
}

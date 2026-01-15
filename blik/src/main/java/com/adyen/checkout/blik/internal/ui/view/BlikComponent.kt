/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.blik.internal.ui.state.BlikIntent
import com.adyen.checkout.blik.internal.ui.state.BlikViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.button.PayButton
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun BlikComponent(
    viewState: BlikViewState,
    onSubmitClick: () -> Unit,
    onIntent: (BlikIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = viewState.isLoading,
        footer = {
            PayButton(onClick = onSubmitClick, isLoading = viewState.isLoading)
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        ) {
            // Helper text
            Body(text = resolveString(CheckoutLocalizationKey.BLIK_HELPER_TEXT))

            // Blik Code Input
            val supportingText = viewState.blikCode.supportingText?.let { resolveString(it) }
            CheckoutTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        onIntent(BlikIntent.UpdateBlikCodeFocus(focusState.hasFocus))
                    },
                label = resolveString(CheckoutLocalizationKey.BLIK_CODE),
                initialValue = viewState.blikCode.text,
                isError = viewState.blikCode.isError,
                supportingText = supportingText,
                onValueChange = { value ->
                    onIntent(BlikIntent.UpdateBlikCode(value))
                },
                inputTransformation = DigitOnlyInputTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shouldFocus = viewState.blikCode.isFocused,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlikComponentPreview() {
    BlikComponent(
        viewState = BlikViewState(
            blikCode = TextInputViewState(),
            isLoading = false,
        ),
        onIntent = {},
        onSubmitClick = {},
    )
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.card.internal.ui.state.CardChangeListener
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.core.old.ui.validation.CardNumberValidator
import com.adyen.checkout.ui.internal.CheckoutTextField
import com.adyen.checkout.ui.internal.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.Dimensions

@Composable
internal fun CardComponent(
    viewState: CardViewState,
    changeListener: CardChangeListener,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
    ) {
        val showCardNumberError =
            viewState.cardNumber.errorMessage != null && viewState.cardNumber.showError
        val supportingTextCardNumber = if (showCardNumberError) {
            viewState.cardNumber.errorMessage?.let { resolveString(it) }
        } else {
            null
        }

        val outputTransformation = remember(viewState.isAmex) {
            CardNumberOutputTransformation(isAmex = viewState.isAmex)
        }

        CheckoutTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                },
            label = resolveString(CheckoutLocalizationKey.CARD_NUMBER),
            initialValue = viewState.cardNumber.text,
            isError = showCardNumberError,
            supportingText = supportingTextCardNumber,
            onValueChange = { value ->
                changeListener.onCardNumberChanged(value)
            },
            inputTransformation = DigitOnlyInputTransformation().maxLength(
                maxLength = CardNumberValidator.MAXIMUM_CARD_NUMBER_LENGTH,
            ),
            outputTransformation = outputTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shouldFocus = viewState.cardNumber.isFocused,
        )
    }
    // TODO - Card Full UI
}

@Preview(showBackground = true)
@Composable
private fun CardComponentPreview() {
    CardComponent(
        viewState = CardViewState(
            cardNumber = TextInputState(
                "5555444433331111",
            ),
            isAmex = false,
        ),
        changeListener = object : CardChangeListener {
            override fun onCardNumberChanged(newCardNumber: String) = Unit
        },
    )
}

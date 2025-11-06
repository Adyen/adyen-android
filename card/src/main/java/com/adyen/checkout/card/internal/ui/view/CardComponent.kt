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
import com.adyen.checkout.core.common.helper.CardNumberValidator
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.ui.internal.CheckoutTextField
import com.adyen.checkout.ui.internal.ComponentScaffold
import com.adyen.checkout.ui.internal.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.Dimensions
import com.adyen.checkout.ui.internal.PayButton

@Composable
internal fun CardComponent(
    viewState: CardViewState,
    changeListener: CardChangeListener,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        footer = {
            PayButton(onClick = onSubmitClick, isLoading = viewState.isLoading)
        },
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
        ) {
            CardNumberField(
                cardNumberState = viewState.cardNumber,
                isAmex = viewState.isAmex,
                onCardNumberChanged = changeListener::onCardNumberChanged,
                onCardNumberFocusChanged = changeListener::onCardNumberFocusChanged,
            )
        }
    }
    // TODO - Card Full UI
}

@Composable
private fun CardNumberField(
    cardNumberState: TextInputState,
    isAmex: Boolean,
    onCardNumberChanged: (String) -> Unit,
    onCardNumberFocusChanged: (Boolean) -> Unit,
) {
    val showCardNumberError =
        cardNumberState.errorMessage != null && cardNumberState.showError
    val supportingTextCardNumber = if (showCardNumberError) {
        cardNumberState.errorMessage?.let { resolveString(it) }
    } else {
        null
    }

    val outputTransformation = remember(isAmex) {
        CardNumberOutputTransformation(isAmex = isAmex)
    }

    CheckoutTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onCardNumberFocusChanged(focusState.isFocused)
            },
        label = resolveString(CheckoutLocalizationKey.CARD_NUMBER),
        initialValue = cardNumberState.text,
        isError = showCardNumberError,
        supportingText = supportingTextCardNumber,
        onValueChange = { value ->
            onCardNumberChanged(value)
        },
        inputTransformation = DigitOnlyInputTransformation().maxLength(
            maxLength = CardNumberValidator.MAXIMUM_CARD_NUMBER_LENGTH,
        ),
        outputTransformation = outputTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shouldFocus = cardNumberState.isFocused,
    )
}

@Preview(showBackground = true)
@Composable
private fun CardComponentPreview() {
    CardComponent(
        viewState = CardViewState(
            cardNumber = TextInputState(
                "5555444433331111",
            ),
            supportedCardBrands = emptyList(),
            isSupportedCardBrandsShown = false,
            isAmex = false,
            isLoading = false,
        ),
        changeListener = object : CardChangeListener {
            override fun onCardNumberChanged(newCardNumber: String) = Unit

            override fun onCardNumberFocusChanged(hasFocus: Boolean) = Unit
        },
        onSubmitClick = {},
    )
}

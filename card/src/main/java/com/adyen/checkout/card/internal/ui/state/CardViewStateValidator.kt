/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ViewStateValidator

internal class CardViewStateValidator(
    private val cardValidationMapper: CardValidationMapper,
) : ViewStateValidator<CardViewState, CardComponentState> {

    override fun validate(viewState: CardViewState, componentState: CardComponentState): CardViewState {
        val cardNumber = viewState.cardNumber
        // TODO - Card Full Validation

        val isReliable = componentState.detectedCardTypes.any { it.isReliable }
        val filteredDetectedCardTypes = componentState.detectedCardTypes.filter { it.isSupported }

        val selectedOrFirstCardType = filteredDetectedCardTypes.firstOrNull()

        // perform a Luhn Check if no brands are detected
        val enableLuhnCheck = selectedOrFirstCardType?.enableLuhnCheck ?: true
        val shouldFailWithUnsupportedBrand = selectedOrFirstCardType == null && isReliable

        val cardNumberError = cardValidationMapper.mapCardNumberValidation(
            // TODO - Card Number Luhn Check & Brand Supported
            validation = CardValidationUtils.validateCardNumber(
                number = cardNumber.text,
                enableLuhnCheck = enableLuhnCheck,
                isBrandSupported = !shouldFailWithUnsupportedBrand,
            ),
        )

        return viewState.copy(
            cardNumber = cardNumber.copy(errorMessage = cardNumberError),
        )
    }

    override fun isValid(viewState: CardViewState): Boolean {
        // TODO - Card Full Validation
        return viewState.cardNumber.errorMessage == null
    }

    override fun highlightAllValidationErrors(viewState: CardViewState): CardViewState {
        val hasCardNumberError = viewState.cardNumber.errorMessage != null

        return viewState.copy(
            cardNumber = viewState.cardNumber.copy(
                showError = hasCardNumberError,
                isFocused = hasCardNumberError,
            ),
        )
    }
}

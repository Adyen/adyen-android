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
) : ViewStateValidator<CardViewState> {

    override fun validate(viewState: CardViewState): CardViewState {
        val cardNumber = viewState.cardNumber
        // TODO - Card Full Validation
        val cardNumberError = cardValidationMapper.mapCardNumberValidation(
            // TODO - Card Number Luhn Check & Brand Supported
            validation = CardValidationUtils.validateCardNumber(
                number = cardNumber.text,
                // TODO - Card Enable Luhn Check from component state
                enableLuhnCheck = true,
                isBrandSupported = true,
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
        TODO("Not yet implemented")
    }
}

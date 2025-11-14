/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ViewStateValidator
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState

internal class CardViewStateValidator(
    private val cardValidationMapper: CardValidationMapper,
) : ViewStateValidator<CardViewState, CardComponentState> {

    override fun validate(
        viewState: CardViewState,
        componentState: CardComponentState
    ): CardViewState {
        val isReliable = componentState.detectedCardTypes.any { it.isReliable }
        val filteredDetectedCardTypes = componentState.detectedCardTypes.filter { it.isSupported }
        val selectedOrFirstCardType = filteredDetectedCardTypes.firstOrNull()

        val cardNumber = viewState.cardNumber
        val cardNumberError = validateCardNumber(cardNumber, selectedOrFirstCardType, isReliable)

        val expiryDate = viewState.expiryDate
        val expiryDateError = validateExpiryDate(expiryDate, selectedOrFirstCardType)

        // TODO - Card. Security Code UI State.
        val securityCode = viewState.securityCode
        val securityCodeError = validateSecurityCode(securityCode, selectedOrFirstCardType, InputFieldUIState.REQUIRED)

        return viewState.copy(
            cardNumber = cardNumber.copy(errorMessage = cardNumberError),
            expiryDate = expiryDate.copy(errorMessage = expiryDateError),
            securityCode = securityCode.copy(errorMessage = securityCodeError),
            // TODO - State: Create an updater logic which would update the viewState when component state is updated
            isSupportedCardBrandsShown = filteredDetectedCardTypes.isEmpty(),
            detectedBrand = selectedOrFirstCardType?.cardBrand
        )
    }

    override fun isValid(viewState: CardViewState): Boolean {
        // TODO - Card Full Validation
        return viewState.cardNumber.errorMessage == null &&
            viewState.expiryDate.errorMessage == null &&
            viewState.securityCode.errorMessage == null
    }

    override fun highlightAllValidationErrors(viewState: CardViewState): CardViewState {
        val hasCardNumberError = viewState.cardNumber.errorMessage != null
        val hasExpiryDateError = viewState.expiryDate.errorMessage != null
        val hasSecurityCodeError = viewState.expiryDate.errorMessage != null

        return viewState.copy(
            cardNumber = viewState.cardNumber.copy(
                showError = hasCardNumberError,
                isFocused = hasCardNumberError,
            ),
            expiryDate = viewState.expiryDate.copy(
                showError = hasExpiryDateError,
                isFocused = hasExpiryDateError && !hasCardNumberError,
            ),
            securityCode = viewState.securityCode.copy(
                showError = hasSecurityCodeError,
                isFocused = hasSecurityCodeError && !hasCardNumberError && !hasExpiryDateError,
            )
        )
    }

    private fun validateCardNumber(
        cardNumber: TextInputState,
        selectedOrFirstCardType: DetectedCardType?,
        isReliable: Boolean,
    ): CheckoutLocalizationKey? {
        val cardNumber = cardNumber
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

        return cardNumberError
    }

    private fun validateExpiryDate(
        expiryDate: TextInputState,
        selectedOrFirstCardType: DetectedCardType?,
    ): CheckoutLocalizationKey? {
        val expiryDateError = cardValidationMapper.mapExpiryDateValidation(
            validation = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate.text,
                fieldPolicy = selectedOrFirstCardType?.expiryDatePolicy,
            )
        )

        return expiryDateError
    }

    private fun validateSecurityCode(
        securityCode: TextInputState,
        selectedOrFirstCardType: DetectedCardType?,
        uiState: InputFieldUIState,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapSecurityCodeValidation(
            validation = CardValidationUtils.validateSecurityCode(
                securityCode = securityCode.text,
                detectedCardType = selectedOrFirstCardType,
                uiState = uiState,
            )
        )
    }
}

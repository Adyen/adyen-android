/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer

internal class CardComponentStateReducer : ComponentStateReducer<CardComponentState, CardIntent> {

    override fun reduce(state: CardComponentState, intent: CardIntent): CardComponentState {
        return when (intent) {
            is CardIntent.UpdateCardNumber -> state.copy(
                cardNumber = state.cardNumber.updateText(intent.number),
            )

            is CardIntent.UpdateCardNumberFocus -> state.copy(
                cardNumber = state.cardNumber.updateFocus(intent.hasFocus),
            )

            is CardIntent.UpdateExpiryDate -> state.copy(
                expiryDate = state.expiryDate.updateText(intent.expiryDate),
            )

            is CardIntent.UpdateExpiryDateFocus -> state.copy(
                expiryDate = state.expiryDate.updateFocus(intent.hasFocus),
            )

            is CardIntent.UpdateSecurityCode -> state.copy(
                securityCode = state.securityCode.updateText(intent.securityCode),
            )

            is CardIntent.UpdateSecurityCodeFocus -> state.copy(
                securityCode = state.securityCode.updateFocus(intent.hasFocus),
            )

            is CardIntent.UpdateHolderName -> state.copy(
                holderName = state.holderName.updateText(intent.holderName),
            )

            is CardIntent.UpdateHolderNameFocus -> state.copy(
                holderName = state.holderName.updateFocus(intent.hasFocus),
            )

            is CardIntent.UpdateStorePaymentMethod -> state.copy(
                storePaymentMethod = intent.isChecked,
            )

            is CardIntent.SelectBrand -> state.copy(
                selectedCardBrand = intent.cardBrand,
            )

            is CardIntent.UpdateDetectedCardTypes -> state.copy(
                detectedCardTypes = intent.detectedCardTypes,
            )

            is CardIntent.UpdateLoading -> state.copy(
                isLoading = intent.isLoading,
            )

            is CardIntent.HighlightValidationErrors -> highlightValidationErrors(state)
        }
    }

    private fun highlightValidationErrors(state: CardComponentState): CardComponentState {
        val hasCardNumberError = state.cardNumber.errorMessage != null
        val hasExpiryDateError = state.expiryDate.errorMessage != null
        val hasSecurityCodeError = state.securityCode.errorMessage != null
        val hasHolderNameError = state.holderName.errorMessage != null

        return state.copy(
            cardNumber = state.cardNumber.copy(
                showError = hasCardNumberError,
                isFocused = hasCardNumberError,
            ),
            expiryDate = state.expiryDate.copy(
                showError = hasExpiryDateError,
                isFocused = hasExpiryDateError && !hasCardNumberError,
            ),
            securityCode = state.securityCode.copy(
                showError = hasSecurityCodeError,
                isFocused = hasSecurityCodeError && !hasCardNumberError && !hasExpiryDateError,
            ),
            holderName = state.holderName.copy(
                showError = hasHolderNameError,
                isFocused = hasHolderNameError && !hasCardNumberError && !hasExpiryDateError && !hasSecurityCodeError,
            ),
        )
    }
}

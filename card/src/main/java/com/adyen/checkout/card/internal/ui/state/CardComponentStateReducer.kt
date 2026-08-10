/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.ExpiryDateParser
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer

internal class CardComponentStateReducer(
    private val cardBrandIntentsHandler: CardBrandIntentsHandler,
) : ComponentStateReducer<CardComponentState, CardIntent> {

    @Suppress("CyclomaticComplexMethod", "LongMethod")
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

            is CardIntent.UpdateSocialSecurityNumber -> state.copy(
                socialSecurityNumber = state.socialSecurityNumber.updateText(intent.socialSecurityNumber),
            )

            is CardIntent.UpdateSocialSecurityNumberFocus -> state.copy(
                socialSecurityNumber = state.socialSecurityNumber.updateFocus(intent.hasFocus),
            )

            is CardIntent.UpdateKcpBirthDateOrTaxNumber -> state.copy(
                kcpBirthDateOrTaxNumber = state.kcpBirthDateOrTaxNumber.updateText(intent.kcpBirthDateOrTaxNumber),
            )

            is CardIntent.UpdateKcpBirthDateOrTaxNumberFocus -> state.copy(
                kcpBirthDateOrTaxNumber = state.kcpBirthDateOrTaxNumber.updateFocus(intent.hasFocus),
            )

            is CardIntent.UpdateKcpCardPassword -> state.copy(
                kcpCardPassword = state.kcpCardPassword.updateText(intent.kcpCardPassword),
            )

            is CardIntent.UpdateKcpCardPasswordFocus -> state.copy(
                kcpCardPassword = state.kcpCardPassword.updateFocus(intent.hasFocus),
            )

            is CardIntent.UpdatePostalCode -> state.copy(
                postalCode = state.postalCode.updateText(intent.postalCode)
            )

            is CardIntent.UpdatePostalCodeFocus -> state.copy(
                postalCode = state.postalCode.updateFocus(intent.hasFocus)
            )

            is CardIntent.UpdateStorePaymentMethod -> state.copy(
                storePaymentMethod = intent.isChecked,
            )

            is CardIntent.SelectBrand -> {
                cardBrandIntentsHandler.onBrandSelected(state, intent)
            }

            is CardIntent.UpdateDetectedCardTypes -> {
                cardBrandIntentsHandler.onUpdateDetectedCardTypes(state, intent)
            }

            is CardIntent.UpdateInstallment -> state.copy(
                installmentState = state.installmentState.copy(selectedInstallment = intent.installment),
            )

            is CardIntent.UpdateLoading -> state.copy(
                isLoading = intent.isLoading,
            )

            is CardIntent.UpdateCardScanningAvailability -> state.copy(
                isCardScanningAvailable = intent.isAvailable,
            )

            is CardIntent.UpdateCardScanResult -> state.copy(
                cardNumber = state.cardNumber.updateText(intent.pan.orEmpty()),
                expiryDate = state.expiryDate.updateText(
                    ExpiryDateParser.formatToMMyy(intent.expiryMonth, intent.expiryYear),
                ),
            )

            is CardIntent.HighlightValidationErrors -> highlightValidationErrors(state)
        }
    }

    private fun highlightValidationErrors(state: CardComponentState): CardComponentState {
        var isFocusConsumed = false

        fun shouldFocus(hasError: Boolean): Boolean {
            return (hasError && !isFocusConsumed).also { shouldFocus ->
                if (shouldFocus) isFocusConsumed = true
            }
        }

        val hasCardNumberError = !state.cardNumber.isValid
        val hasExpiryDateError = !state.expiryDate.isValid
        val hasSecurityCodeError = !state.securityCode.isValid
        val hasHolderNameError = !state.holderName.isValid
        val hasSocialSecurityNumberError = !state.socialSecurityNumber.isValid
        val hasKcpBirthDateOrTaxNumberError = !state.kcpBirthDateOrTaxNumber.isValid
        val hasKcpCardPasswordError = !state.kcpCardPassword.isValid
        val hasPostalCodeError = !state.postalCode.isValid

        return state.copy(
            cardNumber = state.cardNumber.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasCardNumberError)),
            expiryDate = state.expiryDate.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasExpiryDateError)),
            securityCode = state.securityCode.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasSecurityCodeError)),
            holderName = state.holderName.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasHolderNameError)),
            socialSecurityNumber = state.socialSecurityNumber.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasSocialSecurityNumberError)),
            kcpBirthDateOrTaxNumber = state.kcpBirthDateOrTaxNumber.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasKcpBirthDateOrTaxNumberError)),
            kcpCardPassword = state.kcpCardPassword.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasKcpCardPasswordError)),
            postalCode = state.postalCode.showErrorIfPresent()
                .copy(isFocused = shouldFocus(hasPostalCodeError)),
        )
    }
}

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

    @Suppress("CyclomaticComplexMethod")
    override fun reduce(state: CardComponentState, intent: CardIntent): CardComponentState {
        return when (intent) {
            is CardIntent.UpdateCardNumber -> state.copy(
                cardNumber = state.cardNumber.updateText(intent.number),
            )

            is CardIntent.UpdateExpiryDate -> state.copy(
                expiryDate = state.expiryDate.updateText(intent.expiryDate),
            )

            is CardIntent.UpdateSecurityCode -> state.copy(
                securityCode = state.securityCode.updateText(intent.securityCode),
            )

            is CardIntent.UpdateHolderName -> state.copy(
                holderName = state.holderName.updateText(intent.holderName),
            )

            is CardIntent.UpdateSocialSecurityNumber -> state.copy(
                socialSecurityNumber = state.socialSecurityNumber.updateText(intent.socialSecurityNumber),
            )

            is CardIntent.UpdateKcpBirthDateOrTaxNumber -> state.copy(
                kcpBirthDateOrTaxNumber = state.kcpBirthDateOrTaxNumber.updateText(intent.kcpBirthDateOrTaxNumber),
            )

            is CardIntent.UpdateKcpCardPassword -> state.copy(
                kcpCardPassword = state.kcpCardPassword.updateText(intent.kcpCardPassword),
            )

            is CardIntent.UpdatePostalCode -> state.copy(
                postalCode = state.postalCode.updateText(intent.postalCode),
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

            is CardIntent.UpdateFieldFocus,
            is CardIntent.FocusRequestConsumed,
            is CardIntent.HighlightValidationErrors -> state
        }
    }
}

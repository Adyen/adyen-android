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
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.nextTextInputAfter
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalid
import com.adyen.checkout.core.components.internal.ui.state.model.applyFocusChange

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
                postalCode = state.postalCode.updateText(intent.postalCode)
            )

            is CardIntent.UpdateFieldFocus -> state.updateFieldFocus(intent.id, intent.hasFocus)

            is CardIntent.FocusRequestConsumed -> if (state.focusRequest?.id == intent.id) {
                state.copy(focusRequest = null)
            } else {
                state
            }

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
            ).requestFocusAfterScan(intent)

            is CardIntent.HighlightValidationErrors -> highlightValidationErrors(state)
        }
    }

    /**
     * Moves the shopper to the first field the scan did not fill. A scan can come back with only part of a card, or
     * with nothing usable at all, and a scan that returns no card number also clears the one that was there, so the
     * shopper has to go back rather than forward.
     *
     * This goes by what the scanner returned rather than by what is still invalid, because the reducer runs before the
     * validator: at this point every error still describes the text from before the scan.
     */
    private fun CardComponentState.requestFocusAfterScan(
        intent: CardIntent.UpdateCardScanResult,
    ): CardComponentState {
        val focusTarget = when {
            intent.pan.isNullOrBlank() -> CardFormElementId.CARD_NUMBER
            intent.expiryMonth == null || intent.expiryYear == null -> form.nextTextInputAfter(
                CardFormElementId.CARD_NUMBER
            )
            else -> form.nextTextInputAfter(CardFormElementId.EXPIRY_DATE)
        }
        return copy(focusRequest = focusTarget?.let { FocusRequest(id = it) })
    }

    private fun CardComponentState.updateFieldFocus(id: CardFormElementId, hasFocus: Boolean): CardComponentState =
        updateTextInput(id) { field -> field.applyFocusChange(focusRequest, id, hasFocus) }

    private fun highlightValidationErrors(state: CardComponentState): CardComponentState {
        val highlighted = CardFormElementId.entries.fold(state) { current, id ->
            current.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }

        return highlighted.copy(focusRequest = state.form.requestFocusOnFirstInvalid())
    }
}

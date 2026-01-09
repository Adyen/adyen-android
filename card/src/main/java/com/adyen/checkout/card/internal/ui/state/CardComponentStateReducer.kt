/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

internal class CardComponentStateReducer(
    private val componentParams: CardComponentParams,
) : ComponentStateReducer<CardComponentState, CardIntent> {

    @Suppress("CyclomaticComplexMethod")
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

            is CardIntent.UpdateDetectedCardTypes -> {
                val cardType = if (state.selectedCardBrand != null) {
                    intent.detectedCardTypes.firstOrNull { it.cardBrand.txVariant == state.selectedCardBrand.txVariant }
                } else {
                    intent.detectedCardTypes.firstOrNull { it.isReliable && it.isSupported }
                }

                state.copy(
                    detectedCardTypes = intent.detectedCardTypes,
                    securityCode = state.securityCode.copy(
                        requirementPolicy = getSecurityCodeRequirementPolicy(cardType)
                    ),
                    expiryDate = state.expiryDate.copy(
                        requirementPolicy = getExpiryDateRequirementPolicy(cardType)
                    ),
                )
            }

            is CardIntent.UpdateLoading -> state.copy(
                isLoading = intent.isLoading,
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

        val hasCardNumberError = state.cardNumber.errorMessage != null
        val hasExpiryDateError = state.expiryDate.errorMessage != null
        val hasSecurityCodeError = state.securityCode.errorMessage != null
        val hasHolderNameError = state.holderName.errorMessage != null

        return state.copy(
            cardNumber = state.cardNumber.copy(
                showError = hasCardNumberError,
                isFocused = shouldFocus(hasCardNumberError),
            ),
            expiryDate = state.expiryDate.copy(
                showError = hasExpiryDateError,
                isFocused = shouldFocus(hasExpiryDateError),
            ),
            securityCode = state.securityCode.copy(
                showError = hasSecurityCodeError,
                isFocused = shouldFocus(hasSecurityCodeError),
            ),
            holderName = state.holderName.copy(
                showError = hasHolderNameError,
                isFocused = shouldFocus(hasHolderNameError),
            ),
        )
    }

    private fun getExpiryDateRequirementPolicy(cardType: DetectedCardType?): RequirementPolicy {
        return cardType?.let {
            when (it.expiryDatePolicy) {
                Brand.FieldPolicy.REQUIRED -> RequirementPolicy.Required
                Brand.FieldPolicy.OPTIONAL -> RequirementPolicy.Optional
                Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
            }
        } ?: RequirementPolicy.Required
    }

    private fun getSecurityCodeRequirementPolicy(cardType: DetectedCardType?): RequirementPolicy {
        return cardType?.let {
            when (componentParams.cvcVisibility) {
                CVCVisibility.ALWAYS_SHOW,
                CVCVisibility.HIDE_FIRST -> {
                    when (it.cvcPolicy) {
                        Brand.FieldPolicy.REQUIRED -> RequirementPolicy.Required
                        Brand.FieldPolicy.OPTIONAL -> RequirementPolicy.Optional
                        Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
                    }
                }

                CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
            }
        } ?: when (componentParams.cvcVisibility) {
            CVCVisibility.ALWAYS_SHOW -> RequirementPolicy.Required
            CVCVisibility.HIDE_FIRST, CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
        }
    }
}

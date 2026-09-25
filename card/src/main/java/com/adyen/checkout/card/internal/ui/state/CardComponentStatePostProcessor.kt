/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.internal.properties.ExpiryDateProperties.EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS
import com.adyen.checkout.core.components.internal.ui.state.ComponentStatePostProcessor
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalidTextInput
import com.adyen.checkout.core.components.internal.ui.state.model.updateErrorVisibility

internal class CardComponentStatePostProcessor : ComponentStatePostProcessor<CardComponentState, CardIntent> {

    override fun processInitialState(state: CardComponentState) = state.focusFirstInvalid(showErrorIfPresent = false)

    override fun process(
        previousState: CardComponentState,
        currentState: CardComponentState,
        intent: CardIntent,
    ) = when (intent) {
        is CardIntent.UpdateCardNumber,
        is CardIntent.UpdateDetectedCardTypes -> autoAdvanceAfterCardNumberCompletion(
            previousState = previousState,
            currentState = currentState,
        )

        is CardIntent.UpdateExpiryDate -> autoAdvanceAfterExpiryDateCompletion(
            previousState = previousState,
            currentState = currentState,
        )

        is CardIntent.UpdateFieldFocus -> currentState.updateErrorVisibility(intent.id, intent.hasFocus)
        is CardIntent.FocusRequestConsumed -> currentState.clearFocusRequest(intent.id)
        is CardIntent.UpdateCardScanResult -> currentState.focusFirstInvalid(showErrorIfPresent = false)

        is CardIntent.HighlightValidationErrors ->
            currentState.showAllErrors().focusFirstInvalid(showErrorIfPresent = true)

        else -> currentState
    }

    private fun autoAdvanceAfterCardNumberCompletion(
        previousState: CardComponentState,
        currentState: CardComponentState
    ) =
        if (!previousState.isCardNumberComplete() && currentState.isCardNumberComplete()) {
            currentState.focusFirstInvalid(showErrorIfPresent = false, afterElementId = CardFormElementId.CARD_NUMBER)
        } else {
            currentState
        }

    private fun autoAdvanceAfterExpiryDateCompletion(
        previousState: CardComponentState,
        currentState: CardComponentState
    ) =
        if (!previousState.isExpiryDateComplete() && currentState.isExpiryDateComplete()) {
            currentState.focusFirstInvalid(showErrorIfPresent = false, afterElementId = CardFormElementId.EXPIRY_DATE)
        } else {
            currentState
        }

    private fun CardComponentState.focusFirstInvalid(
        showErrorIfPresent: Boolean,
        afterElementId: CardFormElementId? = null,
    ) = copy(
        focusRequest = form.requestFocusOnFirstInvalidTextInput(
            showErrorIfPresent = showErrorIfPresent,
            afterElementId = afterElementId,
        ),
    )

    private fun CardComponentState.clearFocusRequest(id: CardFormElementId) =
        if (focusRequest?.id == id) copy(focusRequest = null) else this

    private fun CardComponentState.updateErrorVisibility(id: CardFormElementId, hasFocus: Boolean) =
        updateTextInput(id) { field -> field.updateErrorVisibility(focusRequest, id, hasFocus) }

    private fun CardComponentState.showAllErrors() =
        CardFormElementId.entries.fold(this) { state, id ->
            state.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }
}

private fun CardComponentState.isCardNumberComplete(): Boolean {
    val panLength = cardBrandState.detectedPanLength() ?: return false
    return cardNumber.text.length == panLength && form.isElementVisibleAndValid(CardFormElementId.CARD_NUMBER)
}

private fun CardComponentState.isExpiryDateComplete() =
    expiryDate.text.length == EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS &&
        form.isElementVisibleAndValid(CardFormElementId.EXPIRY_DATE)

private fun CardBrandState.detectedPanLength(): Int? = when (this) {
    is CardBrandState.SingleReliableBrand -> cardBrandData.panLength
    is CardBrandState.SingleReliableWithHiddenBrand -> cardBrandData.panLength
    is CardBrandState.DualBrand -> cardBrandDataList.firstOrNull()?.panLength
    is CardBrandState.DualBrandWithShopperSelection -> shopperSelectedCardBrandData.panLength
    else -> null
}

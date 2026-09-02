/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class StoredCardViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<StoredCardComponentState, StoredCardViewState> {

    override fun produce(state: StoredCardComponentState) = StoredCardViewState(
        // The form decides which fields are shown and in which order, so building one element per member of that order
        // is the only place either question is answered.
        elements = state.form.elements.map { state.toElement(it.id) },
        isLoading = state.isLoading,
        payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
    )

    private fun StoredCardComponentState.toElement(id: StoredCardFormElementId): StoredCardFormElement = when (id) {
        StoredCardFormElementId.SECURITY_CODE -> {
            val cardNumberFormat = detectedCardType?.cardBrand.toCardNumberFormat()
            StoredCardFormElement.SecurityCode(
                textInputViewState = securityCode
                    .toViewState(form, focusRequest, id, getSecurityCodeTrailingIcon(securityCode, cardNumberFormat)),
                cardNumberFormat = cardNumberFormat,
            )
        }
    }
}

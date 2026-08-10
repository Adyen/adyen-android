/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class StoredCardViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<StoredCardComponentState, StoredCardViewState> {

    override fun produce(state: StoredCardComponentState): StoredCardViewState {
        val cardNumberFormat = state.detectedCardType?.cardBrand.toCardNumberFormat()

        return StoredCardViewState(
            securityCode = state.securityCode.toViewState(
                customTrailingIcon = getSecurityCodeTrailingIcon(state.securityCode, cardNumberFormat),
            ),
            brand = state.detectedCardType?.cardBrand,
            cardNumberFormat = cardNumberFormat,
            isLoading = state.isLoading,
            payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
        )
    }

    private fun getSecurityCodeTrailingIcon(
        securityCode: TextInputComponentState,
        cardNumberFormat: CardNumberFormat,
    ): SecurityCodeTrailingIcon {
        return when {
            securityCode.isValid && securityCode.text.isNotEmpty() -> SecurityCodeTrailingIcon.Checkmark
            cardNumberFormat == CardNumberFormat.AMEX -> SecurityCodeTrailingIcon.PlaceholderAmex
            else -> SecurityCodeTrailingIcon.PlaceholderDefault
        }
    }
}

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
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class StoredCardViewStateProducer(
    private val amount: Amount?,
) : ViewStateProducer<StoredCardComponentState, StoredCardViewState> {

    override fun produce(state: StoredCardComponentState): StoredCardViewState {
        val cardNumberFormat = state.detectedCardType?.cardBrand.toCardNumberFormat()

        return StoredCardViewState(
            securityCode = state.securityCode.toViewState(
                trailingIcon = getSecurityCodeTrailingIcon(state.securityCode, cardNumberFormat),
            ),
            brand = state.detectedCardType?.cardBrand,
            cardNumberFormat = cardNumberFormat,
            isLoading = state.isLoading,
            amount = amount,
        )
    }

    private fun getSecurityCodeTrailingIcon(
        securityCode: TextInputComponentState,
        cardNumberFormat: CardNumberFormat,
    ): SecurityCodeTrailingIcon {
        val isValid = securityCode.errorMessage == null && securityCode.text.isNotEmpty()
        val isInvalid = securityCode.errorMessage != null && securityCode.showError

        return when {
            isValid -> SecurityCodeTrailingIcon.Checkmark
            isInvalid -> SecurityCodeTrailingIcon.Warning
            cardNumberFormat == CardNumberFormat.AMEX -> SecurityCodeTrailingIcon.PlaceholderAmex
            else -> SecurityCodeTrailingIcon.PlaceholderDefault
        }
    }
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class StoredCardViewStateProducer : ViewStateProducer<StoredCardComponentState, StoredCardViewState> {

    override fun produce(state: StoredCardComponentState): StoredCardViewState {
        val cardNumberFormat = getCardNumberFormat(state)

        return StoredCardViewState(
            securityCode = state.securityCode.toViewState(
                trailingIcon = getSecurityCodeTrailingIcon(state.securityCode, cardNumberFormat),
            ),
            brand = state.detectedCardType?.cardBrand,
            cardNumberFormat = cardNumberFormat,
            isLoading = state.isLoading,
        )
    }

    private fun getCardNumberFormat(state: StoredCardComponentState): CardNumberFormat {
        return if (state.detectedCardType?.cardBrand?.txVariant == CardType.AMERICAN_EXPRESS.txVariant) {
            CardNumberFormat.AMEX
        } else {
            CardNumberFormat.DEFAULT
        }
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

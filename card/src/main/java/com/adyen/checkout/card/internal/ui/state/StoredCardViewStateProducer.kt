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
import com.adyen.checkout.core.components.internal.ui.state.model.toViewStateIfVisible

internal class StoredCardViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<StoredCardComponentState, StoredCardViewState> {

    override fun produce(state: StoredCardComponentState): StoredCardViewState {
        val cardNumberFormat = state.detectedCardType?.cardBrand.toCardNumberFormat()

        return StoredCardViewState(
            // TODO - POC: replace with an element list, like card
            securityCode = state.securityCode.toViewStateIfVisible(
                customTrailingIcon = getSecurityCodeTrailingIcon(state.securityCode, cardNumberFormat),
            ),
            brand = state.detectedCardType?.cardBrand,
            cardNumberFormat = cardNumberFormat,
            isLoading = state.isLoading,
            payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
        )
    }
}

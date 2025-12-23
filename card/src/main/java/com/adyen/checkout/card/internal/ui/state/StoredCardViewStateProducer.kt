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
        val isAmex = state.detectedCardType?.cardBrand?.txVariant == CardType.AMERICAN_EXPRESS.txVariant

        return StoredCardViewState(
            securityCode = state.securityCode.toViewState(
                trailingIcon = getSecurityCodeTrailingIcon(state.securityCode, isAmex),
            ),
            brand = state.detectedCardType?.cardBrand,
            isLoading = state.isLoading,
        )
    }

    private fun getSecurityCodeTrailingIcon(
        securityCode: TextInputComponentState,
        isAmex: Boolean,
    ): SecurityCodeTrailingIcon {
        val isValid = securityCode.errorMessage == null && securityCode.text.isNotEmpty()
        val isInvalid = securityCode.errorMessage != null && securityCode.showError

        return when {
            isValid -> SecurityCodeTrailingIcon.Checkmark
            isInvalid -> SecurityCodeTrailingIcon.Warning
            isAmex -> SecurityCodeTrailingIcon.PlaceholderAmex
            else -> SecurityCodeTrailingIcon.PlaceholderDefault
        }
    }
}

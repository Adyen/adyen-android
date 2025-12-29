/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.helper.requirementPolicy
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class CardComponentStateFactory(
    private val componentParams: CardComponentParams,
) : ComponentStateFactory<CardComponentState> {

    override fun createInitialState(): CardComponentState {
        return CardComponentState(
            cardNumber = TextInputComponentState(isFocused = true),
            expiryDate = TextInputComponentState(),
            securityCode = TextInputComponentState(
                requirementPolicy = componentParams.cvcVisibility.requirementPolicy(),
            ),
            holderName = TextInputComponentState(),
            isHolderNameRequired = componentParams.isHolderNameRequired,
            storePaymentMethod = false,
            isStorePaymentFieldVisible = componentParams.isStorePaymentFieldVisible,
            supportedCardBrands = componentParams.supportedCardBrands,
            isLoading = false,
            detectedCardTypes = emptyList(),
            selectedCardBrand = null,
        )
    }
}

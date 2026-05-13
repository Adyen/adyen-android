/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class CardComponentStateFactory(
    private val componentParams: CardComponentParams,
) : ComponentStateFactory<CardComponentState> {

    override fun createInitialState(): CardComponentState {
        return CardComponentState(
            cardNumber = TextInputComponentState(isFocused = true),
            expiryDate = TextInputComponentState(),
            securityCode = TextInputComponentState(
                requirementPolicy = when (componentParams.cvcVisibility) {
                    CVCVisibility.ALWAYS_SHOW -> RequirementPolicy.Required
                    CVCVisibility.HIDE_FIRST -> RequirementPolicy.Hidden
                    CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
                },
            ),
            holderName = TextInputComponentState(
                requirementPolicy = when (componentParams.showCardholderName) {
                    true -> RequirementPolicy.Required
                    false -> RequirementPolicy.Hidden
                },
            ),
            socialSecurityNumber = TextInputComponentState(
                requirementPolicy = when (componentParams.socialSecurityNumberVisibility) {
                    FieldVisibility.SHOW -> RequirementPolicy.Required
                    FieldVisibility.HIDE -> RequirementPolicy.Hidden
                },
            ),
            kcpBirthDateOrTaxNumber = TextInputComponentState(
                requirementPolicy = when (componentParams.koreanAuthenticationVisibility) {
                    FieldVisibility.SHOW -> RequirementPolicy.Required
                    FieldVisibility.HIDE -> RequirementPolicy.Hidden
                },
            ),
            kcpCardPassword = TextInputComponentState(
                requirementPolicy = when (componentParams.koreanAuthenticationVisibility) {
                    FieldVisibility.SHOW -> RequirementPolicy.Required
                    FieldVisibility.HIDE -> RequirementPolicy.Hidden
                },
            ),
            postalCode = TextInputComponentState(
                requirementPolicy = when (componentParams.showPostalCode) {
                    true -> RequirementPolicy.Required
                    false -> RequirementPolicy.Hidden
                }
            ),
            storePaymentMethod = false,
            isStorePaymentFieldVisible = componentParams.showStorePayment,
            supportedCardBrands = componentParams.supportedCardBrands,
            isLoading = false,
            cardBrandState = CardBrandState.NoBrandsDetected,
        )
    }
}

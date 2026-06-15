/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class StoredCardComponentStateFactory(
    private val storedPaymentMethod: StoredCardPaymentMethod,
    private val componentParams: CardComponentParams,
) : ComponentStateFactory<StoredCardComponentState> {
    override fun createInitialState(): StoredCardComponentState {
        val cardType = CardBrand(txVariant = storedPaymentMethod.brand)

        val storedDetectedCardType = DetectedCardType(
            cardBrand = cardType,
            enableLuhnCheck = true,
            cvcPolicy = when {
                componentParams.storedCVCVisibility == StoredCVCVisibility.HIDE ||
                    NO_CVC_BRANDS.contains(cardType) -> Brand.FieldPolicy.HIDDEN

                else -> Brand.FieldPolicy.REQUIRED
            },
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = true,
            isHidden = false,
            isShopperSelectionAllowedInDualBranded = false,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )

        return StoredCardComponentState(
            securityCode = TextInputComponentState(
                isFocused = true,
                requirementPolicy = when (componentParams.storedCVCVisibility) {
                    StoredCVCVisibility.SHOW -> RequirementPolicy.Required
                    StoredCVCVisibility.HIDE -> RequirementPolicy.Hidden
                },
            ),
            isLoading = false,
            detectedCardType = storedDetectedCardType,
        )
    }

    companion object {
        private val NO_CVC_BRANDS: Set<CardBrand> = setOf(CardBrand(txVariant = CardType.BCMC.txVariant))
    }
}

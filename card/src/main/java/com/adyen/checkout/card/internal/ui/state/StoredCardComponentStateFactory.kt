/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.LocalCardBrandMapper
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class StoredCardComponentStateFactory(
    private val storedPaymentMethod: StoredCardPaymentMethod,
    private val componentParams: CardComponentParams,
) : ComponentStateFactory<StoredCardComponentState> {
    override fun createInitialState(): StoredCardComponentState {
        val storedDetectedCardType = LocalCardBrandMapper.map(
            cardBrand = CardBrand(txVariant = storedPaymentMethod.brand),
            isSupported = true,
            hideCvc = componentParams.storedCVCVisibility == StoredCVCVisibility.HIDE,
        )

        val securityCode = TextInputComponentState(
            requirementPolicy = when (componentParams.storedCVCVisibility) {
                StoredCVCVisibility.SHOW -> RequirementPolicy.Required
                StoredCVCVisibility.HIDE -> RequirementPolicy.Hidden
            },
        )

        return StoredCardComponentState(
            securityCode = securityCode,
            isLoading = false,
            detectedCardType = storedDetectedCardType,
            // Opens the keyboard on the security code as soon as the screen appears. A stored card that asks for none
            // has nothing to focus, and a request for a field that is not on screen would never be reported back.
            focusRequest = FocusRequest(id = StoredCardFieldId.SECURITY_CODE).takeIf { securityCode.isVisible },
        )
    }
}

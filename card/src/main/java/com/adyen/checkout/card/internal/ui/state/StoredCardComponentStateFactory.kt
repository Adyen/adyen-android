/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class StoredCardComponentStateFactory(
    private val componentParams: CardComponentParams,
) : ComponentStateFactory<StoredCardComponentState> {
    override fun createInitialState(): StoredCardComponentState {
        return StoredCardComponentState(
            securityCode = TextInputComponentState(
                isFocused = true,
                requirementPolicy = when (componentParams.storedCVCVisibility) {
                    StoredCVCVisibility.SHOW -> RequirementPolicy.Required
                    StoredCVCVisibility.HIDE -> RequirementPolicy.Hidden
                },
            ),
            isLoading = false,
            detectedCardType = null,
        )
    }
}

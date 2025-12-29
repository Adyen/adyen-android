/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class StoredCardComponentStateValidator(
    private val cardValidationMapper: CardValidationMapper,
) : ComponentStateValidator<StoredCardComponentState> {

    override fun validate(state: StoredCardComponentState): StoredCardComponentState {
        val securityCodeError =
            validateSecurityCode(state.securityCode, state.detectedCardType, InputFieldUIState.REQUIRED)

        return state.copy(
            securityCode = state.securityCode.copy(errorMessage = securityCodeError),
        )
    }

    override fun isValid(state: StoredCardComponentState): Boolean {
        return state.securityCode.errorMessage == null
    }

    private fun validateSecurityCode(
        securityCode: TextInputComponentState,
        selectedOrFirstCardType: DetectedCardType?,
        uiState: InputFieldUIState,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapSecurityCodeValidation(
            validation = CardValidationUtils.validateSecurityCode(
                securityCode = securityCode.text,
                detectedCardType = selectedOrFirstCardType,
                uiState = uiState,
            ),
        )
    }
}

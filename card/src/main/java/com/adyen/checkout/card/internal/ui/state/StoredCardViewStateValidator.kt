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
import com.adyen.checkout.core.components.internal.ui.state.ViewStateValidator
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class StoredCardViewStateValidator(
    private val cardValidationMapper: CardValidationMapper,
) : ViewStateValidator<StoredCardViewState, StoredCardComponentState> {

    override fun validate(
        viewState: StoredCardViewState,
        componentState: StoredCardComponentState,
    ): StoredCardViewState {
        val securityCode = viewState.securityCode
        val securityCodeError =
            validateSecurityCode(securityCode, componentState.detectedCardType, InputFieldUIState.REQUIRED)

        return viewState.copy(
            securityCode = securityCode.copy(errorMessage = securityCodeError),
            brand = componentState.detectedCardType?.cardBrand,
        )
    }

    override fun isValid(viewState: StoredCardViewState): Boolean {
        return viewState.securityCode.errorMessage == null
    }

    override fun highlightAllValidationErrors(viewState: StoredCardViewState): StoredCardViewState {
        val hasSecurityCodeError = viewState.securityCode.errorMessage != null

        return viewState.copy(
            securityCode = viewState.securityCode.copy(
                showError = hasSecurityCodeError,
                isFocused = hasSecurityCodeError,
            ),
        )
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

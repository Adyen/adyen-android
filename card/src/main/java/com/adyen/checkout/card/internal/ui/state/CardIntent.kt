/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface CardIntent : ComponentStateIntent {

    // User input intents
    data class UpdateCardNumber(val number: String) : CardIntent

    data class UpdateCardNumberFocus(val hasFocus: Boolean) : CardIntent

    data class UpdateExpiryDate(val expiryDate: String) : CardIntent

    data class UpdateExpiryDateFocus(val hasFocus: Boolean) : CardIntent

    data class UpdateSecurityCode(val securityCode: String) : CardIntent

    data class UpdateSecurityCodeFocus(val hasFocus: Boolean) : CardIntent

    data class UpdateHolderName(val holderName: String) : CardIntent

    data class UpdateHolderNameFocus(val hasFocus: Boolean) : CardIntent

    data class UpdateStorePaymentMethod(val isChecked: Boolean) : CardIntent

    data class SelectBrand(val cardBrand: CardBrand) : CardIntent

    // System intents
    data class UpdateDetectedCardTypes(val detectedCardTypes: List<DetectedCardType>) : CardIntent

    data class UpdateLoading(val isLoading: Boolean) : CardIntent

    data object HighlightValidationErrors : CardIntent
}

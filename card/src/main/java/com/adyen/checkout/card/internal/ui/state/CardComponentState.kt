/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class CardComponentState(
    // UI input fields
    val cardNumber: TextInputComponentState,
    val expiryDate: TextInputComponentState,
    val securityCode: TextInputComponentState,
    val holderName: TextInputComponentState,

    // UI configuration
    val isHolderNameRequired: Boolean,
    val storePaymentMethod: Boolean,
    val isStorePaymentFieldVisible: Boolean,
    val supportedCardBrands: List<CardBrand>,
    val isLoading: Boolean,

    // Component state
    val detectedCardTypes: List<DetectedCardType>,
    val selectedCardBrand: CardBrand?,
) : ComponentState

internal val CardComponentState.binValue: String
    get() = if (cardNumber.isValid && cardNumber.text.length >= EXTENDED_CARD_NUMBER_LENGTH) {
        cardNumber.text.take(BIN_VALUE_EXTENDED_LENGTH)
    } else {
        cardNumber.text.take(BIN_VALUE_LENGTH)
    }

@VisibleForTesting
internal const val BIN_VALUE_LENGTH = 6

@VisibleForTesting
internal const val BIN_VALUE_EXTENDED_LENGTH = 8
private const val EXTENDED_CARD_NUMBER_LENGTH = 16

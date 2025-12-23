/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface StoredCardIntent : ComponentStateIntent {

    // User input intents
    data class UpdateSecurityCode(val securityCode: String) : StoredCardIntent

    data class UpdateSecurityCodeFocus(val hasFocus: Boolean) : StoredCardIntent

    // System intents
    data class UpdateDetectedCardType(val detectedCardType: DetectedCardType) : StoredCardIntent

    data class UpdateLoading(val isLoading: Boolean) : StoredCardIntent

    data object HighlightValidationErrors : StoredCardIntent
}

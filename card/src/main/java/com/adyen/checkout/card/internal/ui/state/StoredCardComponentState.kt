/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class StoredCardComponentState(
    val securityCode: TextInputComponentState,
    val isLoading: Boolean,
    val detectedCardType: DetectedCardType?,
) : ComponentState

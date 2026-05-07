/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/5/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType

internal data class NetworkBinLookupState(
    val detectedCardTypes: List<DetectedCardType>,
    val issuingCountryCode: String?,
)

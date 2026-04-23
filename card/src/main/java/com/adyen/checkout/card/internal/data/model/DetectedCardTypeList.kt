/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/4/2026.
 */

package com.adyen.checkout.card.internal.data.model

internal data class DetectedCardTypeList(
    val detectedCardTypes: List<DetectedCardType>,
    val source: Source,
    /**
     * This BIN is only used internally for card brand detection and is different from the actual BIN which is returned
     * in the public API.
     */
    val cardDetectionBin: String?,
) {
    enum class Source {
        LOCAL,
        NETWORK,
    }
}

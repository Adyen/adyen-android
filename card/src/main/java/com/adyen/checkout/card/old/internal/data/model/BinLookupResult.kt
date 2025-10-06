/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.data.model

/**
 * Result of Bin Lookup cache query.
 */
internal sealed class BinLookupResult {
    /**
     * Bin Lookup Result not available in cache.
     */
    object Unavailable : BinLookupResult()

    /**
     * Bin Lookup Result is being fetched from the API.
     */
    object Loading : BinLookupResult()

    /**
     * Bin Lookup Result is available in cache.
     */
    data class Available(val detectedCardTypes: List<DetectedCardType>) : BinLookupResult()
}

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.model

/**
 * Result of Bin Lookup cache query.
 */
internal sealed class BinLookupCacheResult {
    /**
     * Bin Lookup Result not available in cache.
     */
    object Unavailable : BinLookupCacheResult()

    /**
     * Bin Lookup Result is being fetched from the API.
     */
    object Fetching : BinLookupCacheResult()

    /**
     * Bin Lookup Result is available in cache.
     */
    data class Available(val detectedCardTypes: List<DetectedCardType>) : BinLookupCacheResult()
}

/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/4/2026.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.BinLookupCacheResult
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.internal.helper.Sha256
import java.util.concurrent.ConcurrentHashMap

internal class BinLookupCache {

    private val cachedBinLookupResults = ConcurrentHashMap<String, BinLookupCacheResult>()

    fun getResult(bin: String): BinLookupCacheResult {
        val key = hashBin(bin)
        return cachedBinLookupResults[key] ?: BinLookupCacheResult.Unavailable
    }

    fun setFetching(bin: String) {
        val key = hashBin(bin)
        cachedBinLookupResults[key] = BinLookupCacheResult.Fetching
    }

    fun setCachedResults(bin: String, detectedCardTypes: List<DetectedCardType>) {
        val key = hashBin(bin)
        cachedBinLookupResults[key] = BinLookupCacheResult.Available(detectedCardTypes)
    }

    fun remove(bin: String) {
        val key = hashBin(bin)
        cachedBinLookupResults.remove(key)
    }

    private fun hashBin(bin: String): String {
        return Sha256.hashString(bin)
    }
}

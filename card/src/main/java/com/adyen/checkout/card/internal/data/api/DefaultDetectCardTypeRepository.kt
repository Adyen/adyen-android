/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.api

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.card.internal.data.model.BinLookupCacheResult
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class DefaultDetectCardTypeRepository(
    private val binLookupCache: BinLookupCache,
    private val localCardBrandDetectionService: LocalCardBrandDetectionService,
    private val networkCardBrandDetectionService: NetworkCardBrandDetectionService,
) : DetectCardTypeRepository {

    override fun detectCardTypes(cardNumber: String): Flow<DetectedCardTypeList> = flow {
        // if we remove this@DefaultDetectCardTypeRepository the tag gets resolved as SafeCollector since we're inside
        // a flow block
        // TODO fix logger to resolve the correct tag
        this@DefaultDetectCardTypeRepository.adyenLog(AdyenLogLevel.VERBOSE) { "detectCardTypes" }
        val bin = getBin(cardNumber)

        val cachedResult = if (bin != null) {
            binLookupCache.getResult(bin)
        } else {
            BinLookupCacheResult.Unavailable
        }

        when (cachedResult) {
            is BinLookupCacheResult.Available -> {
                // found card types in cache, no need to fetch from network or local
                emit(DetectedCardTypeList(cachedResult.detectedCardTypes, DetectedCardTypeList.Source.NETWORK))
                this@DefaultDetectCardTypeRepository.adyenLog(AdyenLogLevel.DEBUG) {
                    "card types returned from cache: ${cachedResult.detectedCardTypes.toLogString()}"
                }
            }

            is BinLookupCacheResult.Fetching -> {
                // do nothing, a previous call/flow will emit the results when finished
                this@DefaultDetectCardTypeRepository.adyenLog(AdyenLogLevel.DEBUG) {
                    "request already in progress, will not emit results"
                }
            }

            is BinLookupCacheResult.Unavailable -> {
                // return local card types first
                val localDetectedCardTypes = localCardBrandDetectionService.getCardBrands(cardNumber)
                emit(DetectedCardTypeList(localDetectedCardTypes, DetectedCardTypeList.Source.LOCAL))
                this@DefaultDetectCardTypeRepository.adyenLog(AdyenLogLevel.DEBUG) {
                    "card types detected locally: ${localDetectedCardTypes.toLogString()}"
                }

                if (bin != null) {
                    // fetch from network and cache results
                    val networkDetectedCardTypes = detectCardTypesFromNetwork(bin)
                    if (networkDetectedCardTypes != null) {
                        emit(DetectedCardTypeList(networkDetectedCardTypes, DetectedCardTypeList.Source.NETWORK))
                        this@DefaultDetectCardTypeRepository.adyenLog(AdyenLogLevel.DEBUG) {
                            "card types fetched from network: ${networkDetectedCardTypes.toLogString()}"
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the BIN if the card number is equal or longer than the BIN length
     */
    @VisibleForTesting
    internal fun getBin(cardNumber: String): String? {
        return cardNumber.takeIf { it.length >= BIN_LENGTH }?.take(BIN_LENGTH)
    }

    private suspend fun detectCardTypesFromNetwork(bin: String): List<DetectedCardType>? {
        adyenLog(AdyenLogLevel.VERBOSE) { "detectCardTypesFromNetwork" }

        // ensure we don't call bin lookup again while another call for the same bin is in progress
        binLookupCache.setFetching(bin)

        val detectionResult = networkCardBrandDetectionService.getCardBrands(bin)
        return detectionResult.fold(
            onSuccess = { detectedCardTypes ->
                binLookupCache.setCachedResults(bin, detectedCardTypes)
                detectedCardTypes
            },
            onFailure = {
                binLookupCache.remove(bin)
                null
            },
        )
    }

    /**
     * example logs:
     *   [amex, diners] - Unsupported: [cartebancaire, dankort]
     *   [amex, diners]
     *   Unsupported: [cartebancaire, dankort]
     *   none
     */
    private fun List<DetectedCardType>.toLogString(): String {
        val supportedTypes = filter { it.isSupported }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "[", postfix = "]") { it.cardBrand.txVariant }

        val unsupportedTypes = filter { !it.isSupported }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "Unsupported: [", postfix = "]") { it.cardBrand.txVariant }

        return listOfNotNull(supportedTypes, unsupportedTypes)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" - ")
            ?: "none"
    }

    companion object {
        private const val BIN_LENGTH = 11
    }
}

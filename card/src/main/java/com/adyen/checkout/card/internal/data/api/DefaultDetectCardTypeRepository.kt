/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.BinLookupCacheResult
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class DefaultDetectCardTypeRepository(
    private val binLookupCache: BinLookupCache,
    private val localCardBrandDetectionService: LocalCardBrandDetectionService,
    private val networkCardBrandDetectionService: NetworkCardBrandDetectionService,
) : DetectCardTypeRepository {

    private val _detectedCardTypesFlow: Channel<List<DetectedCardType>> = bufferedChannel()
    override val detectedCardTypesFlow: Flow<List<DetectedCardType>> = _detectedCardTypesFlow.receiveAsFlow()

    @Suppress("LongParameterList")
    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        coroutineScope: CoroutineScope,
        paymentMethodType: String?
    ) {
        adyenLog(AdyenLogLevel.DEBUG) { "detectCardType" }
        if (shouldFetchReliableTypes(cardNumber)) {
            when (val cachedResult = getFromCache(cardNumber)) {
                is BinLookupCacheResult.Available -> {
                    adyenLog(AdyenLogLevel.DEBUG) { "Retrieving from cache." }
                    _detectedCardTypesFlow.trySend(cachedResult.detectedCardTypes)
                    return
                }

                is BinLookupCacheResult.Fetching -> {
                    adyenLog(AdyenLogLevel.DEBUG) { "BinLookup request is in progress." }
                }

                is BinLookupCacheResult.Unavailable -> {
                    adyenLog(AdyenLogLevel.DEBUG) { "Fetching from network." }
                    fetchFromNetwork(
                        cardNumber,
                        publicKey,
                        supportedCardBrands,
                        clientKey,
                        coroutineScope,
                        paymentMethodType,
                    )
                }
            }
        }
        val locallyDetectedCardBrands = localCardBrandDetectionService.getCardBrands(cardNumber, supportedCardBrands)
        _detectedCardTypesFlow.trySend(locallyDetectedCardBrands)
    }

    @Suppress("LongParameterList")
    private fun fetchFromNetwork(
        cardNumber: String,
        publicKey: String?,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        coroutineScope: CoroutineScope,
        paymentMethodType: String?
    ) {
        if (publicKey != null) {
            adyenLog(AdyenLogLevel.DEBUG) { "Launching Bin Lookup" }

            coroutineScope.launch {
                adyenLog(AdyenLogLevel.DEBUG) { "Emitting new detectedCardTypes" }
                fetch(
                    cardNumber,
                    publicKey,
                    supportedCardBrands,
                    clientKey,
                    paymentMethodType,
                )?.let {
                    _detectedCardTypesFlow.send(it)
                }
            }
        }
    }

    private fun shouldFetchReliableTypes(cardNumber: String): Boolean {
        return cardNumber.length >= REQUIRED_BIN_SIZE
    }

    private fun getFromCache(cardNumber: String): BinLookupCacheResult {
        val bin = getBin(cardNumber)
        return binLookupCache.getResult(bin)
    }

    private suspend fun fetch(
        cardNumber: String,
        publicKey: String,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        paymentMethodType: String?
    ): List<DetectedCardType>? {
        val bin = getBin(cardNumber)
        binLookupCache.setFetching(bin)

        val detectedCardTypes = networkCardBrandDetectionService.getCardBrands(
            cardNumber,
            publicKey,
            supportedCardBrands,
            clientKey,
            paymentMethodType,
        )
        if (detectedCardTypes == null) {
            binLookupCache.remove(bin)
            return null
        } else {
            binLookupCache.setCachedResults(bin, detectedCardTypes)
            return detectedCardTypes
        }
    }

    private fun getBin(cardNumber: String): String {
        return cardNumber.take(REQUIRED_BIN_SIZE)
    }

    companion object {
        private const val REQUIRED_BIN_SIZE = 11
    }
}

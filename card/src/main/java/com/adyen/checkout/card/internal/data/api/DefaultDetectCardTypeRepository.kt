/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.BinLookupCacheResult
import com.adyen.checkout.card.internal.data.model.BinLookupRequest
import com.adyen.checkout.card.internal.data.model.BinLookupResponse
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

internal class DefaultDetectCardTypeRepository(
    private val cardEncryptor: BaseCardEncryptor,
    private val binLookupService: BinLookupService,
    private val binLookupCache: BinLookupCache,
    private val localCardBrandDetectionService: LocalCardBrandDetectionService,
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
        type: String?
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
                        type,
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
        type: String?
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
                    type,
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
        type: String?
    ): List<DetectedCardType>? {
        val bin = getBin(cardNumber)
        binLookupCache.setFetching(bin)

        val binLookupResponse = makeBinLookup(cardNumber, publicKey, supportedCardBrands, clientKey, type)
        return if (binLookupResponse == null) {
            binLookupCache.remove(bin)
            null
        } else {
            val detectedCardTypes = mapResponse(binLookupResponse)
            binLookupCache.setCachedResults(bin, detectedCardTypes)
            detectedCardTypes
        }
    }

    private fun getBin(cardNumber: String): String {
        return cardNumber.take(REQUIRED_BIN_SIZE)
    }

    private suspend fun makeBinLookup(
        cardNumber: String,
        publicKey: String,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        type: String?
    ): BinLookupResponse? {
        return runSuspendCatching {
            val encryptedBin = cardEncryptor.encryptBin(cardNumber, publicKey)
            val cardBrands = supportedCardBrands.map { it.txVariant }
            val request = BinLookupRequest(encryptedBin, UUID.randomUUID().toString(), cardBrands, type)

            binLookupService.makeBinLookup(
                request = request,
                clientKey = clientKey,
            )
        }
            .onFailure { e -> adyenLog(AdyenLogLevel.ERROR, e) { "checkCardType - Failed to do bin lookup" } }
            .getOrNull()
    }

    private fun mapResponse(binLookupResponse: BinLookupResponse): List<DetectedCardType> {
        adyenLog(AdyenLogLevel.DEBUG) { "handleBinLookupResponse" }
        adyenLog(AdyenLogLevel.VERBOSE) { "Brands: ${binLookupResponse.brands}" }

        // Any null or unmapped values are ignored, a null response becomes an empty list
        return binLookupResponse.brands.orEmpty().mapNotNull { brandResponse ->
            if (brandResponse.brand == null) return@mapNotNull null
            val cardBrand = CardBrand(txVariant = brandResponse.brand)
            DetectedCardType(
                cardBrand = cardBrand,
                isReliable = true,
                enableLuhnCheck = brandResponse.enableLuhnCheck == true,
                cvcPolicy = Brand.FieldPolicy.parse(
                    brandResponse.cvcPolicy ?: Brand.FieldPolicy.REQUIRED.value,
                ),
                expiryDatePolicy = Brand.FieldPolicy.parse(
                    brandResponse.expiryDatePolicy ?: Brand.FieldPolicy.REQUIRED.value,
                ),
                isSupported = brandResponse.supported != false,
                panLength = brandResponse.panLength,
                paymentMethodVariant = brandResponse.paymentMethodVariant,
                localizedBrand = brandResponse.localizedBrand,
            )
        }
    }

    companion object {
        private const val REQUIRED_BIN_SIZE = 11
    }
}

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/8/2022.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.api.BinLookupService
import com.adyen.checkout.card.api.model.BinLookupRequest
import com.adyen.checkout.card.api.model.BinLookupResponse
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.core.encryption.Sha256
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching
import com.adyen.checkout.cse.CardEncrypter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

internal class DefaultDetectCardTypeRepository(
    private val cardEncrypter: CardEncrypter,
    private val binLookupService: BinLookupService,
) : DetectCardTypeRepository {

    private val _detectedCardTypesFlow: Channel<List<DetectedCardType>> = bufferedChannel()
    override val detectedCardTypesFlow: Flow<List<DetectedCardType>> = _detectedCardTypesFlow.receiveAsFlow()

    private val cachedBinLookup = HashMap<String, BinLookupResult>()

    @Suppress("LongParameterList")
    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardTypes: List<CardType>,
        clientKey: String,
        coroutineScope: CoroutineScope,
    ) {
        Logger.d(TAG, "detectCardType")
        if (shouldFetchReliableTypes(cardNumber)) {
            when (val cachedResult = getFromCache(cardNumber)) {
                is BinLookupResult.Available -> {
                    Logger.d(TAG, "Retrieving from cache.")
                    _detectedCardTypesFlow.trySend(cachedResult.detectedCardTypes)
                    return
                }
                is BinLookupResult.Loading -> {
                    Logger.d(TAG, "BinLookup request is in progress.")
                }
                is BinLookupResult.Unavailable -> {
                    Logger.d(TAG, "Fetching from network.")
                    fetchFromNetwork(
                        cardNumber,
                        publicKey,
                        supportedCardTypes,
                        clientKey,
                        coroutineScope
                    )
                }
            }
        }
        _detectedCardTypesFlow.trySend(detectCardLocally(cardNumber, supportedCardTypes))
    }

    @Suppress("LongParameterList")
    private fun fetchFromNetwork(
        cardNumber: String,
        publicKey: String?,
        supportedCardTypes: List<CardType>,
        clientKey: String,
        coroutineScope: CoroutineScope,
    ) {
        if (publicKey != null) {
            Logger.d(TAG, "Launching Bin Lookup")

            coroutineScope.launch {
                Logger.d(TAG, "Emitting new detectedCardTypes")
                fetch(
                    cardNumber,
                    publicKey,
                    supportedCardTypes,
                    clientKey
                )?.let {
                    _detectedCardTypesFlow.send(it)
                }
            }
        }
    }

    private fun detectCardLocally(cardNumber: String, supportedCardTypes: List<CardType>): List<DetectedCardType> {
        Logger.d(TAG, "detectCardLocally")
        if (cardNumber.isEmpty()) {
            return emptyList()
        }
        val estimateCardTypes = CardType.estimate(cardNumber)
        return estimateCardTypes.map { localDetectedCard(it, supportedCardTypes) }
    }

    private fun localDetectedCard(cardType: CardType, supportedCardTypes: List<CardType>): DetectedCardType {
        return DetectedCardType(
            cardType,
            isReliable = false,
            enableLuhnCheck = true,
            cvcPolicy = when {
                NO_CVC_BRANDS.contains(cardType) -> Brand.FieldPolicy.HIDDEN
                else -> Brand.FieldPolicy.REQUIRED
            },
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = supportedCardTypes.contains(cardType),
            panLength = null
        )
    }

    private fun shouldFetchReliableTypes(cardNumber: String): Boolean {
        return cardNumber.length >= REQUIRED_BIN_SIZE
    }

    private fun getFromCache(cardNumber: String): BinLookupResult {
        return cachedBinLookup[hashBin(cardNumber)] ?: BinLookupResult.Unavailable
    }

    private fun hashBin(cardNumber: String): String {
        return Sha256.hashString(cardNumber.take(REQUIRED_BIN_SIZE))
    }

    private suspend fun fetch(
        cardNumber: String,
        publicKey: String,
        supportedCardTypes: List<CardType>,
        clientKey: String,
    ): List<DetectedCardType>? {
        val key = hashBin(cardNumber)
        cachedBinLookup[key] = BinLookupResult.Loading
        val binLookupResponse = makeBinLookup(cardNumber, publicKey, supportedCardTypes, clientKey)

        return if (binLookupResponse == null) {
            cachedBinLookup.remove(key)
            null
        } else {
            val detectedCardTypes = mapResponse(binLookupResponse)
            cachedBinLookup[key] = BinLookupResult.Available(detectedCardTypes)
            detectedCardTypes
        }
    }

    private suspend fun makeBinLookup(
        cardNumber: String,
        publicKey: String,
        supportedCardTypes: List<CardType>,
        clientKey: String,
    ): BinLookupResponse? {
        return runSuspendCatching {
            val encryptedBin = cardEncrypter.encryptBin(cardNumber, publicKey)
            val cardTypes = supportedCardTypes.map { it.txVariant }
            val request = BinLookupRequest(encryptedBin, UUID.randomUUID().toString(), cardTypes)

            binLookupService.makeBinLookup(
                request = request,
                clientKey = clientKey
            )
        }
            .onFailure { e -> Logger.e(TAG, "checkCardType - Failed to do bin lookup", e) }
            .getOrNull()
    }

    private fun mapResponse(binLookupResponse: BinLookupResponse): List<DetectedCardType> {
        Logger.d(TAG, "handleBinLookupResponse")
        Logger.v(TAG, "Brands: ${binLookupResponse.brands}")

        // Any null or unmapped values are ignored, a null response becomes an empty list
        return binLookupResponse.brands.orEmpty().mapNotNull { brandResponse ->
            if (brandResponse.brand == null) return@mapNotNull null
            val cardType = CardType(txVariant = brandResponse.brand)
            DetectedCardType(
                cardType = cardType,
                isReliable = true,
                enableLuhnCheck = brandResponse.enableLuhnCheck == true,
                cvcPolicy = Brand.FieldPolicy.parse(brandResponse.cvcPolicy ?: Brand.FieldPolicy.REQUIRED.value),
                expiryDatePolicy = Brand.FieldPolicy.parse(
                    brandResponse.expiryDatePolicy ?: Brand.FieldPolicy.REQUIRED.value
                ),
                isSupported = brandResponse.supported != false,
                panLength = brandResponse.panLength
            )
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private val NO_CVC_BRANDS: Set<CardType> = hashSetOf(CardType(cardBrand = CardBrand.BCMC))

        private const val REQUIRED_BIN_SIZE = 11
    }
}

/**
 * Result of Bin Lookup cache query.
 */
private sealed class BinLookupResult {
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

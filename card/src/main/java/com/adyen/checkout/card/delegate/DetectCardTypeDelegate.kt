/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/8/2022.
 */

package com.adyen.checkout.card.delegate

import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.repository.BinLookupRepository
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DetectCardTypeDelegate(
    private val binLookupRepository: BinLookupRepository,
) {

    /**
     *
     */
    private fun shouldFetchFromRepository(cardNumber: String): Boolean {
        return cardNumber.length >= BinLookupRepository.REQUIRED_BIN_SIZE
    }

    private val _detectedCardTypesFlow: MutableStateFlow<List<DetectedCardType>> = MutableStateFlow(emptyList())
    internal val detectedCardTypesFlow: Flow<List<DetectedCardType>> = _detectedCardTypesFlow

    @Suppress("LongParameterList")
    fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardTypes: List<CardType>,
        environment: Environment,
        clientKey: String,
        coroutineScope: CoroutineScope,
    ) {
        Logger.d(TAG, "detectCardType")
        if (shouldFetchFromRepository(cardNumber)) {
            when (val cachedResult = binLookupRepository.getFromCache(cardNumber)) {
                is BinLookupRepository.BinLookupResult.Available -> {
                    Logger.d(TAG, "Retrieving from cache.")
                    _detectedCardTypesFlow.tryEmit(cachedResult.detectedCardTypes)
                }
                is BinLookupRepository.BinLookupResult.Loading -> {
                    Logger.d(TAG, "BinLookup request is in progress.")
                }
                is BinLookupRepository.BinLookupResult.Unavailable -> {
                    Logger.d(TAG, "Fetching from network.")
                    fetchFromNetwork(
                        cardNumber,
                        publicKey,
                        supportedCardTypes,
                        environment,
                        clientKey,
                        coroutineScope
                    )
                }
            }
        }
        _detectedCardTypesFlow.tryEmit(detectCardLocally(cardNumber, supportedCardTypes))
    }

    @Suppress("LongParameterList")
    private fun fetchFromNetwork(
        cardNumber: String,
        publicKey: String?,
        supportedCardTypes: List<CardType>,
        environment: Environment,
        clientKey: String,
        coroutineScope: CoroutineScope,
    ) {
        if (publicKey != null) {
            Logger.d(TAG, "Launching Bin Lookup")

            coroutineScope.launch {
                Logger.d(TAG, "Emitting new detectedCardTypes")
                binLookupRepository.fetch(
                    cardNumber,
                    publicKey,
                    supportedCardTypes,
                    environment,
                    clientKey
                )?.let {
                    _detectedCardTypesFlow.emit(it)
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
            isSupported = supportedCardTypes.contains(cardType)
        )
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private val NO_CVC_BRANDS: Set<CardType> = hashSetOf(CardType.BCMC)
    }
}

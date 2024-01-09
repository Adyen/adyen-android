/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.card.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.internal.data.model.BinLookupRequest
import com.adyen.checkout.card.internal.data.model.BinLookupResponse
import com.adyen.checkout.card.internal.data.model.BinLookupResult
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.Sha256
import com.adyen.checkout.core.internal.util.runSuspendCatching
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultDetectCardTypeRepository(
    private val cardEncryptor: BaseCardEncryptor,
    private val binLookupService: BinLookupService,
) : DetectCardTypeRepository {

    private val _detectedCardTypesFlow: Channel<List<DetectedCardType>> = bufferedChannel()
    override val detectedCardTypesFlow: Flow<List<DetectedCardType>> = _detectedCardTypesFlow.receiveAsFlow()

    private val cachedBinLookup = HashMap<String, BinLookupResult>()

    @Suppress("LongParameterList")
    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        coroutineScope: CoroutineScope,
        type: String?
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
                        supportedCardBrands,
                        clientKey,
                        coroutineScope,
                        type
                    )
                }
            }
        }
        _detectedCardTypesFlow.trySend(detectCardLocally(cardNumber, supportedCardBrands))
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
            Logger.d(TAG, "Launching Bin Lookup")

            coroutineScope.launch {
                Logger.d(TAG, "Emitting new detectedCardTypes")
                fetch(
                    cardNumber,
                    publicKey,
                    supportedCardBrands,
                    clientKey,
                    type
                )?.let {
                    _detectedCardTypesFlow.send(it)
                }
            }
        }
    }

    private fun detectCardLocally(cardNumber: String, supportedCardBrands: List<CardBrand>): List<DetectedCardType> {
        Logger.d(TAG, "detectCardLocally")
        if (cardNumber.isEmpty()) {
            return emptyList()
        }
        val estimateCardTypes = CardBrand.estimate(cardNumber)
        return estimateCardTypes.map { localDetectedCard(it, supportedCardBrands) }
    }

    private fun localDetectedCard(cardBrand: CardBrand, supportedCardBrands: List<CardBrand>): DetectedCardType {
        return DetectedCardType(
            cardBrand = cardBrand,
            isReliable = false,
            enableLuhnCheck = true,
            cvcPolicy = when {
                NO_CVC_BRANDS.contains(cardBrand) -> Brand.FieldPolicy.HIDDEN
                else -> Brand.FieldPolicy.REQUIRED
            },
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = supportedCardBrands.contains(cardBrand),
            panLength = null,
            paymentMethodVariant = null,
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
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        type: String?
    ): List<DetectedCardType>? {
        val key = hashBin(cardNumber)
        cachedBinLookup[key] = BinLookupResult.Loading
        val binLookupResponse = makeBinLookup(cardNumber, publicKey, supportedCardBrands, clientKey, type)

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
            val cardBrand = CardBrand(txVariant = brandResponse.brand)
            DetectedCardType(
                cardBrand = cardBrand,
                isReliable = true,
                enableLuhnCheck = brandResponse.enableLuhnCheck == true,
                cvcPolicy = Brand.FieldPolicy.parse(
                    brandResponse.cvcPolicy ?: Brand.FieldPolicy.REQUIRED.value
                ),
                expiryDatePolicy = Brand.FieldPolicy.parse(
                    brandResponse.expiryDatePolicy ?: Brand.FieldPolicy.REQUIRED.value
                ),
                isSupported = brandResponse.supported != false,
                panLength = brandResponse.panLength,
                paymentMethodVariant = brandResponse.paymentMethodVariant,
            )
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private val NO_CVC_BRANDS: Set<CardBrand> = hashSetOf(CardBrand(cardType = CardType.BCMC))

        private const val REQUIRED_BIN_SIZE = 11
    }
}

/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 3/2/2021.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.api.BinLookupService
import com.adyen.checkout.card.api.model.BinLookupRequest
import com.adyen.checkout.card.api.model.BinLookupResponse
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.encryption.Sha256
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching
import com.adyen.checkout.cse.CardEncrypter
import java.util.UUID

private val TAG = LogUtil.getTag()

class BinLookupRepository(
    private val cardEncrypter: CardEncrypter
) {

    sealed class BinLookupResult {
        object Unavailable : BinLookupResult()
        object Loading : BinLookupResult()
        data class Available(val detectedCardTypes: List<DetectedCardType>) : BinLookupResult()
    }

    private val cachedBinLookup = HashMap<String, BinLookupResult>()

    fun getFromCache(cardNumber: String): BinLookupResult {
        return cachedBinLookup[hashBin(cardNumber)] ?: BinLookupResult.Unavailable
    }

    private fun hashBin(cardNumber: String): String {
        return Sha256.hashString(cardNumber.take(REQUIRED_BIN_SIZE))
    }

    suspend fun fetch(
        cardNumber: String,
        publicKey: String,
        supportedCardTypes: List<CardType>,
        environment: Environment,
        clientKey: String,
    ): List<DetectedCardType>? {
        val key = hashBin(cardNumber)
        cachedBinLookup[key] = BinLookupResult.Loading
        val binLookupResponse = makeBinLookup(cardNumber, publicKey, supportedCardTypes, environment, clientKey)

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
        environment: Environment,
        clientKey: String,
    ): BinLookupResponse? {
        return runSuspendCatching {
            val encryptedBin = cardEncrypter.encryptBin(cardNumber, publicKey)
            val cardTypes = supportedCardTypes.map { it.txVariant }
            val request = BinLookupRequest(encryptedBin, UUID.randomUUID().toString(), cardTypes)

            BinLookupService(environment).makeBinLookup(
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
            val cardType = CardType.getByBrandName(brandResponse.brand) ?: CardType.UNKNOWN.apply {
                txVariant = brandResponse.brand
            }
            DetectedCardType(
                cardType = cardType,
                isReliable = true,
                enableLuhnCheck = brandResponse.enableLuhnCheck == true,
                cvcPolicy = Brand.FieldPolicy.parse(brandResponse.cvcPolicy ?: Brand.FieldPolicy.REQUIRED.value),
                expiryDatePolicy = Brand.FieldPolicy.parse(
                    brandResponse.expiryDatePolicy ?: Brand.FieldPolicy.REQUIRED.value
                ),
                isSupported = brandResponse.supported != false
            )
        }
    }

    companion object {
        const val REQUIRED_BIN_SIZE = 11
    }
}

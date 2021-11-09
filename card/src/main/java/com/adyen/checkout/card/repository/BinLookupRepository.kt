/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 3/2/2021.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.api.BinLookupConnection
import com.adyen.checkout.card.api.model.BinLookupRequest
import com.adyen.checkout.card.api.model.BinLookupResponse
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.core.encryption.Sha256
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.exception.EncryptionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.IOException
import java.util.UUID

private val TAG = LogUtil.getTag()

class BinLookupRepository {

    private val cachedBinLookup = HashMap<String, List<DetectedCardType>>()

    fun isRequiredSize(cardNumber: String): Boolean {
        return cardNumber.length >= REQUIRED_BIN_SIZE
    }

    fun contains(cardNumber: String): Boolean {
        return if (isRequiredSize(cardNumber)) {
            cachedBinLookup.contains(hashBin(cardNumber))
        } else {
            false
        }
    }

    private fun hashBin(cardNumber: String): String {
        return Sha256.hashString(cardNumber.take(REQUIRED_BIN_SIZE))
    }

    fun get(cardNumber: String): List<DetectedCardType> {
        if (isRequiredSize(cardNumber)) {
            return cachedBinLookup[hashBin(cardNumber)] ?: throw IllegalArgumentException("BinLookupRepository does not contain card number")
        } else {
            throw IllegalArgumentException("Card number too small card number")
        }
    }

    suspend fun fetch(
        cardNumber: String,
        publicKey: String,
        cardConfiguration: CardConfiguration
    ): List<DetectedCardType> {
        val binLookupResponse = makeBinLookup(cardNumber, publicKey, cardConfiguration)
        val detectedCardTypes = mapResponse(binLookupResponse)
        cachedBinLookup[hashBin(cardNumber)] = detectedCardTypes
        return detectedCardTypes
    }

    private suspend fun makeBinLookup(
        cardNumber: String,
        publicKey: String,
        cardConfiguration: CardConfiguration
    ): BinLookupResponse? = coroutineScope {
        val deferredEncryption = async(Dispatchers.Default) {
            CardEncrypter.encryptBin(cardNumber, publicKey)
        }
        return@coroutineScope try {
            val encryptedBin = deferredEncryption.await()
            val cardTypes = cardConfiguration.supportedCardTypes.map { it.txVariant }
            val request = BinLookupRequest(encryptedBin, UUID.randomUUID().toString(), cardTypes)
            BinLookupConnection(request, cardConfiguration.environment, cardConfiguration.clientKey).suspendedCall()
        } catch (e: EncryptionException) {
            Logger.e(TAG, "checkCardType - Failed to encrypt BIN", e)
            null
        } catch (e: IOException) {
            Logger.e(TAG, "checkCardType - Failed to call binLookup API.", e)
            null
        }
    }

    private fun mapResponse(binLookupResponse: BinLookupResponse?): List<DetectedCardType> {
        Logger.d(TAG, "handleBinLookupResponse")
        Logger.v(TAG, "Brands: ${binLookupResponse?.brands}")

        // Any null or unmapped values are ignored, a null response becomes an empty list
        return binLookupResponse?.brands.orEmpty().mapNotNull { brandResponse ->
            if (brandResponse.brand == null) return@mapNotNull null
            val cardType = CardType.getByBrandName(brandResponse.brand) ?: CardType.UNKNOWN.apply { txVariant = brandResponse.brand }
            DetectedCardType(
                cardType,
                isReliable = true,
                enableLuhnCheck = brandResponse.enableLuhnCheck == true,
                cvcPolicy = Brand.FieldPolicy.parse(brandResponse.cvcPolicy ?: Brand.FieldPolicy.REQUIRED.value),
                expiryDatePolicy = Brand.FieldPolicy.parse(brandResponse.expiryDatePolicy ?: Brand.FieldPolicy.REQUIRED.value),
                isSupported = brandResponse.supported != false
            )
        }
    }

    companion object {
        private const val REQUIRED_BIN_SIZE = 11
    }
}

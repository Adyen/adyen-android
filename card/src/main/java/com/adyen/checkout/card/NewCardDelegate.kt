/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/11/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.api.BinLookupConnection
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.model.BinLookupRequest
import com.adyen.checkout.card.model.BinLookupResponse
import com.adyen.checkout.card.model.Brand
import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.components.validation.ValidatedField
import com.adyen.checkout.core.encryption.Sha256
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.exception.EncryptionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.IOException
import java.util.UUID

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class NewCardDelegate(
    private val paymentMethod: PaymentMethod,
    cardConfiguration: CardConfiguration
) : CardDelegate(cardConfiguration) {

    private val cachedBinLookup = HashMap<String, List<DetectedCardType>>()

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun validateCardNumber(cardNumber: String): ValidatedField<String> {
        return CardValidationUtils.validateCardNumber(cardNumber)
    }

    override fun validateExpiryDate(expiryDate: ExpiryDate): ValidatedField<ExpiryDate> {
        return CardValidationUtils.validateExpiryDate(expiryDate)
    }

    override fun validateSecurityCode(
        securityCode: String,
        cardType: CardType?
    ): ValidatedField<String> {
        return if (cardConfiguration.isHideCvc) {
            ValidatedField(securityCode, ValidatedField.Validation.VALID)
        } else {
            CardValidationUtils.validateSecurityCode(securityCode, cardType)
        }
    }

    override fun isCvcHidden(): Boolean {
        return cardConfiguration.isHideCvc
    }

    override fun requiresInput(): Boolean {
        return true
    }

    override fun isHolderNameRequired(): Boolean {
        return cardConfiguration.isHolderNameRequired
    }

    override fun detectCardType(
        cardNumber: String,
        publicKey: String,
        coroutineScope: CoroutineScope
    ): List<DetectedCardType> {
        if (cardNumber.length == BinLookupConnection.REQUIRED_BIN_SIZE) {
            // trigger BIN lookup
            // on response - trigger output data
            // cache BIN  lookup response
        }

        // Check if we have a cached value
        if (cardNumber.length >= BinLookupConnection.REQUIRED_BIN_SIZE) {
            val bin = cardNumber.substring(0, BinLookupConnection.REQUIRED_BIN_SIZE)
            val hashedBin = Sha256.hashString(bin)
            if (cachedBinLookup.containsKey(hashedBin)) {
                val cashedResult = cachedBinLookup[hashedBin]
                if (cashedResult != null) {
                    return cashedResult
                }
            }
        }

        // regexes logic from updateSupportedFilterCards()
        return detectCardLocally(cardNumber)
    }

    private fun detectCardLocally(cardNumber: String): List<DetectedCardType> {
        Logger.d(TAG, "detectCardLocally")
        if (cardNumber.isEmpty()) {
            return emptyList()
        }
        val supportedCardTypes = cardConfiguration.supportedCardTypes
        val estimateCardTypes = CardType.estimate(cardNumber)
        val detectedCardTypes = supportedCardTypes.filter { estimateCardTypes.contains(it) }
        return detectedCardTypes.map { localDetectedCard(it) }
    }

    override fun localDetectedCard(cardType: CardType): DetectedCardType {
        return DetectedCardType(
            cardType,
            isReliable = false,
            showExpiryDate = true,
            enableLuhnCheck = true,
            cvcPolicy = getCvcPolicy(cardType.txVariant)
        )
    }

    override fun getCvcPolicy(brand: String): Brand.CvcPolicy {
        return when {
            cardConfiguration.isHideCvc || noCvcBrands.contains(brand) -> Brand.CvcPolicy.HIDDEN
            else -> Brand.CvcPolicy.REQUIRED
        }
    }

    private suspend fun makeBinLookup(cardNumber: String, publicKey: String) {
        coroutineScope {
            val deferredEncryption = async(Dispatchers.Default) {
                CardEncrypter.encryptBin(cardNumber, publicKey)
            }
            try {
                val encryptedBin = deferredEncryption.await()
                val cardTypes = cardConfiguration.supportedCardTypes.map { it.txVariant }
                val request = BinLookupRequest(encryptedBin, UUID.randomUUID().toString(), cardTypes)
                val response = BinLookupConnection(request, cardConfiguration.environment, cardConfiguration.clientKey).suspendedCall()
                // TODO: 28/01/2021 send response to flow/callback/livedata
            } catch (e: EncryptionException) {
                Logger.e(TAG, "checkCardType - Failed to encrypt BIN", e)
            } catch (e: IOException) {
                Logger.e(TAG, "checkCardType - Failed to call binLookup API.", e)
            }
        }
    }

    private fun cardTypeReceived(binLookupResponse: BinLookupResponse) {
        Logger.d(TAG, "cardBrandReceived")
        val brands = binLookupResponse.brands
        when {
            brands.isNullOrEmpty() -> {
                Logger.d(TAG, "Card brand not found.")
                // TODO: 19/01/2021 Keep regexes prediction and don't apply business rules
            }
            brands.size > 1 -> {
                Logger.d(TAG, "Multiple brands found.")
                // TODO: 19/01/2021 use first brand
            }
            else -> {
                Logger.d(TAG, "Card brand: ${brands.first().brand}")
                val cardType = CardType.getByBrandName(brands.first().brand.orEmpty())
                Logger.d(TAG, "CardType: ${cardType?.name}")
                // TODO: 19/01/2021 trigger brand specific business logic
            }
        }
    }
}

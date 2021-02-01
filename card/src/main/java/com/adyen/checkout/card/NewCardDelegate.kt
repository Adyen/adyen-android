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
import kotlinx.coroutines.launch
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

        if (cardNumber.length >= BinLookupConnection.REQUIRED_BIN_SIZE) {
            val bin = cardNumber.substring(0, BinLookupConnection.REQUIRED_BIN_SIZE)
            val hashedBin = Sha256.hashString(bin)

            // if length is exactly the size, we call bin lookup API
            if (cardNumber.length == BinLookupConnection.REQUIRED_BIN_SIZE) {
                coroutineScope.launch {
                    val binLookupResponse = makeBinLookup(cardNumber, publicKey)
                    if (binLookupResponse != null) {
                        handleBinLookupResponse(hashedBin, binLookupResponse)
                    }
                }
            }

            // Check if we have a cached value
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

    private suspend fun makeBinLookup(cardNumber: String, publicKey: String): BinLookupResponse? = coroutineScope {
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

    private fun handleBinLookupResponse(hashedBin: String, binLookupResponse: BinLookupResponse) {
        Logger.d(TAG, "handleBinLookupResponse")

        // map result to DetectedCardType
        val detectedCardTypes = binLookupResponse.brands.orEmpty().mapNotNull {
            if (it.brand == null) return@mapNotNull null
            val cardType = CardType.getByBrandName(it.brand) ?: return@mapNotNull null
            DetectedCardType(
                cardType,
                isReliable = true,
                showExpiryDate = it.showExpiryDate == true,
                enableLuhnCheck = it.enableLuhnCheck == true,
                cvcPolicy = Brand.CvcPolicy.parse(it.cvcPolicy ?: Brand.CvcPolicy.REQUIRED.value)
            )
        }

        // cache result
        cachedBinLookup[hashedBin] = detectedCardTypes

        // TODO: 01/02/2021 trigger FLow to the CardComponent to update State
    }
}

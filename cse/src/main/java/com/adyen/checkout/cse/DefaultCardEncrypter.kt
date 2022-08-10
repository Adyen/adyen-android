/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */
package com.adyen.checkout.cse

import androidx.annotation.WorkerThread
import com.adyen.checkout.cse.CardEncrypter.Companion.GENERATION_TIME_KEY
import com.adyen.checkout.cse.exception.EncryptionException
import org.json.JSONException
import org.json.JSONObject
import java.util.Date

class DefaultCardEncrypter(
    private val genericEncrypter: GenericEncrypter
) : CardEncrypter {

    /**
     * Encrypts the available card data from [UnencryptedCard] into individual encrypted blocks.
     *
     * @param unencryptedCard The card data to be encrypted.
     * @param publicKey     The key to be used for encryption.
     * @return An [EncryptedCard] object with each encrypted field.
     * @throws EncryptionException in case the encryption fails.
     */
    @WorkerThread
    @Throws(EncryptionException::class)
    override fun encryptFields(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): EncryptedCard {
        return try {
            val encryptedExpiryMonth: String?
            val encryptedExpiryYear: String?
            val encryptedNumber: String? = if (unencryptedCard.number != null) {
                genericEncrypter.encryptField(
                    CARD_NUMBER_KEY,
                    unencryptedCard.number,
                    publicKey
                )
            } else {
                null
            }
            if (unencryptedCard.expiryMonth != null && unencryptedCard.expiryYear != null) {
                encryptedExpiryMonth = genericEncrypter.encryptField(
                    EXPIRY_MONTH_KEY,
                    unencryptedCard.expiryMonth,
                    publicKey
                )
                encryptedExpiryYear = genericEncrypter.encryptField(
                    EXPIRY_YEAR_KEY,
                    unencryptedCard.expiryYear,
                    publicKey
                )
            } else if (unencryptedCard.expiryMonth == null && unencryptedCard.expiryYear == null) {
                encryptedExpiryMonth = null
                encryptedExpiryYear = null
            } else {
                throw EncryptionException("Both expiryMonth and expiryYear need to be set for encryption.", null)
            }
            val encryptedSecurityCode: String? = if (unencryptedCard.cvc != null) {
                genericEncrypter.encryptField(CVC_KEY, unencryptedCard.cvc, publicKey)
            } else {
                null
            }
            EncryptedCard(encryptedNumber, encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode)
        } catch (e: IllegalStateException) {
            throw EncryptionException(e.message ?: "No message.", e)
        }
    }

    /**
     * Encrypts all the card data present in [UnencryptedCard] into a single block of content.
     *
     * @param unencryptedCard The card data to be encrypted.
     * @param publicKey     The key to be used for encryption.
     * @return The encrypted card data String.
     * @throws EncryptionException in case the encryption fails.
     */
    @WorkerThread
    @Throws(EncryptionException::class)
    override fun encrypt(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): String {
        val cardJson = JSONObject()
        return try {
            cardJson.put(CARD_NUMBER_KEY, unencryptedCard.number)
            cardJson.put(EXPIRY_MONTH_KEY, unencryptedCard.expiryMonth)
            cardJson.put(EXPIRY_YEAR_KEY, unencryptedCard.expiryYear)
            cardJson.put(CVC_KEY, unencryptedCard.cvc)
            cardJson.put(HOLDER_NAME_KEY, unencryptedCard.cardHolderName)
            val formattedGenerationTime = genericEncrypter.makeGenerationTime(unencryptedCard.generationTime)
            cardJson.put(GENERATION_TIME_KEY, formattedGenerationTime)
            val encrypter = ClientSideEncrypter(publicKey)
            encrypter.encrypt(cardJson.toString())
        } catch (e: JSONException) {
            throw EncryptionException("Failed to created encrypted JSON data.", e)
        }
    }

    /**
     * Encrypts the BIN of the card to be used in the Bin Lookup endpoint.
     *
     * @param bin The BIN value to be encrypted.
     * @param publicKey The key to be used for encryption.
     * @return The encrypted bin String.
     * @throws EncryptionException in case the encryption fails.
     */
    @WorkerThread
    @Throws(EncryptionException::class)
    override fun encryptBin(bin: String, publicKey: String): String {
        return try {
            val binJson = JSONObject()
            binJson.put(BIN_KEY, bin)
            binJson.put(GENERATION_TIME_KEY, genericEncrypter.makeGenerationTime(Date()))
            val encrypter = ClientSideEncrypter(publicKey)
            encrypter.encrypt(binJson.toString())
        } catch (e: JSONException) {
            throw EncryptionException("Failed to created encrypted JSON data.", e)
        }
    }

    companion object {
        private const val CARD_NUMBER_KEY = "number"
        private const val EXPIRY_MONTH_KEY = "expiryMonth"
        private const val EXPIRY_YEAR_KEY = "expiryYear"
        private const val CVC_KEY = "cvc"
        private const val HOLDER_NAME_KEY = "holderName"
        private const val BIN_KEY = "binValue"
    }
}

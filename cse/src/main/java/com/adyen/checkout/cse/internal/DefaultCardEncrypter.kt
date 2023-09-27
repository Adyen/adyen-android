/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */
package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard

internal class DefaultCardEncrypter(
    private val genericEncrypter: BaseGenericEncrypter
) : BaseCardEncrypter {

    override fun encryptFields(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): EncryptedCard {
        return try {
            val encryptedNumber = unencryptedCard.number?.let { number ->
                genericEncrypter.encryptField(
                    fieldKeyToEncrypt = CARD_NUMBER_KEY,
                    fieldValueToEncrypt = number,
                    publicKey = publicKey
                )
            }

            val encryptedExpiryMonth: String?
            val encryptedExpiryYear: String?
            when {
                unencryptedCard.expiryMonth != null && unencryptedCard.expiryYear != null -> {
                    encryptedExpiryMonth = genericEncrypter.encryptField(
                        fieldKeyToEncrypt = EXPIRY_MONTH_KEY,
                        fieldValueToEncrypt = unencryptedCard.expiryMonth,
                        publicKey = publicKey
                    )
                    encryptedExpiryYear = genericEncrypter.encryptField(
                        fieldKeyToEncrypt = EXPIRY_YEAR_KEY,
                        fieldValueToEncrypt = unencryptedCard.expiryYear,
                        publicKey = publicKey
                    )
                }

                unencryptedCard.expiryMonth == null && unencryptedCard.expiryYear == null -> {
                    encryptedExpiryMonth = null
                    encryptedExpiryYear = null
                }

                else -> {
                    throw EncryptionException("Both expiryMonth and expiryYear need to be set for encryption.", null)
                }
            }

            val encryptedSecurityCode = unencryptedCard.cvc?.let { cvc ->
                genericEncrypter.encryptField(CVC_KEY, cvc, publicKey)
            }
            EncryptedCard(
                encryptedCardNumber = encryptedNumber,
                encryptedExpiryMonth = encryptedExpiryMonth,
                encryptedExpiryYear = encryptedExpiryYear,
                encryptedSecurityCode = encryptedSecurityCode
            )
        } catch (e: IllegalStateException) {
            throw EncryptionException(e.message ?: "No message.", e)
        }
    }

    override fun encrypt(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): String {
        return genericEncrypter.encryptFields(
            publicKey,
            CARD_NUMBER_KEY to unencryptedCard.number,
            EXPIRY_MONTH_KEY to unencryptedCard.expiryMonth,
            EXPIRY_YEAR_KEY to unencryptedCard.expiryYear,
            CVC_KEY to unencryptedCard.cvc,
            HOLDER_NAME_KEY to unencryptedCard.cardHolderName,
        )
    }

    override fun encryptBin(bin: String, publicKey: String): String {
        return genericEncrypter.encryptField(
            BIN_KEY,
            bin,
            publicKey
        )
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

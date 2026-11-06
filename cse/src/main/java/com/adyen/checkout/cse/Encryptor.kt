/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2026.
 */

package com.adyen.checkout.cse

import com.adyen.checkout.cse.internal.CheckoutEncryptorFactory

/**
 * Encrypts data to be sent to Adyen's APIs.
 */
object Encryptor {

    private val checkoutEncryptor = CheckoutEncryptorFactory.provide()

    /**
     * Encrypts the fields set in [card] individually.
     *
     * The values are encrypted as they are. At least one field must be set. Fields that are not set are not encrypted
     * and remain `null` in the result.
     *
     * @param card The card data to be encrypted.
     * @param publicKey The key to be used for encryption.
     * @return An [EncryptedCard] with the encrypted values of the fields set in [card], or a failure with [Error].
     */
    fun encrypt(card: Card, publicKey: String): Result<EncryptedCard> {
        return checkoutEncryptor.encrypt(card, publicKey)
    }

    /**
     * A single value that can be encrypted.
     */
    abstract class Field internal constructor(internal val key: String) {

        /**
         * The card number.
         */
        object CardNumber : Field("number")

        /**
         * The card expiry month.
         */
        object ExpiryMonth : Field("expiryMonth")

        /**
         * The card expiry year.
         */
        object ExpiryYear : Field("expiryYear")

        /**
         * The card security code.
         */
        object SecurityCode : Field("cvc")

        /**
         * The BIN of the card.
         */
        object Bin : Field("binValue")

        /**
         * The KCP card password.
         */
        object KcpPassword : Field("password")

        /**
         * The bank account number.
         */
        object BankAccountNumber : Field("bankAccountNumber")

        /**
         * The bank routing number.
         */
        object BankRoutingNumber : Field("bankLocationId")
    }

    /**
     * Error returned when the encryption fails.
     */
    class Error internal constructor(
        val code: String,
        message: String? = null,
        cause: Throwable? = null,
    ) : RuntimeException(message, cause) {
        object Code {
            const val INVALID_CARD = "InvalidCard"
            const val INVALID_PUBLIC_KEY = "InvalidPublicKey"
            const val ENCRYPTION_FAILED = "EncryptionFailed"
        }
    }
}

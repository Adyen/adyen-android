/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2022.
 */

package com.adyen.checkout.cse

import androidx.annotation.WorkerThread
import com.adyen.checkout.cse.exception.EncryptionException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

interface CardEncrypter {

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
    fun encryptFields(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): EncryptedCard

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
    fun encrypt(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): String

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
    fun encryptBin(bin: String, publicKey: String): String

    companion object {

        const val GENERATION_TIME_KEY = "generationtime"

        val GENERATION_DATE_FORMAT: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
    }
}

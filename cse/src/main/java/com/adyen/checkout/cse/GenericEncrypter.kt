/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/8/2022.
 */

package com.adyen.checkout.cse

import com.adyen.checkout.cse.internal.GenericEncryptorFactory

/**
 * Allows the encryption of any type of data to be sent to Adyen's APIs.
 * Use this class with custom component integrations.
 */
object GenericEncrypter {

    private val encryptor = GenericEncryptorFactory.provide()

    /**
     * Encrypts a single field into a block of content.
     *
     * @param fieldKeyToEncrypt The key of the field to be encrypted.
     * @param fieldValueToEncrypt The value of the field to be encrypted.
     * @param publicKey The key to be used for encryption.
     * @return The encrypted string.
     * @throws EncryptionException in case the encryption fails.
     */
    @Throws(EncryptionException::class)
    fun encryptField(
        fieldKeyToEncrypt: String,
        fieldValueToEncrypt: Any?,
        publicKey: String,
    ): String {
        return encryptor.encryptField(
            fieldKeyToEncrypt = fieldKeyToEncrypt,
            fieldValueToEncrypt = fieldValueToEncrypt,
            publicKey = publicKey,
        )
    }

    /**
     * Encrypts multiple fields into a single block of content.
     *
     * @param publicKey The key to be used for encryption.
     * @param fieldsToEncrypt The fields to be encrypted.
     * @return The encrypted string.
     * @throws EncryptionException in case the encryption fails.
     */
    @Throws(EncryptionException::class)
    fun encryptFields(
        publicKey: String,
        vararg fieldsToEncrypt: Pair<String, Any?>,
    ): String {
        return encryptor.encryptFields(
            fieldsToEncrypt = fieldsToEncrypt,
            publicKey = publicKey,
        )
    }
}

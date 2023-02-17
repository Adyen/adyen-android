/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/8/2022.
 */

package com.adyen.checkout.cse

import com.adyen.checkout.cse.exception.EncryptionException

interface GenericEncrypter {

    /**
     * Encrypts a single field into a block of content.
     *
     * @param fieldKeyToEncrypt The key of the field to be encrypted.
     * @param fieldValueToEncrypt The value of the field to be encrypted.
     * @param publicKey The key to be used for encryption.
     * @return The encrypted string.
     * @throws EncryptionException in case the encryption fails.
     */
    fun encryptField(
        fieldKeyToEncrypt: String,
        fieldValueToEncrypt: Any?,
        publicKey: String,
    ): String

    /**
     * Encrypts multiple fields into a single block of content.
     *
     * @param publicKey The key to be used for encryption.
     * @param fieldsToEncrypt The fields to be encrypted.
     * @return The encrypted string.
     * @throws EncryptionException in case the encryption fails.
     */
    fun encryptFields(
        publicKey: String,
        vararg fieldsToEncrypt: Pair<String, Any?>,
    ): String
}

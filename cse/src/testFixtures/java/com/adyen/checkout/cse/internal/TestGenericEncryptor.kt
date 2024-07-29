/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/7/2024.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.EncryptionException

class TestGenericEncryptor : BaseGenericEncryptor {

    var shouldThrowException = false

    override fun encryptField(fieldKeyToEncrypt: String, fieldValueToEncrypt: Any?, publicKey: String): String {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)
        return fieldValueToEncrypt.toString()
    }

    override fun encryptFields(publicKey: String, vararg fieldsToEncrypt: Pair<String, Any?>): String {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)
        return fieldsToEncrypt.firstOrNull().toString()
    }
}

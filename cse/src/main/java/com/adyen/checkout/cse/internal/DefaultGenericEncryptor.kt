/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/8/2022.
 */

package com.adyen.checkout.cse.internal

internal class DefaultGenericEncryptor(
    private val dateGenerator: DateGenerator,
    private val jweEncryptor: JSONWebEncryptor,
) : BaseGenericEncryptor {

    override fun encryptField(fieldKeyToEncrypt: String, fieldValueToEncrypt: Any?, publicKey: String): String {
        return encryptFields(
            publicKey,
            fieldKeyToEncrypt to fieldValueToEncrypt,
        )
    }

    override fun encryptFields(publicKey: String, vararg fieldsToEncrypt: Pair<String, Any?>): String {
        val plainText = EncryptionPlainTextGenerator.generate(dateGenerator.getCurrentDate(), mapOf(*fieldsToEncrypt))
        return jweEncryptor.encrypt(publicKey, plainText)
    }
}

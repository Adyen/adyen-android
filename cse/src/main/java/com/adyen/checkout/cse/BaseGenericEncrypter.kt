/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/8/2022.
 */

package com.adyen.checkout.cse

interface BaseGenericEncrypter {

    fun encryptField(
        fieldKeyToEncrypt: String,
        fieldValueToEncrypt: Any?,
        publicKey: String,
    ): String

    fun encryptFields(
        publicKey: String,
        vararg fieldsToEncrypt: Pair<String, Any?>,
    ): String
}

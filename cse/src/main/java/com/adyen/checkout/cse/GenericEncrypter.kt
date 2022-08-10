/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/8/2022.
 */

package com.adyen.checkout.cse

import java.util.Date

interface GenericEncrypter {

    fun encryptField(
        encryptionKey: String,
        fieldToEncrypt: Any,
        publicKey: String
    ): String

    fun makeGenerationTime(generationTime: Date? = null): String

    companion object {
        const val KCP_PASSWORD_KEY = "password"
    }
}

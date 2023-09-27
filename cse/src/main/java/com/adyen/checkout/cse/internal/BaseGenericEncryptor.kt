/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/2/2023.
 */

package com.adyen.checkout.cse.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface BaseGenericEncryptor {

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

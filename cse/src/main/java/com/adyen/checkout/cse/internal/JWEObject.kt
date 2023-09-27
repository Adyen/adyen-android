/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/9/2023.
 */

package com.adyen.checkout.cse.internal

import android.util.Base64

internal data class JWEObject(
    val header: Base64String,
    val encryptedKey: Base64String,
    val iv: Base64String,
    val cipherText: Base64String,
    val authTag: Base64String,
)

internal class Base64String(bytes: ByteArray) {
    val value: String = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)

    override fun toString(): String = value
}

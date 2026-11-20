/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/9/2023.
 */

package com.adyen.checkout.cse.internal

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal data class JWEObject(
    val header: Base64String,
    val encryptedKey: Base64String,
    val initializationVector: Base64String,
    val cipherText: Base64String,
    val authTag: Base64String,
)

@OptIn(ExperimentalEncodingApi::class)
internal class Base64String(bytes: ByteArray) {
    val value: String = Base64.UrlSafe.encode(bytes)

    override fun toString(): String = value
}

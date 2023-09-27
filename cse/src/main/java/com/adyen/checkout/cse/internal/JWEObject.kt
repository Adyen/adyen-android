/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/9/2023.
 */

package com.adyen.checkout.cse.internal

internal data class JWEObject(
    val header: String,
    val encryptedKey: String,
    val iv: String,
    val cipherText: String,
    val authTag: String,
)

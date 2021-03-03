/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 29/1/2021.
 */

package com.adyen.checkout.core.encryption

import java.security.MessageDigest

object Sha256 {

    private const val SHA_256 = "SHA-256"
    private val digest = MessageDigest.getInstance(SHA_256)

    @Suppress("MemberVisibilityCanBePrivate")
    fun hash(byteArray: ByteArray): ByteArray {
        digest.reset()
        digest.update(byteArray)
        return digest.digest()
    }

    fun hashString(string: String): String {
        return String(hash(string.toByteArray(Charsets.UTF_8)), Charsets.UTF_8)
    }
}

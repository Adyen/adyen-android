/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/9/2023.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.EncryptionException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal class CompositeKey(
    inputKey: SecretKey
) {

    val macKey: SecretKey
    val encKey: SecretKey
    val truncatedMacLength: Int

    init {
        val keyBytes = inputKey.encoded
        @Suppress("MagicNumber")
        when (keyBytes.size) {
            32 -> {
                macKey = SecretKeySpec(keyBytes, 0, 16, "HMACSHA256")
                encKey = SecretKeySpec(keyBytes, 16, 16, "AES")
                truncatedMacLength = 16
            }

            48 -> {
                macKey = SecretKeySpec(keyBytes, 0, 24, "HMACSHA384")
                encKey = SecretKeySpec(keyBytes, 24, 24, "AES")
                truncatedMacLength = 24
            }

            64 -> {
                macKey = SecretKeySpec(keyBytes, 0, 32, "HMACSHA512")
                encKey = SecretKeySpec(keyBytes, 32, 32, "AES")
                truncatedMacLength = 32
            }

            else -> {
                throw EncryptionException("Unsupported key length, must be 256, 384 or 512 bits", null)
            }
        }
    }
}

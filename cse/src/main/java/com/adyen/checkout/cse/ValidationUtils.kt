/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2021.
 */
package com.adyen.checkout.cse

import java.util.regex.Pattern

object ValidationUtils {

    private const val PUBLIC_KEY_PATTERN = "([A-F]|[0-9]){5}\\|([A-F]|[0-9]){512}"
    private const val PUBLIC_KEY_SIZE = 5 + 1 + 512

    /**
     * Checks if the public key for encryption is valid.
     *
     * @param publicKey The public key string
     * @return True if valid, False if not.
     */
    @JvmStatic
    fun isPublicKeyValid(publicKey: String): Boolean {
        val pubKeyPattern = Pattern.compile(PUBLIC_KEY_PATTERN)
        return pubKeyPattern.matcher(publicKey).find() && publicKey.length == PUBLIC_KEY_SIZE
    }
}

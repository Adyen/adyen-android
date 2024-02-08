/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/2/2023.
 */

package com.adyen.checkout.cse.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.UnencryptedCard

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface BaseCardEncryptor {

    fun encryptFields(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): EncryptedCard

    fun encrypt(
        unencryptedCard: UnencryptedCard,
        publicKey: String
    ): String

    fun encryptBin(bin: String, publicKey: String): String
}

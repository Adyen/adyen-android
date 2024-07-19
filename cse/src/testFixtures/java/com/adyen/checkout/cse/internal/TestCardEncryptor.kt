/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/7/2024.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard

class TestCardEncryptor : BaseCardEncryptor {

    var shouldThrowException = false

    override fun encryptFields(unencryptedCard: UnencryptedCard, publicKey: String): EncryptedCard {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)

        return EncryptedCard(
            encryptedCardNumber = unencryptedCard.number,
            encryptedExpiryMonth = unencryptedCard.expiryMonth,
            encryptedExpiryYear = unencryptedCard.expiryYear,
            encryptedSecurityCode = unencryptedCard.cvc,
        )
    }

    override fun encrypt(unencryptedCard: UnencryptedCard, publicKey: String): String {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)

        return unencryptedCard.toString()
    }

    override fun encryptBin(bin: String, publicKey: String): String {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)

        return bin
    }
}

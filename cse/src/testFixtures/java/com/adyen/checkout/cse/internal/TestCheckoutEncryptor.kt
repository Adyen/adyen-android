/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2026.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.Card
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.Encryptor

/**
 * Echoes the values it encrypts.
 */
class TestCheckoutEncryptor : CheckoutEncryptor {

    var shouldFailCardEncryption = false

    override fun encrypt(card: Card, publicKey: String): Result<EncryptedCard> = result(shouldFailCardEncryption) {
        EncryptedCard(
            encryptedCardNumber = card.number,
            encryptedExpiryMonth = card.expiryMonth,
            encryptedExpiryYear = card.expiryYear,
            encryptedSecurityCode = card.securityCode,
        )
    }

    private fun <T> result(shouldFail: Boolean, value: () -> T): Result<T> {
        return if (shouldFail) {
            Result.failure(Encryptor.Error(Encryptor.Error.Code.ENCRYPTION_FAILED, "Failed for testing purposes"))
        } else {
            Result.success(value())
        }
    }
}

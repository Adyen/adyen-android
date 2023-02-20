/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2022.
 */

package com.adyen.checkout.cse.internal.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.cse.internal.BaseCardEncrypter
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.EncryptionException

/**
 * Test implementation of [BaseCardEncrypter]. This class should never be used in not test code as it does not do
 * any encryption!
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
class TestCardEncrypter : BaseCardEncrypter {

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

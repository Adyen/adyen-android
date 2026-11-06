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

internal class DefaultCheckoutEncryptor(
    private val genericEncryptor: BaseGenericEncryptor,
) : CheckoutEncryptor {

    @Suppress("ComplexCondition")
    override fun encrypt(card: Card, publicKey: String): Result<EncryptedCard> = runCatching {
        if (card.number == null && card.expiryMonth == null && card.expiryYear == null && card.securityCode == null) {
            throw Encryptor.Error(Encryptor.Error.Code.INVALID_CARD, "Cannot encrypt a card without any fields set.")
        }
        ensurePublicKeyValid(publicKey)

        EncryptedCard(
            encryptedCardNumber = card.number?.let { encryptValue(Encryptor.Field.CardNumber, it, publicKey) },
            encryptedExpiryMonth = card.expiryMonth?.let { encryptValue(Encryptor.Field.ExpiryMonth, it, publicKey) },
            encryptedExpiryYear = card.expiryYear?.let { encryptValue(Encryptor.Field.ExpiryYear, it, publicKey) },
            encryptedSecurityCode = card.securityCode?.let {
                encryptValue(Encryptor.Field.SecurityCode, it, publicKey)
            },
        )
    }

    private fun ensurePublicKeyValid(publicKey: String) {
        if (!ValidationUtils.isPublicKeyValid(publicKey)) {
            throw Encryptor.Error(
                Encryptor.Error.Code.INVALID_PUBLIC_KEY,
                "Encryption failed because the public key is invalid.",
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun encryptValue(field: Encryptor.Field, value: String, publicKey: String): String {
        return try {
            genericEncryptor.encryptField(field.key, value, publicKey)
        } catch (e: Exception) {
            throw Encryptor.Error(Encryptor.Error.Code.ENCRYPTION_FAILED, "Encryption failed.", e)
        }
    }
}

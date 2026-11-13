/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2026.
 */

package com.adyen.checkout.cse

/**
 * Raw card data to be encrypted with [Encryptor].
 *
 * Only the properties that are set are encrypted, so a partial card can be used to encrypt individual card fields.
 *
 * @param number The card number.
 * @param expiryMonth The expiry month, e.g. "3" or "03" for March.
 * @param expiryYear The expiry year, e.g. "2030".
 * @param securityCode The card security code.
 */
class Card(
    val number: String? = null,
    val expiryMonth: String? = null,
    val expiryYear: String? = null,
    val securityCode: String? = null,
)

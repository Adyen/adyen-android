/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2026.
 */

package com.adyen.checkout.cse.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.cse.Card
import com.adyen.checkout.cse.EncryptedCard

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface CheckoutEncryptor {

    fun encrypt(card: Card, publicKey: String): Result<EncryptedCard>
}

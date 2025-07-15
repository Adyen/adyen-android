/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/7/2024.
 */

package com.adyen.checkout.giftcard.internal.ui.protocol

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface GiftCardProtocol {

    fun getComponentViewType(): ComponentViewType

    fun createPaymentMethod(
        paymentMethod: PaymentMethod,
        encryptedCard: EncryptedCard,
        checkoutAttemptId: String?
    ): GiftCardPaymentMethod
}

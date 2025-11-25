/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/7/2024.
 */

package com.adyen.checkout.giftcard.internal.ui.protocol

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.giftcard.internal.ui.GiftCardComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType

internal class DefaultGiftCardProtocol : GiftCardProtocol {
    override fun getComponentViewType(): ComponentViewType {
        return GiftCardComponentViewType()
    }

    override fun createPaymentMethod(
        paymentMethod: PaymentMethod,
        encryptedCard: EncryptedCard,
        checkoutAttemptId: String?,
        sdkData: String?
    ): GiftCardPaymentMethod {
        return GiftCardPaymentMethod(
            type = paymentMethod.type,
            checkoutAttemptId = checkoutAttemptId,
            sdkData = sdkData,
            encryptedCardNumber = encryptedCard.encryptedCardNumber,
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
            encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth,
            encryptedExpiryYear = encryptedCard.encryptedExpiryYear,
            brand = paymentMethod.brand,
        )
    }
}

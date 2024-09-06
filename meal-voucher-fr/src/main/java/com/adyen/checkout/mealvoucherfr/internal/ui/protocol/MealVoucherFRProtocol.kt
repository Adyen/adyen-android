/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr.internal.ui.protocol

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.giftcard.internal.ui.protocol.GiftCardProtocol
import com.adyen.checkout.mealvoucherfr.internal.ui.MealVoucherFRComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType

internal class MealVoucherFRProtocol : GiftCardProtocol {
    override fun getComponentViewType(): ComponentViewType {
        return MealVoucherFRComponentViewType
    }

    override fun createPaymentMethod(
        paymentMethod: PaymentMethod,
        encryptedCard: EncryptedCard,
        checkoutAttemptId: String?
    ): GiftCardPaymentMethod {
        return GiftCardPaymentMethod(
            type = PaymentMethodTypes.MEAL_VOUCHER_FR,
            checkoutAttemptId = checkoutAttemptId,
            encryptedCardNumber = encryptedCard.encryptedCardNumber,
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
            encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth,
            encryptedExpiryYear = encryptedCard.encryptedExpiryYear,
            brand = paymentMethod.type,
        )
    }
}

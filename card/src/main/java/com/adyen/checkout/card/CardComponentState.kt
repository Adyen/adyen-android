/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/2/2021.
 */
package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData

/**
 * PaymentComponentState for CardComponent with additional data.
 */
class CardComponentState(
    paymentComponentData: PaymentComponentData<CardPaymentMethod>,
    isInputValid: Boolean,
    isReady: Boolean,
    val cardType: CardType?,
    val binValue: String,
    val lastFourDigits: String?,
) : PaymentComponentState<CardPaymentMethod>(paymentComponentData, isInputValid, isReady)

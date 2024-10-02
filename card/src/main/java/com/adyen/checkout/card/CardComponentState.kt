/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/2/2021.
 */
package com.adyen.checkout.card

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.CardBrand

/**
 * Represents the state of [CardComponent].
 */
data class CardComponentState(
    override val data: PaymentComponentData<CardPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
    val cardBrand: CardBrand?,
    val binValue: String,
    val lastFourDigits: String?,
) : PaymentComponentState<CardPaymentMethod>

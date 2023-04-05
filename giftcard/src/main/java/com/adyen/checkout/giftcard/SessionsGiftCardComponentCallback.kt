/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/3/2023.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult

// TODO SESSIONS: docs
interface SessionsGiftCardComponentCallback : SessionComponentCallback<GiftCardComponentState> {

    fun onOrder(orderResponse: OrderResponse) = Unit
    fun onBalance(balanceResult: BalanceResult) = Unit
    fun onPartialPayment(result: SessionPaymentResult, order: Order, sessionModel: SessionModel) = Unit
}

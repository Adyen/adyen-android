/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/3/2023.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.sessions.core.SessionComponentCallback

interface SessionsGiftCardComponentCallback : SessionComponentCallback<GiftCardComponentState> {

    fun onOrder(orderResponse: OrderResponse)

    fun onBalance(balanceResult: BalanceResult)
}

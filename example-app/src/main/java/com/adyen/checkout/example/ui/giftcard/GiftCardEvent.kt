/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/4/2023.
 */

package com.adyen.checkout.example.ui.giftcard

import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.action.Action

internal sealed class GiftCardEvent {
    data class PaymentResult(val result: String) : GiftCardEvent()

    data class AdditionalAction(val action: Action) : GiftCardEvent()

    data class OrderCreated(val order: OrderResponse) : GiftCardEvent()

    data class Balance(val balanceResult: BalanceResult) : GiftCardEvent()

    data class ReloadComponent(
        val orderRequest: OrderRequest,
        val giftCardComponentData: GiftCardComponentData
    ) : GiftCardEvent()

    data class ReloadComponentSessions(
        val giftCardComponentData: SessionsGiftCardComponentData
    ) : GiftCardEvent()
}

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
}

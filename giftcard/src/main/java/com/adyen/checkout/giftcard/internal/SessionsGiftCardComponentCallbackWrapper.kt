package com.adyen.checkout.giftcard.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionPaymentResult
import java.lang.ref.WeakReference

/**
 * This class wraps the [SessionComponentCallback] provided by the merchant and implements the [onOrder] and
 * [onBalance] methods so we can continue you the flow correctly with sessions. All other callback methods are
 * propagated to the merchants.
 */
@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionsGiftCardComponentCallbackWrapper(
    component: GiftCardComponent,
    private val componentCallback: SessionsGiftCardComponentCallback,
) : SessionsGiftCardComponentCallback {

    private val componentRef = WeakReference(component)

    override fun onOrder(orderResponse: OrderResponse) {
        componentRef.get()?.resolveOrderResponse(orderResponse)
    }

    override fun onBalance(balanceResult: BalanceResult) {
        componentRef.get()?.resolveBalanceResult(balanceResult)
    }

    override fun onPartialPayment(result: SessionPaymentResult) {
        componentCallback.onPartialPayment(result)
    }

    override fun onStateChanged(state: GiftCardComponentState) {
        componentCallback.onStateChanged(state)
    }

    override fun onAction(action: Action) {
        componentCallback.onAction(action)
    }

    override fun onFinished(result: SessionPaymentResult) {
        componentCallback.onFinished(result)
    }

    override fun onError(componentError: ComponentError) {
        componentCallback.onError(componentError)
    }

    override fun onSubmit(state: GiftCardComponentState): Boolean {
        return componentCallback.onSubmit(state)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData): Boolean {
        return componentCallback.onAdditionalDetails(actionComponentData)
    }

    override fun onBalanceCheck(paymentComponentState: PaymentComponentState<*>): Boolean {
        return componentCallback.onBalanceCheck(paymentComponentState)
    }

    override fun onOrderRequest(): Boolean {
        return componentCallback.onOrderRequest()
    }

    override fun onLoading(isLoading: Boolean) {
        componentCallback.onLoading(isLoading)
    }
}

package com.adyen.checkout.giftcard.internal

import com.adyen.checkout.components.core.internal.BaseComponentCallback
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.giftcard.GiftCardAction
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardException
import kotlinx.coroutines.CoroutineScope

internal class GiftCardComponentEventHandler : ComponentEventHandler<GiftCardComponentState> {

    // no ops
    override fun initialize(coroutineScope: CoroutineScope) = Unit

    // no ops
    override fun onCleared() = Unit

    override fun onPaymentComponentEvent(
        event: PaymentComponentEvent<GiftCardComponentState>,
        componentCallback: BaseComponentCallback
    ) {
        val callback = componentCallback as? GiftCardComponentCallback
            ?: throw CheckoutException(
                "Callback must be type of ${GiftCardComponentCallback::class.java.canonicalName}"
            )
        Logger.v(TAG, "Event received $event")
        when (event) {
            is PaymentComponentEvent.ActionDetails -> callback.onAdditionalDetails(event.data)
            is PaymentComponentEvent.Error -> callback.onError(event.error)
            is PaymentComponentEvent.StateChanged -> callback.onStateChanged(event.state)
            is PaymentComponentEvent.PermissionRequest -> callback.onPermissionRequest(
                event.requiredPermission,
                event.permissionCallback
            )

            is PaymentComponentEvent.Submit -> {
                when (event.state.giftCardAction) {
                    is GiftCardAction.CheckBalance -> {
                        event.state.data.paymentMethod?.let {
                            callback.onBalanceCheck(event.state)
                        } ?: throw GiftCardException(
                            "onBalanceCheck cannot be performed due to payment method being null."
                        )
                    }

                    is GiftCardAction.SendPayment -> {
                        callback.onSubmit(event.state)
                    }

                    is GiftCardAction.CreateOrder -> {
                        callback.onRequestOrder()
                    }

                    is GiftCardAction.Idle -> {
                        // no ops
                        Logger.d(TAG, "No action to be taken.")
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}

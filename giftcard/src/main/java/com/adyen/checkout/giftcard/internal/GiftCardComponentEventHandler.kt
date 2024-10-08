package com.adyen.checkout.giftcard.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.BaseComponentCallback
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.giftcard.GiftCardAction
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardException
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GiftCardComponentEventHandler : ComponentEventHandler<GiftCardComponentState> {

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
                "Callback must be type of ${GiftCardComponentCallback::class.java.canonicalName}",
            )
        adyenLog(AdyenLogLevel.VERBOSE) { "Event received $event" }
        when (event) {
            is PaymentComponentEvent.ActionDetails -> callback.onAdditionalDetails(event.data)
            is PaymentComponentEvent.Error -> callback.onError(event.error)
            is PaymentComponentEvent.StateChanged -> callback.onStateChanged(event.state)
            is PaymentComponentEvent.PermissionRequest -> callback.onPermissionRequest(
                event.requiredPermission,
                event.permissionCallback,
            )

            is PaymentComponentEvent.Submit -> {
                when (event.state.giftCardAction) {
                    is GiftCardAction.CheckBalance -> {
                        event.state.data.paymentMethod?.let {
                            callback.onBalanceCheck(event.state)
                        } ?: throw GiftCardException(
                            "onBalanceCheck cannot be performed due to payment method being null.",
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
                        adyenLog(AdyenLogLevel.DEBUG) { "No action to be taken." }
                    }
                }
            }
        }
    }
}

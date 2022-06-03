/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/11/2021.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.dropin.ui.giftcard.GiftCardPaymentConfirmationData
import com.adyen.checkout.sessions.model.Session

sealed class DropInActivityEvent {
    data class MakePartialPayment(val paymentComponentState: PaymentComponentState<*>) : DropInActivityEvent()
    object ShowPaymentMethods : DropInActivityEvent()
    class CancelOrder(val order: OrderRequest, val isDropInCancelledByUser: Boolean) : DropInActivityEvent()
    object CancelDropIn : DropInActivityEvent()
    class NavigateTo(val destination: DropInDestination) : DropInActivityEvent()
    data class SessionServiceConnected(
        val session: Session,
        val clientKey: String,
        val baseUrl: String,
        val shouldFetchPaymentMethods: Boolean,
        val isFlowTakenOver: Boolean,
    ) : DropInActivityEvent()
}

sealed class DropInDestination {
    object PreselectedStored : DropInDestination()
    object PaymentMethods : DropInDestination()
    class PaymentComponent(val paymentMethod: PaymentMethod) : DropInDestination()
    class ActionComponent(val action: Action) : DropInDestination()
    class GiftCardPaymentConfirmation(val data: GiftCardPaymentConfirmationData) : DropInDestination()
}

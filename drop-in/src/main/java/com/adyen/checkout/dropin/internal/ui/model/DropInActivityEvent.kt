/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/11/2021.
 */

package com.adyen.checkout.dropin.internal.ui.model

import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.sessions.core.SessionModel

internal sealed class DropInActivityEvent {
    data class MakePartialPayment(val paymentComponentState: PaymentComponentState<*>) : DropInActivityEvent()
    object ShowPaymentMethods : DropInActivityEvent()
    class CancelOrder(val order: OrderRequest, val isDropInCancelledByUser: Boolean) : DropInActivityEvent()
    object CancelDropIn : DropInActivityEvent()
    class NavigateTo(val destination: DropInDestination) : DropInActivityEvent()
    data class SessionServiceConnected(
        val sessionModel: SessionModel,
        val clientKey: String,
        val environment: Environment,
        val isFlowTakenOver: Boolean,
        val analyticsManager: AnalyticsManager,
    ) : DropInActivityEvent()
}

internal sealed class DropInDestination {
    object PreselectedStored : DropInDestination()
    object PaymentMethods : DropInDestination()
    class PaymentComponent(val paymentMethod: PaymentMethod) : DropInDestination()
    class GiftCardPaymentConfirmation(val data: GiftCardPaymentConfirmationData) : DropInDestination()
}

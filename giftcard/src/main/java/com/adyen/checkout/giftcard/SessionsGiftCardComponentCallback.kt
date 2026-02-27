/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/3/2023.
 */

package com.adyen.checkout.giftcard

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.old.exception.MethodNotImplementedException
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionPaymentResult
import org.json.JSONObject

/**
 * Implement this callback to interact with a [GiftCardComponent] initialized with a session.
 */
interface SessionsGiftCardComponentCallback : SessionComponentCallback<GiftCardComponentState> {

    /**
     * Indicates that a partial payment has been done. This means an order for this payment has been created and
     * part of the amount is still remaining to be paid. This callback provides you with the necessary objects to
     * be able to create a new session to complete the payment for the remaining amount.
     *
     * @param result The result of the payment.
     */
    fun onPartialPayment(result: SessionPaymentResult)

    // API Events

    /**
     * Only applicable for partial payments flow.
     *
     * Override this method if you want to take over the sessions flow and make a network call to the
     * /paymentMethods/balance endpoint of the Checkout API through your server. This method is called right after the
     * user enters their partial payment method details and submits them.
     *
     * You need to return [true] if you want to take over the sessions flow, otherwise the API calls will still be
     * handled internally by the SDK. This could be useful in case you want to handle the flow yourself only in certain
     * conditions, then you can return [false] if these conditions are not met.
     *
     * Once you take over the flow you will need to handle all the necessary subsequent network calls, otherwise a
     * [MethodNotImplementedException] will be thrown.
     *
     * We provide a [PaymentComponentState] which has a [PaymentComponentData] object containing a non-serialized
     * version of the partial payment method JSON. Use [PaymentMethodDetails.SERIALIZER] to serialize it to a
     * [JSONObject].
     *
     * You should eventually call [component.resolveBalanceResult] with a [BalanceResult] containing the result of the
     * network request.
     *
     * Note that not overriding this method while enabling partial payments will cause a [MethodNotImplementedException]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentState The state from the partial payment method component.
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onBalanceCheck(paymentComponentState: PaymentComponentState<*>): Boolean = false

    /**
     * Only applicable for partial payments flow.
     *
     * Override this method if you want to take over the sessions flow and make a network call to the /orders endpoint
     * of the Checkout API through your server. This method is called when the user is trying to pay a part of the
     * Drop-in amount using a partial payment method.
     *
     * You need to return [true] if you want to take over the sessions flow, otherwise the API calls will still be
     * handled internally by the SDK. This could be useful in case you want to handle the flow yourself only in certain
     * conditions, then you can return [false] if these conditions are not met.
     *
     * Once you take over the flow you will need to handle all the necessary subsequent network calls, otherwise a
     * [MethodNotImplementedException] will be thrown.
     *
     * You should eventually call [component.resolveOrderResponse] with an [OrderResponse] containing the result of the
     * network request.
     *
     * Note that not overriding this method while enabling partial payments will cause a [MethodNotImplementedException]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onOrderRequest(): Boolean = false

    // Internal Events
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun onOrder(orderResponse: OrderResponse) = Unit

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun onBalance(balanceResult: BalanceResult) = Unit
}

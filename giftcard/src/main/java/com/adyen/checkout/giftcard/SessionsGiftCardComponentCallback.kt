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

/**
 * Implement this callback to interact with a [GiftCardComponent] initialized with a session.
 */
interface SessionsGiftCardComponentCallback : SessionComponentCallback<GiftCardComponentState> {

    /**
     * Indicates that an order has been created. You can implement this optional callback to access the response
     * returned from order creation.
     *
     * @param orderResponse The order that has been created to make the payment using gift cards.
     */
    fun onOrder(orderResponse: OrderResponse) = Unit

    /**
     * Indicates that balance check has been done. You can implement this optional callback to access the response
     * returned from balance check.
     *
     * @param balanceResult The result of the balance for the gift card that has been added.
     */
    fun onBalance(balanceResult: BalanceResult) = Unit

    /**
     * Indicates that a partial payment has been done. This means an order for this payment has been created and
     * part of the amount is still remaining to be paid. This callback provides you with the necessary objects to
     * be able to create a new session to complete the payment for the remaining amount.
     *
     * @param result The result of the payment.
     * @param order The order that's been created and used to make the partial payment. To complete the payment
     * for the remaining amount this has to be passed while creating a new session.
     * @param sessionModel The object that contains the necessary information about the session that's used to make
     * the payment. To complete the payment for the remaining amount this has to be passed while creating a new session.
     */
    fun onPartialPayment(result: SessionPaymentResult, order: Order, sessionModel: SessionModel) = Unit
}

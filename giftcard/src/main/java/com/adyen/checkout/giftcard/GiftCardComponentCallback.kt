/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/3/2023.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import org.json.JSONObject

/**
 * Implement this callback to interact with a GiftCardComponent.
 */
interface GiftCardComponentCallback : ComponentCallback<GiftCardComponentState> {
    /**
     * In this method you should make a network call to the /orders endpoint of the Checkout API through your server.
     * This method is called when the user is trying to pay a part of the amount using a partial payment method.
     *
     * You should eventually call [GiftCardComponent.resolveOrderResponse] with a
     * [com.adyen.checkout.components.core.OrderResponse] that you will receive in the response of the API call.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     */
    fun onRequestOrder() = Unit

    /**
     * In this method you should make a network call to the /paymentMethods/balance endpoint of the Checkout API through
     * your server. This method is called right after the user enters their partial payment method details and submits
     * them.
     *
     * We provide a [PaymentMethodDetails] object that contains a non-serialized version of the partial payment method
     * JSON. Use [PaymentMethodDetails.SERIALIZER] to serialize it to a [JSONObject].
     *
     * You should eventually call [GiftCardComponent.resolveBalanceResult] with a
     * [com.adyen.checkout.components.core.BalanceResult] that you will receive in the response of the API call.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentMethodDetails The data from the partial payment method component.
     */
    fun onBalanceCheck(paymentMethodDetails: PaymentMethodDetails) = Unit
}

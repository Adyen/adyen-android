/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.dropin.service

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

// TODO SESSIONS: check docs
interface DropInServiceContract {

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/ endpoint.
     *
     * We provide a [PaymentComponentData] (as JSONObject) with the parameters we can infer from
     * the Component [Configuration] and the user input,
     * specially the "paymentMethod" object with the shopper input details.
     * The rest of the payments/ call object should be filled in, on your server, according to your needs.
     *
     * We also provide a [PaymentComponentState] that contains a non-serialized version of the
     * payment component JSON and might also contain more details about the state of the
     * component at the moment in which the payment is confirmed by the user.
     *
     * - Asynchronous handling:
     *
     *     Since this method runs on the main thread, you should make sure the payments/ call and
     * any other long running operation is made on a background thread. You should eventually call
     * [sendResult] with a [DropInServiceResult] containing the result of the network request.
     * The base class will handle messaging the UI afterwards, based on the [DropInServiceResult].
     *
     *     Note that overriding this method means that the [makePaymentsCall] method will not be
     * called anymore and therefore you can disregard it.
     *
     * - Synchronous handling:
     *
     *     Alternatively, if you don't need asynchronous handling but you still want to access
     * the [PaymentComponentState], you will still need to implement [makePaymentsCall]. After you
     * are done handling the [PaymentComponentState] inside [onPaymentsCallRequested], call
     * [super.onPaymentsCallRequested] to proceed. This will internally invoke your
     * implementation of [makePaymentsCall] in a background thread so you won't need to
     * manage the threads yourself.
     *
     * Note that the [PaymentComponentState] is a abstract class, you can check and cast to
     * one of its child classes for a more component specific state.
     *
     * Only applicable for gift card flow: in case of a partial payment, you should update Drop-in
     * by calling [sendResult] with [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentState The state of the [PaymentComponent] at the moment the user
     * submits the payment.
     */
    fun onSubmit(state: PaymentComponentState<*>)

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/details/
     * endpoint.
     *
     * We provide an [ActionComponentData] (as JSONObject) with the whole result expected by the
     * payments/details/ endpoint (if paymentData was provided).
     *
     * We also provide an [ActionComponentData] that contains a non-serialized version of the
     * action component JSON.
     *
     * - Asynchronous handling:
     *
     *     Since this method runs on the main thread, you should make sure the payments/details/
     * call and any other long running operation is made on a background thread. You should
     * eventually call [sendResult] with a [DropInServiceResult] containing the result of the
     * network request. The base class will handle messaging the UI afterwards, based on the
     * [DropInServiceResult].
     *
     *     Note that overriding this method means that the [makeDetailsCall] method will not be
     * called anymore and therefore you can disregard it.
     *
     * - Synchronous handling:
     *
     *     Alternatively, if you don't need asynchronous handling but you still want to access
     * the [ActionComponentData], you will still need to implement [makeDetailsCall]. After you
     * are done handling the [ActionComponentData] inside [onDetailsCallRequested], call
     * [super.onDetailsCallRequested] to proceed. This will internally invoke your
     * implementation of [makeDetailsCall] in a background thread so you won't need to
     * manage the threads yourself.
     *
     * Only applicable for gift card flow: in case of a partial payment, you should update Drop-in
     * by calling [sendResult] with [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentData The data from the [ActionComponent].
     */
    fun onAdditionalDetails(actionComponentData: ActionComponentData)

    /**
     * Only applicable for gift card flow.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * paymentMethods/balance/ endpoint. This method is called right after the user enters their gift card
     * details and submits them.
     *
     * We provide [paymentMethodData], a [PaymentMethodDetails] object that contains a non-serialized
     * version of the gift card payment method JSON. Use [PaymentMethodDetails.SERIALIZER] to serialize it
     * to a [JSONObject].
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the
     * paymentMethods/balance/ call and any other long running operation is made on a background thread.
     *
     * You should eventually call [sendBalanceResult] with a [BalanceDropInServiceResult] containing the result
     * of the network request. The base class will handle messaging the UI afterwards, based on the
     * [BalanceDropInServiceResult].
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentMethodData The data from the gift card component.
     */
    fun onBalanceCheck(paymentMethodDetails: PaymentMethodDetails) {
        throw NotImplementedError("Method onBalanceCheck is not implemented.")
    }

    /**
     * Only applicable for gift card flow.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * orders/ endpoint. This method is called when the user is trying to pay a part of the Drop-in amount
     * using a gift card.
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the orders/
     * call and any other long running operation is made on a background thread.
     *
     * You should eventually call [sendOrderResult] with a [OrderDropInServiceResult] containing the result of the
     * network request. The base class will handle messaging the UI afterwards, based on the [OrderDropInServiceResult].
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     */
    fun onOrderRequest() {
        throw NotImplementedError("Method onOrderRequest is not implemented.")
    }

    /**
     * Only applicable for gift card flow.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * orders/cancel/ endpoint. This method is called during a partial payment, when the user removes
     * their already paid gift cards either by using the remove button or cancelling Drop-in.
     *
     * We provide [order], an [OrderRequest] object that contains a non-serialized version of the order
     * to be cancelled. Use [OrderRequest.SERIALIZER] to serialize it to a [JSONObject].
     *
     * The [shouldUpdatePaymentMethods] flag indicates the next step you should take after the API call
     * is made:
     * - [true] means that Drop-in is still showing and you should therefore call paymentMethods/
     * then update Drop-in with the new list of payment methods, by passing [DropInServiceResult.Update] to
     * [sendResult].
     * - [false] means that Drop-in is being dismissed by the user so there is no need to do any further calls.
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the
     * paymentMethods/balance/ call and any other long running operation is made on a background thread.
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param order The data from order being cancelled.
     * @param shouldUpdatePaymentMethods indicates whether payment methods should be re-fetched and passed to Drop-in.
     */
    fun onOrderCancel(orderRequest: OrderRequest, shouldUpdatePaymentMethods: Boolean) {
        throw NotImplementedError("Method onOrderCancel is not implemented.")
    }
}

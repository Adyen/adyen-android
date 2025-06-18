/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.dropin

import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.old.exception.MethodNotImplementedException
import org.json.JSONObject

interface SessionDropInServiceContract {

    /**
     * Override this method if you want to take over the sessions flow and make a network call to the /payments endpoint
     * of the Checkout API through your server.
     *
     * You need to return [true] if you want to take over the sessions flow, otherwise the API calls will still be
     * handled internally by the SDK. This could be useful in case you want to handle the flow yourself only in certain
     * conditions, then you can return [false] if these conditions are not met.
     *
     * Once you take over the flow you will need to handle all the necessary subsequent network calls, otherwise a
     * [MethodNotImplementedException] will be thrown. You can use the [isFlowTakenOver] field to check whether you did
     * take over the flow in a previous call or not.
     *
     * We provide a [PaymentComponentState] which contains information about the state of the payment component at the
     * moment the user submits the payment.
     *
     * We also provide inside [PaymentComponentState.data] the parameters that we can infer from the component's
     * configuration and the user input, especially the [state.data.paymentMethod] object with the shopper input
     * details.
     *
     * Use [PaymentComponentData.SERIALIZER] to serialize this data to a [JSONObject]. The rest of the /payments call
     * request data should be filled in, on your server, according to your needs.
     *
     * You should eventually call [sendResult] with a [DropInServiceResult] containing the result of the network
     * request. Drop-in will be updated then based on the [DropInServiceResult] you sent.
     *
     * NOTICE: this method runs on the main thread, you should make sure the API call and any other long running
     * operation is made on a background thread.
     *
     * Note that the [PaymentComponentState] is a abstract class, you can check and cast to one of its subclasses for
     * a more component specific state.
     *
     * Only applicable for partial payments flow: in case of a partial payment, you should update Drop-in by calling
     * [sendResult] with [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param state The state of the payment component at the moment the user submits the payment.
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onSubmit(state: PaymentComponentState<*>): Boolean = false

    /**
     * Override this method if you want to take over the sessions flow and make a network call to the /payments/details
     * endpoint of the Checkout API through your server.
     *
     * You need to return [true] if you want to take over the sessions flow, otherwise the API calls will still be
     * handled internally by the SDK. This could be useful in case you want to handle the flow yourself only in certain
     * conditions, then you can return [false] if these conditions are not met.
     *
     * Once you take over the flow you will need to handle all the necessary subsequent network calls, otherwise a
     * [MethodNotImplementedException] will be thrown. You can use the [isFlowTakenOver] field to check whether you did
     * take over the flow in a previous call or not.
     *
     * We provide inside [ActionComponentData] the whole request data expected by the /payments/details endpoint. Use
     * [ActionComponentData.SERIALIZER] to serialize this data to a [JSONObject].
     *
     * You should eventually call [sendResult] with a [DropInServiceResult] containing the result of the network
     * request. Drop-in will be updated then based on the [DropInServiceResult] you sent.
     *
     * NOTICE: this method runs on the main thread, you should make sure the API call and any other long running
     * operation is made on a background thread.
     *
     * Only applicable for partial payments flow: in case of a partial payment, you should update Drop-in by calling
     * [sendResult] with [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentData The data from the action component.
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onAdditionalDetails(actionComponentData: ActionComponentData): Boolean = false

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
     * [MethodNotImplementedException] will be thrown. You can use the [isFlowTakenOver] field to check whether you did
     * take over the flow in a previous call or not.
     *
     * We provide a [PaymentComponentState] which has a [PaymentComponentData] object containing a non-serialized
     * version of the partial payment method JSON. Use [PaymentMethodDetails.SERIALIZER] to serialize it to a
     * [JSONObject].
     *
     * NOTICE: this method runs on the main thread, you should make sure the API call and any other long running
     * operation is made on a background thread.
     *
     * You should eventually call [sendBalanceResult] with a [BalanceDropInServiceResult] containing the result
     * of the network request. Drop-in will be updated then based on the [BalanceDropInServiceResult] you sent.
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
     * [MethodNotImplementedException] will be thrown. You can use the [isFlowTakenOver] field to check whether you did
     * take over the flow in a previous call or not.
     *
     * NOTICE: this method runs on the main thread, you should make sure the API call and any other long running
     * operation is made on a background thread.
     *
     * You should eventually call [sendOrderResult] with a [OrderDropInServiceResult] containing the result of the
     * network request. The base class will handle messaging the UI afterwards, based on the [OrderDropInServiceResult].
     *
     * Note that not overriding this method while enabling partial payments will cause a [MethodNotImplementedException]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onOrderRequest(): Boolean = false

    /**
     * Only applicable for partial payments flow.
     *
     * Override this method if you want to take over the sessions flow and make a network call to the /orders/cancel
     * endpoint of the Checkout API through your server. This method is called during a partial payment, when the user
     * removes their already submitted partial payments either by using the remove button or cancelling Drop-in.
     *
     * You need to return [true] if you want to take over the sessions flow, otherwise the API calls will still be
     * handled internally by the SDK. This could be useful in case you want to handle the flow yourself only in certain
     * conditions, then you can return [false] if these conditions are not met.
     *
     * Once you take over the flow you will need to handle all the necessary subsequent network calls, otherwise a
     * [MethodNotImplementedException] will be thrown. You can use the [isFlowTakenOver] field to check whether you did
     * take over the flow in a previous call or not.
     *
     * We provide [order], an [Order] object that contains a non-serialized version of the order
     * to be cancelled. Use [Order.SERIALIZER] to serialize it to a [JSONObject].
     *
     * The [shouldUpdatePaymentMethods] flag indicates the next step you should take after the API call
     * is made:
     * - [true] means that Drop-in is still showing and you might want to call /paymentMethods with the new payment
     * amount. Update Drop-in with the new list of payment methods, by passing [DropInServiceResult.Update] to
     * [sendResult].
     * - [false] means that Drop-in is being dismissed by the user so there is no need to make any further calls.
     *
     * NOTICE: this method runs on the main thread, you should make sure the API call and any other long running
     * operation is made on a background thread.
     *
     * Note that not overriding this method while enabling partial payments will cause a [MethodNotImplementedException]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param order The data from order being cancelled.
     * @param shouldUpdatePaymentMethods indicates whether payment methods should be re-fetched and passed to Drop-in.
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onOrderCancel(order: Order, shouldUpdatePaymentMethods: Boolean): Boolean = false
}

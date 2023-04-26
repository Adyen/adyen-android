/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 10/1/2023.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.BaseComponentCallback
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.exception.MethodNotImplementedException
import org.json.JSONObject

/**
 * Implement this callback to interact with a [PaymentComponent] initialized with a session.
 */
interface SessionComponentCallback<T : PaymentComponentState<*>> : BaseComponentCallback {
    // Generic events
    /**
     * Indicates that an action needs to be handled to continue the payment flow. You can call [component.handleAction]
     * to handle this action.
     *
     * @param action The action that need to be handled.
     */
    fun onAction(action: Action)

    /**
     * Indicates that the payment flow has finished.
     *
     * @param result The result of the payment.
     */
    fun onFinished(result: SessionPaymentResult)

    /**
     * The component has encountered an error.
     * Use [ComponentError.exception] to get the internal exception.
     *
     * @param componentError The error encountered.
     */
    fun onError(componentError: ComponentError)

    /**
     * You can implement this optional method to receive an update any time the state of the component changes.
     *
     * This mainly occurs through a user interaction (editing an input, selecting an item from a list, etc) and will be
     * triggered regardless whether the component is valid or not.
     *
     * We provide a [PaymentComponentState] which contains information about the state of the payment component at that
     * moment.
     *
     * @param state The state of the payment component at the current moment.
     */
    fun onStateChanged(state: T) = Unit

    // API events

    /**
     * Override this method if you want to take over the sessions flow and make a network call to the /payments endpoint
     * of the Checkout API through your server.
     *
     * You need to return [true] if you want to take over the sessions flow, otherwise the API calls will still be
     * handled internally by the SDK. This could be useful in case you want to handle the flow yourself only in certain
     * conditions, then you can return [false] if these conditions are not met.
     *
     * Once you take over the flow you will need to handle all the necessary subsequent network calls, otherwise a
     * [MethodNotImplementedException] will be thrown.
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
     * In case you receive an additional action, you can call [component.handleAction] with an [Action] containing the
     * result of the network request.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param state The state of the payment component at the moment the user submits the payment.
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onSubmit(state: T): Boolean = false

    /**
     * Override this method if you want to take over the sessions flow and make a network call to the /payments/details
     * endpoint of the Checkout API through your server.
     *
     * You need to return [true] if you want to take over the sessions flow, otherwise the API calls will still be
     * handled internally by the SDK. This could be useful in case you want to handle the flow yourself only in certain
     * conditions, then you can return [false] if these conditions are not met.
     *
     * Once you take over the flow you will need to handle all the necessary subsequent network calls, otherwise a
     * [MethodNotImplementedException] will be thrown.
     *
     * We provide inside [ActionComponentData] the whole request data expected by the /payments/details endpoint. Use
     * [ActionComponentData.SERIALIZER] to serialize this data to a [JSONObject].
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
     * [MethodNotImplementedException] will be thrown.
     *
     * We provide a [PaymentMethodDetails] object that contains a non-serialized version of the partial payment method
     * JSON. Use [PaymentMethodDetails.SERIALIZER] to serialize it to a [JSONObject].
     *
     * You should eventually call [component.resolveBalanceResult] with a [BalanceResult] containing the result of the
     * network request.
     *
     * Note that not overriding this method while enabling partial payments will cause a [MethodNotImplementedException]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentMethodDetails The data from the partial payment method component.
     * @return [true] if you took over the sessions flow, [false] otherwise.
     */
    fun onBalanceCheck(paymentMethodDetails: PaymentMethodDetails): Boolean = false

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

    /**
     * Indicates that an API call is being executed by the component. Could be used to show a loading indicator in your
     * UI.
     */
    fun onLoading(isLoading: Boolean) = Unit
}

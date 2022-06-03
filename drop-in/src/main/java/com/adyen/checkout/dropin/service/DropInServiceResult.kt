/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.service

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.core.exception.CheckoutException
import org.json.JSONException
import org.json.JSONObject
import com.adyen.checkout.components.model.payments.response.Action as ActionResponse

sealed class BaseDropInServiceResult

internal interface DropInServiceResultError {
    val errorMessage: String?
    val reason: String?
    val dismissDropIn: Boolean
}

/**
 * The result from a server call request on the [DropInService]
 */
sealed class DropInServiceResult : BaseDropInServiceResult() {

    /**
     * Call was successful and payment is finished. This does not necessarily mean that the
     * payment was authorized, it can simply indicate that all the necessary network calls were
     * made without any exceptions or unexpected errors.
     */
    class Finished(val result: String) : DropInServiceResult()

    /**
     * Call was successful and returned with an
     * [com.adyen.checkout.components.model.payments.response.Action] that needs to be handled.
     *
     * Use [com.adyen.checkout.components.model.payments.response.Action.SERIALIZER] to serialize
     * your JSON response string.
     */
    @Suppress("MemberNameEqualsClassName")
    class Action : DropInServiceResult {
        val action: ActionResponse

        constructor(action: ActionResponse) {
            this.action = action
        }

        @Deprecated("Use the new constructor which takes an Action object as parameter")
        constructor(actionJSON: String) {
            val actionJSONObject = try {
                JSONObject(actionJSON)
            } catch (e: JSONException) {
                throw CheckoutException("Provided action is not a JSON object")
            }
            action = ActionResponse.SERIALIZER.deserialize(actionJSONObject)
        }
    }

    /**
     * Only applicable for gift card flow.
     *
     * Update drop-in with a new list of payment methods and optionally an order.
     *
     * After submitting a partial payment, you need to call /paymentMethods again with the new remaining payment amount, and
     * pass the updated payment methods list, alongside the latest order object.
     *
     * Also after cancelling an order, you need to call /paymentMethods again with the original payment amount, and pass the
     * updated payment methods list, with a null order object.
     *
     * Use [OrderResponse.SERIALIZER] to serialize your JSON response string.
     *
     * @param paymentMethodsApiResponse the updated payment methods list.
     * @param order the order object returned from the backend, or null if an order was cancelled.
     */
    class Update(
        val paymentMethodsApiResponse: PaymentMethodsApiResponse,
        val order: OrderResponse?
    ) : DropInServiceResult()

    /**
     * Call failed with an error. Can have the localized error message which will be shown
     * in an Alert Dialog, otherwise a generic error message will be shown.
     */
    class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : DropInServiceResult(), DropInServiceResultError
}

sealed class BalanceDropInServiceResult : BaseDropInServiceResult() {

    /**
     * Only applicable for gift card flow.
     *
     * A call to fetch a gift card balance was successful and returned with a [BalanceResult] that needs to be handled.
     *
     * Use [BalanceResult.SERIALIZER] to serialize your JSON response string.
     */
    class Balance(val balance: BalanceResult) : BalanceDropInServiceResult()

    /**
     * Call failed with an error. Can have the localized error message which will be shown
     * in an Alert Dialog, otherwise a generic error message will be shown.
     */
    class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : BalanceDropInServiceResult(), DropInServiceResultError
}

sealed class OrderDropInServiceResult : BaseDropInServiceResult() {

    /**
     * Only applicable for gift card flow.
     *
     * A call to create a new order was successful and returned with a
     * [OrderResponse] that needs to be handled.
     *
     * Use [OrderResponse.SERIALIZER] to serialize your JSON response string.
     */
    class OrderCreated(val order: OrderResponse) : OrderDropInServiceResult()

    /**
     * Call failed with an error. Can have the localized error message which will be shown
     * in an Alert Dialog, otherwise a generic error message will be shown.
     */
    class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : OrderDropInServiceResult(), DropInServiceResultError
}

sealed class RecurringDropInServiceResult : BaseDropInServiceResult() {

    /**
     * Only applicable to removing stored payment methods.
     *
     * A call to remove a stored payment method was successful.
     *
     * @param id Id of the stored payment method.
     */
    class PaymentMethodRemoved(val id: String) : RecurringDropInServiceResult()

    /**
     * Call failed with an error. Can have the localized error message which will be shown
     * in an Alert Dialog, otherwise a generic error message will be shown.
     */
    class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : RecurringDropInServiceResult(), DropInServiceResultError
}

internal sealed class SessionDropInServiceResult : BaseDropInServiceResult() {

    data class SetupDone(val paymentMethods: PaymentMethodsApiResponse?) : SessionDropInServiceResult()

    data class SessionDataChanged(val sessionData: String) : SessionDropInServiceResult()

    data class SessionTakenOverUpdated(val isFlowTakenOver: Boolean) : SessionDropInServiceResult()

    data class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : SessionDropInServiceResult(), DropInServiceResultError
}

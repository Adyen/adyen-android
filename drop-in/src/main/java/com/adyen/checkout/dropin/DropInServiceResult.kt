/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin

import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.adyen.checkout.components.core.action.Action as ActionResponse

sealed class BaseDropInServiceResult

internal interface DropInServiceResultError {
    val errorMessage: String?
    val reason: String?
    val dismissDropIn: Boolean
}

/**
 * The result of a network call to be sent to [DropInService] or [SessionDropInService].
 */
sealed class DropInServiceResult : BaseDropInServiceResult() {

    /**
     * A call to /payments or /payments/details was successful and the checkout flow is finished. This does not
     * necessarily mean that the payment was authorized, it can simply indicate that all the necessary network calls
     * were made without any exceptions or unexpected errors.
     *
     * @param result The final result of the checkout flow. You will receive this value back in your [DropInCallback]
     * class. This value is not used internally by Drop-in.
     */
    class Finished(val result: String) : DropInServiceResult()

    /**
     * A call to /payments or /payments/details was successful and returned with an action that needs to be handled.
     *
     * Use [ActionResponse.SERIALIZER] to deserialize your JSON response string.
     *
     * @param action the action object to be handled by Drop-in.
     */
    class Action(val action: ActionResponse) : DropInServiceResult()

    /**
     * Only applicable for partial payments flow.
     *
     * Update Drop-in with a new list of payment methods and optionally an order.
     *
     * After submitting a partial payment, you might need to call /paymentMethods again with the new remaining amount,
     * and pass the updated payment methods list, alongside the latest order object.
     *
     * Also after cancelling an order, you need to call /paymentMethods again with the original payment amount, and pass
     * the updated payment methods list, with a null order object.
     *
     * Use [OrderResponse.SERIALIZER] to deserialize your JSON response string.
     *
     * @param paymentMethodsApiResponse the updated payment methods list.
     * @param order the order object returned from the backend, or null if an order was cancelled.
     */
    class Update(
        val paymentMethodsApiResponse: PaymentMethodsApiResponse,
        val order: OrderResponse?
    ) : DropInServiceResult()

    /**
     * Send this to display an error dialog and optionally dismiss Drop-in.
     *
     * @param errorMessage the localized error message to be shown in an Alert Dialog. If not provided a generic error
     * message will be shown.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : DropInServiceResult(), DropInServiceResultError
}

sealed class BalanceDropInServiceResult : BaseDropInServiceResult() {

    /**
     * Only applicable for partial payments flow.
     *
     * A call to /paymentMethods/balance was successful and returned with a [BalanceResult] that needs to be handled.
     *
     * Use [BalanceResult.SERIALIZER] to deserialize your JSON response string.
     */
    class Balance(val balance: BalanceResult) : BalanceDropInServiceResult()

    /**
     * Only applicable for partial payments flow.
     *
     * Send this to display an error dialog and optionally dismiss Drop-in.
     *
     * @param errorMessage the localized error message to be shown in an Alert Dialog. If not provided a generic error
     * message will be shown.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : BalanceDropInServiceResult(), DropInServiceResultError
}

sealed class OrderDropInServiceResult : BaseDropInServiceResult() {

    /**
     * Only applicable for partial payments flow.
     *
     * A call to /orders was successful and returned with an order that needs to be handled.
     *
     * Use [OrderResponse.SERIALIZER] to deserialize your JSON response string.
     */
    class OrderCreated(val order: OrderResponse) : OrderDropInServiceResult()

    /**
     * Only applicable for partial payments flow.
     *
     * Send this to display an error dialog and optionally dismiss Drop-in.
     *
     * @param errorMessage the localized error message to be shown in an Alert Dialog. If not provided a generic error
     * message will be shown.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
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
     * Only applicable to removing stored payment methods.
     *
     * Send this to display an error dialog and optionally dismiss Drop-in.
     *
     * @param errorMessage the localized error message to be shown in an Alert Dialog. If not provided a generic error
     * message will be shown.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : RecurringDropInServiceResult(), DropInServiceResultError
}

internal sealed class SessionDropInServiceResult : BaseDropInServiceResult() {

    data class SessionDataChanged(val sessionData: String) : SessionDropInServiceResult()

    data class SessionTakenOverUpdated(val isFlowTakenOver: Boolean) : SessionDropInServiceResult()

    data class Error(
        override val errorMessage: String? = null,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false
    ) : SessionDropInServiceResult(), DropInServiceResultError

    class Finished(val result: SessionPaymentResult) : SessionDropInServiceResult()
}

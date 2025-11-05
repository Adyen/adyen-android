/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old

import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.adyen.checkout.components.core.action.Action as ActionResponse

sealed class BaseDropInServiceResult

internal interface DropInServiceResultError {
    val reason: String?
    val dismissDropIn: Boolean
    val errorDialog: ErrorDialog?
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
     * @param finishedDialog If set, a dialog will be shown with the data passed in [FinishedDialog]. If null, no
     * dialog will be displayed.
     */
    class Finished(
        val result: String,
        val finishedDialog: FinishedDialog? = null,
    ) : DropInServiceResult()

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
     * @param errorDialog If set, a dialog will be shown with the data passed in [ErrorDialog]. If null, no
     * dialog will be displayed.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorDialog: ErrorDialog?,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false,
    ) : DropInServiceResult(), DropInServiceResultError

    /**
     * Send this to navigate to the payment methods list. Optionally provide a [PaymentMethodsApiResponse] to refresh
     * the displayed payment methods.
     *
     * @param paymentMethodsApiResponse Optionally pass this to refresh the displayed payment methods.
     */
    class ToPaymentMethodsList(
        val paymentMethodsApiResponse: PaymentMethodsApiResponse? = null,
    ) : DropInServiceResult()
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
     * @param errorDialog If set, a dialog will be shown with the data passed in [ErrorDialog]. If null, no
     * dialog will be displayed.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorDialog: ErrorDialog?,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false,
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
     * @param errorDialog If set, a dialog will be shown with the data passed in [ErrorDialog]. If null, no
     * dialog will be displayed.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorDialog: ErrorDialog?,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false,
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
     * @param errorDialog If set, a dialog will be shown with the data passed in [ErrorDialog]. If null, no
     * dialog will be displayed.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorDialog: ErrorDialog?,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false,
    ) : RecurringDropInServiceResult(), DropInServiceResultError
}

sealed class AddressLookupDropInServiceResult : BaseDropInServiceResult() {

    /**
     * Only applicable to address lookup flow.
     *
     * Send this to display the options received for the query shopper has inputted.
     *
     * @param options Address options to be displayed to the shopper.
     */
    class LookupResult(
        val options: List<LookupAddress>
    ) : AddressLookupDropInServiceResult()

    /**
     * Only applicable to address lookup flow.
     *
     * Send this to prefill the address after making an api call to fetch the complete address details.
     *
     * @param lookupAddress Complete address details.
     */
    class LookupComplete(
        val lookupAddress: LookupAddress
    ) : AddressLookupDropInServiceResult()

    /**
     * * Only applicable to address lookup flow.
     *
     * Send this to display an error dialog and optionally dismiss Drop-in.
     *
     * @param errorDialog If set, a dialog will be shown with the data passed in [ErrorDialog]. If null, no
     * dialog will be displayed.
     * @param reason the reason of the error. You will receive this value back in your [DropInCallback] class. This
     * value is not used internally by Drop-in.
     * @param dismissDropIn whether Drop-in should be dismissed after presenting the Alert Dialog.
     */
    class Error(
        override val errorDialog: ErrorDialog?,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false,
    ) : AddressLookupDropInServiceResult(), DropInServiceResultError
}

internal sealed class SessionDropInServiceResult : BaseDropInServiceResult() {

    data class SessionDataChanged(val sessionData: String) : SessionDropInServiceResult()

    data class SessionTakenOverUpdated(val isFlowTakenOver: Boolean) : SessionDropInServiceResult()

    data class Error(
        override val errorDialog: ErrorDialog?,
        override val reason: String? = null,
        override val dismissDropIn: Boolean = false,
    ) : SessionDropInServiceResult(), DropInServiceResultError

    class Finished(val result: SessionPaymentResult) : SessionDropInServiceResult()
}

/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.service

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
     */
    class Action(val actionJSON: String) : DropInServiceResult()

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
     * A call to fetch a gift card balance was successful and returned with a
     * [com.adyen.checkout.components.model.payments.response.BalanceResult] that needs to be handled.
     */
    class Balance(val balanceJSON: String) : BalanceDropInServiceResult()

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

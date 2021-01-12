/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.service

/**
 * The result from a server call request on the [DropInService]
 */
sealed class DropInServiceResult {

    /**
     * Call was successful and payment is finished.
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
    class Error(val errorMessage: String? = null, val reason: String? = null, val dismissDropIn: Boolean = false) : DropInServiceResult()

    /**
     * Call wants to wait for asynchronous processing.
     */
    class Wait : DropInServiceResult()
}

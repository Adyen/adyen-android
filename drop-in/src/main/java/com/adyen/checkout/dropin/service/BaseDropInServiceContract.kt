/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.dropin.service

import android.os.Bundle
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.dropin.DropInConfiguration

interface BaseDropInServiceContract {

    /**
     * Only applicable to removing stored payment methods.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * Recurring/<version_number>/disable endpoint. This method is called when the user initiates
     * removing a stored payment method using the remove button.
     *
     * We provide [storedPaymentMethod] that contains the id of the stored payment method to be removed
     * in the field [StoredPaymentMethod.id].
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the
     * Recurring/<version>/disable call and any other long running operation is made on a background thread.
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     */
    fun onRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        throw NotImplementedError("Method onRemoveStoredPaymentMethod is not implemented")
    }

    /**
     * Allow asynchronously sending the results of the payments/ and payments/details/ network
     * calls.
     *
     * Call this method when using [onPaymentsCallRequested] and [onDetailsCallRequested] with a
     * [DropInServiceResult] depending on the response of the corresponding network call.
     * Check the subclasses of [DropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    fun sendResult(result: DropInServiceResult)

    /**
     * Allow asynchronously sending the results of the paymentMethods/balance/ network call.
     *
     * Call this method when using [checkBalance] with a [BalanceDropInServiceResult] depending
     * on the response of the corresponding network call.
     * Check the subclasses of [BalanceDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    fun sendBalanceResult(result: BalanceDropInServiceResult)

    /**
     * Allow asynchronously sending the results of the orders/ network call.
     *
     * Call this method when using [createOrder] with a [OrderDropInServiceResult] depending
     * on the response of the corresponding network call.
     * Check the subclasses of [OrderDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    fun sendOrderResult(result: OrderDropInServiceResult)

    /**
     * Allow asynchronously sending the results of the Recurring/ network call.
     *
     * Call this method after making a network call to remove a stored payment method
     * while using [onRemoveStoredPaymentMethod] and pass an instance of [RecurringDropInServiceResult]
     * depending on the response of the corresponding network call.
     * Check the subclasses of [RecurringDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    fun sendRecurringResult(result: RecurringDropInServiceResult)

    /**
     * Gets the additional data that was set when starting drop-in using
     * [DropInConfiguration.Builder.setAdditionalDataForDropInService] or null if nothing was set.
     */
    fun getAdditionalData(): Bundle?
}

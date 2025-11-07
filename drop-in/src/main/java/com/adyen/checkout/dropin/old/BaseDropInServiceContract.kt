/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old

import android.os.Bundle
import com.adyen.checkout.card.old.BinLookupData
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.core.old.exception.MethodNotImplementedException

@Suppress("TooManyFunctions")
interface BaseDropInServiceContract {

    /**
     * Only applicable to removing stored payment methods. Use
     * [DropInConfiguration.Builder.setEnableRemovingStoredPaymentMethods] to enable this feature.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * DELETE /storedPaymentMethods endpoint. This method is called when the user initiates
     * removing a stored payment method using the remove button.
     *
     * We provide [storedPaymentMethod] that contains the id of the stored payment method to be removed
     * in the field [StoredPaymentMethod.id].
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the
     * DELETE /storedPaymentMethods call and any other long running operation is made on a background thread.
     *
     * Use [sendRecurringResult] to send the final result of this call back to the Drop-in.
     *
     * Note that not overriding this method while enabling this feature will cause a [MethodNotImplementedException] to
     * be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     */
    fun onRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        throw MethodNotImplementedException("Method onRemoveStoredPaymentMethod is not implemented")
    }

    /**
     * Allows sending the result of the /payments and /payments/details network calls.
     *
     * Call this method with a [DropInServiceResult] depending on the response of the corresponding network call.
     *
     * Check the subclasses of [DropInServiceResult] for more information.
     *
     * @param result the result of the network call.
     */
    fun sendResult(result: DropInServiceResult)

    /**
     * Allows sending the result of the /paymentMethods/balance network call.
     *
     * Call this method with a [BalanceDropInServiceResult] depending on the response of the corresponding network call.
     *
     * Check the subclasses of [BalanceDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    fun sendBalanceResult(result: BalanceDropInServiceResult)

    /**
     * Allows sending the result of the /orders network call.
     *
     * Call this method with a [OrderDropInServiceResult] depending on the response of the corresponding network call.
     *
     * Check the subclasses of [OrderDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    fun sendOrderResult(result: OrderDropInServiceResult)

    /**
     * Allows sending the result of the DELETE /storedPaymentMethods network call.
     *
     * Call this method with a [RecurringDropInServiceResult] depending on the response of the corresponding network
     * call.
     *
     * Check the subclasses of [RecurringDropInServiceResult] for more information.
     *
     * @param result the result of the network call.
     */
    fun sendRecurringResult(result: RecurringDropInServiceResult)

    /**
     * Allows sending the result of Address Lookup operations.
     *
     * Call this method with a [AddressLookupDropInServiceResult] depending on the performed address lookup action.
     *
     * Check the subclasses of [AddressLookupDropInServiceResult] for more information.
     *
     * @param result the result of the action.
     */
    fun sendAddressLookupResult(result: AddressLookupDropInServiceResult)

    /**
     * Gets the additional data that was set when starting Drop-in using
     * [DropInConfiguration.Builder.setAdditionalDataForDropInService] or null if nothing was set.
     */
    fun getAdditionalData(): Bundle?

    /**
     * Set a callback that will be called when a redirect is made.
     */
    fun onRedirect() = Unit

    /**
     * Set a callback that will be called when the bin value changes.
     *
     * @param binValue Up to the first 6 or 8 digits of the card number.
     */
    fun onBinValue(binValue: String) = Unit

    /**
     * Set a callback that will be called when a bin lookup is performed.
     *
     * @param data A list of [BinLookupData], which contains information about the detected brands.
     */
    fun onBinLookup(data: List<BinLookupData>) = Unit

    /**
     * Set a callback that will be called when shopper inputs a query to perform address lookup.
     *
     * @param query Query inputted by shopper.
     */
    fun onAddressLookupQueryChanged(query: String) {
        throw MethodNotImplementedException("Method onAddressLookupQueryChanged is not implemented")
    }

    /**
     * Set a callback that will be called when shopper chooses an address option that requires complete details to be
     * provided.
     *
     * @param lookupAddress Address option selected by shopper.
     */
    fun onAddressLookupCompletion(lookupAddress: LookupAddress) = false
}

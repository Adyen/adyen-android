/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/12/2023.
 */

package com.adyen.checkout.components.core

/**
 * Implement this callback to be able to use Address Lookup functionality.
 */
interface AddressLookupCallback {

    /**
     * In this method you will receive the query as shopper types it.
     *
     * This query is to be used to perform an address search operation.
     *
     * @param query The search query.
     */
    fun onQueryChanged(query: String)

    /**
     * In this method you will receive a [LookupAddress] object that is incomplete.
     *
     * This callback should be used to retrieve the complete details of the given [LookupAddress].
     *
     * @param lookupAddress The address.
     */
    fun onLookupCompletion(lookupAddress: LookupAddress) = false
}

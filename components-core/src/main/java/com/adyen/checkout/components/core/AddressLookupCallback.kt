/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/12/2023.
 */

package com.adyen.checkout.components.core

/**
 * TODO docs
 */
interface AddressLookupCallback {

    fun onQueryChanged(query: String)

    fun onLookupCompleted(lookupAddress: LookupAddress) = false
}

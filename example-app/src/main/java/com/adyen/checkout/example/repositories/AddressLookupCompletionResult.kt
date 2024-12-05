/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/6/2024.
 */

package com.adyen.checkout.example.repositories

import com.adyen.checkout.components.core.LookupAddress

sealed class AddressLookupCompletionResult {
    data class Error(val message: String = "Something went wrong") : AddressLookupCompletionResult()
    data class Address(val lookupAddress: LookupAddress) : AddressLookupCompletionResult()
}

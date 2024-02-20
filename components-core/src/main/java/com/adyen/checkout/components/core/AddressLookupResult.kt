/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/1/2024.
 */

package com.adyen.checkout.components.core

/**
 * A class that contains the result of address lookup completion call.
 */
sealed class AddressLookupResult {
    /**
     * An error occurred while making of the call.
     *
     * @param message Error message to be shown to shopper.
     */
    data class Error(val message: String? = null) : AddressLookupResult()

    /**
     * Completion call has been successfully completed.
     *
     * @param lookupAddress The complete address details.
     */
    data class Completed(val lookupAddress: LookupAddress) : AddressLookupResult()
}

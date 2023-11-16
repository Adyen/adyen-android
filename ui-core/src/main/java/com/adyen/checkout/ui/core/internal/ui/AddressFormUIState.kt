/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 13/1/2023.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class AddressFormUIState {
    NONE,
    POSTAL_CODE,
    FULL_ADDRESS;

    companion object {
        /**
         * Get visibility state of the address form.
         *
         * @param addressParams Configuration object for address form.
         *
         * @return Visibility state of the address form.
         */
        fun fromAddressParams(addressParams: AddressParams): AddressFormUIState {
            return when (addressParams) {
                is AddressParams.FullAddress -> FULL_ADDRESS
                is AddressParams.PostalCode -> POSTAL_CODE
                is AddressParams.None -> NONE
            }
        }
    }
}

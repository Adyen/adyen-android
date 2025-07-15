/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.old.internal.ui.model.AddressParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class AddressFormUIState {
    NONE,
    POSTAL_CODE,
    FULL_ADDRESS,
    LOOKUP;

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
                is AddressParams.Lookup -> LOOKUP
            }
        }
    }
}

package com.adyen.checkout.card

import com.adyen.checkout.components.base.AddressVisibility

enum class AddressFormUIState {
    NONE, POSTAL_CODE, FULL_ADDRESS;

    companion object {
        fun fromAddressConfiguration(addressConfiguration: AddressConfiguration): AddressFormUIState {
            return when (addressConfiguration) {
                is AddressConfiguration.FullAddress -> FULL_ADDRESS
                is AddressConfiguration.PostalCode -> POSTAL_CODE
                is AddressConfiguration.None -> NONE
            }
        }

        fun fromAddressVisibility(addressVisibility: AddressVisibility): AddressFormUIState {
            return when (addressVisibility) {
                AddressVisibility.POSTAL_CODE -> POSTAL_CODE
                AddressVisibility.NONE -> NONE
            }
        }
    }
}

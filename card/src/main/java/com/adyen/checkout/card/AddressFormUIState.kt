package com.adyen.checkout.card

import com.adyen.checkout.components.base.AddressVisibility

enum class AddressFormUIState {
    NONE, POSTAL_CODE, FULL_ADDRESS;

    companion object {
        fun fromAddressConfiguration(addressConfiguration: AddressConfiguration): AddressFormUIState {
            return when (addressConfiguration) {
                is AddressConfiguration.FullAddress -> AddressFormUIState.FULL_ADDRESS
                is AddressConfiguration.PostalCode -> AddressFormUIState.POSTAL_CODE
                is AddressConfiguration.None -> AddressFormUIState.NONE
            }
        }

        fun fromAddressVisibility(addressVisibility: AddressVisibility): AddressFormUIState {
            return when (addressVisibility) {
                AddressVisibility.POSTAL_CODE -> AddressFormUIState.FULL_ADDRESS
                AddressVisibility.NONE -> AddressFormUIState.POSTAL_CODE
            }
        }
    }
}

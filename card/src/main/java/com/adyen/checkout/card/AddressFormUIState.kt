package com.adyen.checkout.card

enum class AddressFormUIState {
    NONE, POSTAL_CODE, FULL_ADDRESS;

    companion object {
        /**
         * Get visibility state of the address form.
         *
         * @param addressConfiguration Configuration object for address form.
         *
         * @return Visibility state of the address form.
         */
        fun fromAddressConfiguration(addressConfiguration: AddressConfiguration): AddressFormUIState {
            return when (addressConfiguration) {
                is AddressConfiguration.FullAddress -> FULL_ADDRESS
                is AddressConfiguration.PostalCode -> POSTAL_CODE
                is AddressConfiguration.None -> NONE
            }
        }
    }
}

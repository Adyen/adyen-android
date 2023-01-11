package com.adyen.checkout.card

enum class AddressFormUIState {
    NONE, POSTAL_CODE, FULL_ADDRESS;

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

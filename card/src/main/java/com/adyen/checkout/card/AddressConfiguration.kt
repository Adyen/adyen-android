package com.adyen.checkout.card

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration class for Address Form in Card Component. This class can be used define the
 * visibility of the address form.
 */
sealed class AddressConfiguration : Parcelable {

    /**
     * Address Form will be hidden.
     */
    @Parcelize
    object None : AddressConfiguration()

    /**
     * Only postal code will be shown as part of the card component.
     */
    @Parcelize
    data class PostalCode(
        val addressFieldPolicy: CardAddressFieldPolicy = CardAddressFieldPolicy.Required()
    ) : AddressConfiguration()

    /**
     * Full Address Form will be shown as part of the card component.
     *
     * @param defaultCountryCode Default country to be selected while initializing the form.
     * @param supportedCountryCodes Supported country codes to be filtered from the available country
     * options.
     */
    @Parcelize
    data class FullAddress(
        val defaultCountryCode: String? = null,
        val supportedCountryCodes: List<String> = emptyList(),
        val addressFieldPolicy: CardAddressFieldPolicy = CardAddressFieldPolicy.Required()
    ) : AddressConfiguration()

    /**
     * Configuration for requirement of the address fields.
     */
    sealed class CardAddressFieldPolicy : Parcelable {

        /**
         * Address form fields will be required.
         */
        @Parcelize
        class Required : CardAddressFieldPolicy()

        /**
         * Address form fields will be optional.
         */
        @Parcelize
        class Optional : CardAddressFieldPolicy()

        /**
         * Address form fields will be optional for given [brands] and required for the other brands.
         */
        @Parcelize
        data class OptionalForCardTypes(val brands: List<String>) : CardAddressFieldPolicy()
    }
}

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
    object PostalCode : AddressConfiguration()

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
        val supportedCountryCodes: List<String> = emptyList()
    ) : AddressConfiguration()
}

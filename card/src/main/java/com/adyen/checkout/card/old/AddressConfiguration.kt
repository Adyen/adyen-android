package com.adyen.checkout.card.old

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration class for Address Form in Card Component. This class can be used define the
 * visibility of the address form.
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
sealed class AddressConfiguration : Parcelable {

    /**
     * Address Form will be hidden.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    @SuppressLint("ObjectInPublicSealedClass")
    @Parcelize
    object None : AddressConfiguration()

    /**
     * Only postal code will be shown as part of the card component.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
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
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    @Parcelize
    data class FullAddress(
        val defaultCountryCode: String? = null,
        val supportedCountryCodes: List<String> = emptyList(),
        val addressFieldPolicy: CardAddressFieldPolicy = CardAddressFieldPolicy.Required()
    ) : AddressConfiguration()

    /**
     * Address Lookup option will be shown as part of card component.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    @Parcelize
    class Lookup : AddressConfiguration()

    /**
     * Configuration for requirement of the address fields.
     */
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    sealed class CardAddressFieldPolicy : Parcelable {

        /**
         * Address form fields will be required.
         */
        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        @Parcelize
        class Required : CardAddressFieldPolicy()

        /**
         * Address form fields will be optional.
         */
        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        @Parcelize
        class Optional : CardAddressFieldPolicy()

        /**
         * Address form fields will be optional for given [brands] and required for the other brands.
         */
        @Deprecated(
            message = "Deprecated. This will be removed in a future release.",
            level = DeprecationLevel.WARNING,
        )
        @Parcelize
        data class OptionalForCardTypes(val brands: List<String>) : CardAddressFieldPolicy()
    }
}

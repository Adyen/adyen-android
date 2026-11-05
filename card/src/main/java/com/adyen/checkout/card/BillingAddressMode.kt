package com.adyen.checkout.card

import android.os.Parcelable
import com.adyen.checkout.core.common.CardBrand
import kotlinx.parcelize.Parcelize

/**
 * The display mode for the Billing Address Form in Card Component.
 */
sealed class BillingAddressMode : Parcelable {

    /**
     * Billing address form will not be shown.
     */
    @Parcelize
    class None : BillingAddressMode() {
        override fun equals(other: Any?) = other is None

        override fun hashCode() = javaClass.hashCode()
    }

    /**
     * Only postal code will be shown as part of the card component
     */
    @Parcelize
    class PostalCode : BillingAddressMode() {
        override fun equals(other: Any?) = other is PostalCode

        override fun hashCode() = javaClass.hashCode()
    }

    /**
     * Full Address Form will be requested as part of the card component.
     *
     * @param supportedCountryCodes ISO country codes supported for the billing address, as uppercase ISO 3166-1 alpha-2
     *                              codes such as `NL`. When empty, all countries are available.
     * @param hideForCardBrands The [CardBrand]s for which the address form will be hidden.
     */
    @Parcelize
    data class Full(
        val supportedCountryCodes: Set<String> = emptySet(),
        val hideForCardBrands: Set<CardBrand> = emptySet()
    ) : BillingAddressMode()

    /* The Lookup below is for the types completeness.
    Uncomment and refine it once implementing that flow.

    /**
     * Address Lookup option will be shown as part of card component.
     */
    @Parcelize
    class Lookup : BillingAddressMode() */
}

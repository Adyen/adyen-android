package com.adyen.checkout.card

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The display mode for the Billing Address Form in Card Component.
 */
sealed class BillingAddressMode : Parcelable {

    /**
     * Billing address form will not be shown.
     */
    @Parcelize
    class None : BillingAddressMode()

    /**
     * Only postal code will be shown as part of the card component
     */
    @Parcelize
    class PostalCode : BillingAddressMode()

    /**
     * The full billing address will be requested on a separate screen, opened from the card component.
     *
     * @param supportedCountryCodes The ISO 3166-1 alpha-2 codes of the countries the shopper can pick. When empty, all
     * countries are available.
     */
    @Parcelize
    class Full(
        val supportedCountryCodes: Set<String> = emptySet(),
    ) : BillingAddressMode()

    /* The Lookup below is for the types completeness.
    Uncomment and refine it once implementing that flow.

    /**
     * Address Lookup option will be shown as part of card component.
     */
    @Parcelize
    class Lookup : BillingAddressMode() */
}

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

    /* The Full and Lookup below are for the types completeness.
    Uncomment and refine them once implementing that flows.

    /**
     * Full Address Form will be shown as part of the card component.
     *
     * @param supportedCountryCodes Supported country codes to be filtered from the available country options.
     * @param hideForCardTypes The [CardType]s for which the address form will be hidden.
     */
    @Parcelize
    data class Full(
        val supportedCountryCodes: Set<String> = emptySet(),
        val hideForCardTypes: Set<CardType> = emptySet()
    ) : BillingAddressMode()

    /**
     * Address Lookup option will be shown as part of card component.
     */
    @Parcelize
    class Lookup : BillingAddressMode() */
}

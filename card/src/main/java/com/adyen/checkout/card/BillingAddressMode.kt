package com.adyen.checkout.card

import android.os.Parcelable
import com.adyen.checkout.core.common.CardType
import kotlinx.parcelize.Parcelize

/**
 * The display mode for the Billing Address Form in Card Component.
 */
sealed class BillingAddressMode : Parcelable {

    /**
     * Billing address form will not be shown.
     */
    @Parcelize
    data object None : BillingAddressMode()

    /**
     * Only postal code will be shown as part of the card component
     */
    @Parcelize
    data object PostalCode : BillingAddressMode()

    /**
     * Full Address Form will be shown as part of the card component.
     * TODO add explanation of the parameters
     */
    @Parcelize
    data class Full(
        // TODO shall we make this a set?
        val supportedCountryCodes: List<String> = emptyList(),
        // TODO are we good with hideForCardTypes name?
        val hideForCardTypes: Set<CardType> = emptySet()
    ) : BillingAddressMode()
}

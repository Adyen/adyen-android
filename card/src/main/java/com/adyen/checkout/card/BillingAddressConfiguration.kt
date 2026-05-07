package com.adyen.checkout.card

import android.os.Parcelable
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import kotlinx.parcelize.Parcelize

/**
 * Configuration class for Billing Address Form in Card Component.
 * This class is used to define the visibility of the billing address form.
 */
@Parcelize
class BillingAddressConfiguration(
    val billingAddressMode: BillingAddressMode,
    val supportedCountryCodes: List<String>? = null,
    val hideForCardTypes: Set<CardType> = emptySet()
) : Parcelable {

    /** The display mode for the billing address form. */
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
         */
        @Parcelize
        data object Full : BillingAddressMode()
    }
}

class BillingAddressConfigurationBuilder internal constructor() {

    var billingAddressMode: BillingAddressConfiguration.BillingAddressMode =
        BillingAddressConfiguration.BillingAddressMode.None
    var supportedCountryCodes: List<String>? = null
    var hideForCardTypes: Set<CardType> = emptySet()

    internal fun build() = BillingAddressConfiguration(
        billingAddressMode = billingAddressMode,
        supportedCountryCodes = supportedCountryCodes,
        hideForCardTypes = hideForCardTypes,
    )
}

fun CardConfigurationBuilder.billingAddress(
    configuration: @CheckoutConfigurationMarker BillingAddressConfigurationBuilder.() -> Unit = {},
) {
    billingAddressConfiguration = BillingAddressConfigurationBuilder()
        .apply(configuration)
        .build()
}

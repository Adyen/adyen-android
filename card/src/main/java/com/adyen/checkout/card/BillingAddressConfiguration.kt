package com.adyen.checkout.card

import android.os.Parcelable
import androidx.annotation.RestrictTo
import kotlinx.parcelize.Parcelize

// TODO Remove the RestrictTo annotation after aligning the class API with other platforms
/**
 * Configuration class for Billing Address Form in Card Component.
 * This class is used to define the visibility of the billing address form.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class BillingAddressConfiguration : Parcelable {

    /**
     * Billing Address Form will be hidden.
     */
    @Parcelize
    object None : BillingAddressConfiguration()

    /**
     * Only postal code will be shown as part of the billing address form.
     */
    @Parcelize
    data class PostalCode(
        val fieldPolicy: CardBillingAddressFieldPolicy = CardBillingAddressFieldPolicy.Required()
    ) : BillingAddressConfiguration()


    // TODO Remove the RestrictTo annotation after aligning the class API with other platforms
    /**
     * Configuration for requirement of the address fields.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    sealed class CardBillingAddressFieldPolicy : Parcelable {

        /**
         * Address form fields will be required.
         */
        @Parcelize
        class Required : CardBillingAddressFieldPolicy()

        /**
         * Address form fields will be optional.
         */
        @Parcelize
        class Optional : CardBillingAddressFieldPolicy()

        /**
         * Address form fields will be optional for given [brands] and required for the other brands.
         */
        @Parcelize
        data class OptionalForCardTypes(val brands: List<String>) : CardBillingAddressFieldPolicy()
    }
}

package com.adyen.checkout.card

/**
 * Class that contains data about the entered card number.
 *
 * @param brand The detected brand.
 * @param paymentMethodVariant The type of payment method. See the
 * [docs](https://docs.adyen.com/development-resources/paymentmethodvariant/) for more information.
 */
data class BinLookupData(
    val brand: String,
    val paymentMethodVariant: String?,
)

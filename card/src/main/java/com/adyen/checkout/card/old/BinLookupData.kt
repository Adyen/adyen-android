package com.adyen.checkout.card.old

/**
 * Class that contains data about the entered card number.
 *
 * @param brand The detected brand.
 * @param paymentMethodVariant The type of payment method. See the
 * [docs](https://docs.adyen.com/development-resources/paymentmethodvariant/) for more information.
 * @param isReliable Indicates whether the data is reliable. If true, the card number is checked with Adyen systems.
 * If false, the data is provided by a local detection algorithm.
 */
data class BinLookupData(
    val brand: String,
    val paymentMethodVariant: String?,
    val isReliable: Boolean,
)

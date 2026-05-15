package com.adyen.checkout.card

/**
 * Represents a card brand detected during a BIN lookup.
 *
 * @param brand The identifier of the card brand (e.g. "visa", "mc").
 * @param supported Whether this brand is supported by the merchant's configuration.
 * @param paymentMethodVariant The type of payment method variant, or `null` if not available.
 * See the [docs](https://docs.adyen.com/development-resources/paymentmethodvariant/) for more information.
 */
data class BinLookupBrand(
    val brand: String,
    val supported: Boolean,
    val paymentMethodVariant: String?,
)

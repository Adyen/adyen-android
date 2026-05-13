package com.adyen.checkout.card

/**
 * Data about the result of a BIN lookup for the entered card number.
 *
 * @param issuingCountryCode The country code of the card issuer, or `null` if not available.
 * @param brands The list of detected card brands.
 */
data class BinLookupData(
    val issuingCountryCode: String?,
    val brands: List<BinLookupBrand>,
)

package com.adyen.checkout.card

/**
 * Data about the result of a BIN lookup for the entered card number.
 *
 * @param brands The list of detected card brands.
 * @param issuingCountryCode The country code of the card issuer, or `null` if not available.
 */
data class BinLookupData(
    val brands: List<BinLookupBrand>,
    val issuingCountryCode: String?,
)

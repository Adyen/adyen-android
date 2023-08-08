package com.adyen.checkout.card

/**
 * Class that contains data about the entered card number.
 *
 * @param brand The detected brand.
 * @param isReliable Indicates whether the data is reliable. If true, the card number is checked with Adyen systems.
 * If false, the data is provided by a local detection algorithm.
 */
data class BinLookupData(
    val brand: String,
    val isReliable: Boolean,
)

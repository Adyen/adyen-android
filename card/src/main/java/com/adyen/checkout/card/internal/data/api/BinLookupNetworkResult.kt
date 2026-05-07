package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.DetectedCardType

internal data class BinLookupNetworkResult(
    val detectedCardTypes: List<DetectedCardType>,
    val issuingCountryCode: String?,
)

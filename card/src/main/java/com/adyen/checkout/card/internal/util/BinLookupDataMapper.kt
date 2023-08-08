package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.internal.data.model.DetectedCardType

internal fun DetectedCardType.toBinLookupData() = BinLookupData(
    brand = cardBrand.txVariant,
    paymentMethodVariant = paymentMethodVariant,
    isReliable = isReliable,
)

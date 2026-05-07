/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.card.BinLookupBrand
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.card.internal.ui.state.CardBrandData

internal fun DetectedCardType.toCardBrandData() = CardBrandData(
    cardBrand = cardBrand,
    enableLuhnCheck = enableLuhnCheck,
    cvcPolicy = cvcPolicy,
    expiryDatePolicy = expiryDatePolicy,
    panLength = panLength,
    paymentMethodVariant = paymentMethodVariant,
    localizedBrand = localizedBrand,
)

internal fun DetectedCardTypeList.toBinLookupData() = BinLookupData(
    issuingCountryCode = issuingCountryCode,
    brands = detectedCardTypes.map { detectedCardType ->
        BinLookupBrand(
            brand = detectedCardType.cardBrand.txVariant,
            supported = detectedCardType.isSupported,
            paymentMethodVariant = detectedCardType.paymentMethodVariant,
        )
    },
)

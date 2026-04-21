/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.state.CardBrandData

internal fun DetectedCardType.toCardBrandData() = CardBrandData(
    cardBrand = cardBrand,
    enableLuhnCheck = enableLuhnCheck,
    panLength = panLength,
    paymentMethodVariant = paymentMethodVariant,
    localizedBrand = localizedBrand,
)

internal fun CardBrandData.toBinLookupData() = BinLookupData(
    brand = cardBrand.txVariant,
    paymentMethodVariant = paymentMethodVariant,
)

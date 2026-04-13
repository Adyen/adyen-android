/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.CardBrand
import kotlinx.coroutines.flow.Flow

internal interface DetectCardTypeRepository {

    fun detectCardTypes(
        cardNumber: String,
        publicKey: String,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        paymentMethodType: String?
    ): Flow<List<DetectedCardType>>
}

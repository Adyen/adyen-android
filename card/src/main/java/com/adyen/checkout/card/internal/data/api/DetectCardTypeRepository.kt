/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface DetectCardTypeRepository {

    val detectedCardTypesFlow: Flow<List<DetectedCardType>>

    @Suppress("LongParameterList")
    fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardBrands: List<CardBrand>,
        clientKey: String,
        coroutineScope: CoroutineScope,
        type: String? = null
    )
}

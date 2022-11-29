/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 8/8/2022.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface DetectCardTypeRepository {

    val detectedCardTypesFlow: Flow<List<DetectedCardType>>

    @Suppress("LongParameterList")
    fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        supportedCardTypes: List<CardType>,
        clientKey: String,
        coroutineScope: CoroutineScope,
    )
}

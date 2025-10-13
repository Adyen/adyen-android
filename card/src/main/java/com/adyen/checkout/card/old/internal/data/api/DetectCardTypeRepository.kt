/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.old.internal.data.model.DetectedCardType
import com.adyen.checkout.core.old.CardBrand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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

/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 29/1/2021.
 */

package com.adyen.checkout.card.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.CardBrand

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class DetectedCardType(
    val cardBrand: CardBrand,
    val isReliable: Boolean,
    val enableLuhnCheck: Boolean,
    val cvcPolicy: Brand.FieldPolicy,
    val expiryDatePolicy: Brand.FieldPolicy,
    val isSupported: Boolean,
    val panLength: Int?,
    val paymentMethodVariant: String?,
    val localizedBrand: String?
)

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.CardBrand

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

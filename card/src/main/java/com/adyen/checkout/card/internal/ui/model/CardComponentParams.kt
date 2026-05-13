/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/10/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardComponentParams(
    private val commonComponentParams: CommonComponentParams,
    val showCardholderName: Boolean,
    val supportedCardBrands: List<CardBrand>,
    val showStorePaymentMethod: Boolean,
    val showSupportedCardBrandLogos: Boolean,
    val socialSecurityNumberVisibility: FieldVisibility,
    val koreanAuthenticationVisibility: FieldVisibility,
    val showPostalCode: Boolean,
    val cvcVisibility: CVCVisibility,
    val storedCVCVisibility: StoredCVCVisibility,
) : ComponentParams by commonComponentParams

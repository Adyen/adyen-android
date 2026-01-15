/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/10/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.FieldMode
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardComponentParams(
    private val commonComponentParams: CommonComponentParams,
    val isHolderNameRequired: Boolean,
    val supportedCardBrands: List<CardBrand>,
    val shopperReference: String?,
    val isStorePaymentFieldVisible: Boolean,
    val socialSecurityNumberVisibility: FieldMode,
    val kcpAuthVisibility: FieldMode,
    val cvcVisibility: CVCVisibility,
    val storedCVCVisibility: StoredCVCVisibility,
) : ComponentParams by commonComponentParams

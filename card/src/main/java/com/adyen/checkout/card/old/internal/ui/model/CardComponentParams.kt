/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.old.KCPAuthVisibility
import com.adyen.checkout.card.old.SocialSecurityNumberVisibility
import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.core.old.CardBrand
import com.adyen.checkout.ui.core.old.internal.ui.model.AddressParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val isSubmitButtonVisible: Boolean,
    val isHolderNameRequired: Boolean,
    val supportedCardBrands: List<CardBrand>,
    val shopperReference: String?,
    val isStorePaymentFieldVisible: Boolean,
    val socialSecurityNumberVisibility: SocialSecurityNumberVisibility,
    val kcpAuthVisibility: KCPAuthVisibility,
    val installmentParams: InstallmentParams?,
    val addressParams: AddressParams,
    val cvcVisibility: CVCVisibility,
    val storedCVCVisibility: StoredCVCVisibility
) : ComponentParams by commonComponentParams, ButtonParams

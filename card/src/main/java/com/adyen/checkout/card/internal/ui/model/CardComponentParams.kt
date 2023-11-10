/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsParams: AnalyticsParams,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount?,
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
) : ComponentParams, ButtonParams

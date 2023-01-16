/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.base.ButtonParams
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import java.util.Locale
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CardComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount,
    override val isSubmitButtonVisible: Boolean,
    val isHolderNameRequired: Boolean,
    val supportedCardTypes: List<CardType>,
    val shopperReference: String?,
    val isStorePaymentFieldVisible: Boolean,
    val isHideCvc: Boolean,
    val isHideCvcStoredCard: Boolean,
    val socialSecurityNumberVisibility: SocialSecurityNumberVisibility,
    val kcpAuthVisibility: KCPAuthVisibility,
    val installmentConfiguration: InstallmentConfiguration?,
    val addressParams: AddressParams,
) : ComponentParams, ButtonParams

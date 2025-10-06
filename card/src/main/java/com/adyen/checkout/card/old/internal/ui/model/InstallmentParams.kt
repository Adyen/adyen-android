/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.old.CardBrand
import java.util.Locale

/**
 * Component params class for Installments in Card Component. This class can be used
 * to define installment options for all cards or specific [CardBrand]. [defaultOptions]
 * and [cardBasedOptions] can be combined together. In that case [InstallmentOptionParams] from
 * [cardBasedOptions] will override the option defined in [defaultOptions].
 *
 * Note: [cardBasedOptions] should contain only one [InstallmentOptionParams.CardBasedInstallmentOptions]
 * instance for a [CardBrand].
 *
 * @param defaultOptions Installment Options to be used for all card types.
 * @param cardBasedOptions Installment Options to be used for specific card types.
 * @param amount Amount of the transaction.
 * @param shopperLocale The [Locale] of the shopper.
 * @param showInstallmentAmount A flag to show the installment amount.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class InstallmentParams(
    val defaultOptions: InstallmentOptionParams.DefaultInstallmentOptions? = null,
    val cardBasedOptions: List<InstallmentOptionParams.CardBasedInstallmentOptions> = emptyList(),
    val amount: Amount? = null,
    val shopperLocale: Locale,
    val showInstallmentAmount: Boolean = false
)

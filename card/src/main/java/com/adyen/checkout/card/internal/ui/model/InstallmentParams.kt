/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.data.model.Amount

/**
 * Internal installment params for the Card Component. Holds resolved installment options derived
 * from either merchant [com.adyen.checkout.card.InstallmentConfiguration] or session configuration.
 *
 * @param defaultOptions Options applied to all card brands not present in [cardBasedOptions].
 * @param cardBasedOptions Brand-specific options. Overrides [defaultOptions] for matching brands.
 * @param amount Amount of the transaction.
 * @param showInstallmentAmount Whether to show the per-installment amount in the installment dropdown.
 */
internal data class InstallmentParams(
    val defaultOptions: InstallmentOptionParams? = null,
    val cardBasedOptions: Map<CardBrand, InstallmentOptionParams> = emptyMap(),
    val amount: Amount? = null,
    val showInstallmentAmount: Boolean = false,
)

/**
 * Internal installment option params for a specific brand or as a default.
 *
 * @param values List of available installment counts.
 * @param plans The plan types available.
 * @param preselectedValue The installment count pre-selected in the UI.
 */
internal data class InstallmentOptionParams(
    val values: List<Int>,
    val plans: List<InstallmentPlan>,
    val preselectedValue: Int? = null,
)

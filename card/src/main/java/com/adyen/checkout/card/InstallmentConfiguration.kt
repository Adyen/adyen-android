/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 4/6/2026.
 */

package com.adyen.checkout.card

import android.os.Parcelable
import com.adyen.checkout.core.common.CardBrand
import kotlinx.parcelize.Parcelize

/**
 * Installment configuration for the Card Component. Defines which installment plans are available
 * to the shopper. [defaultOptions] and [cardBasedOptions] can be combined. In that case [InstallmentOptions]
 * from [cardBasedOptions] will override the option defined in [defaultOptions].
 *
 * @param defaultOptions Options applied to all card brands.
 * @param cardBasedOptions Brand-specific options. Overrides [defaultOptions] for matching brands.
 * @param showInstallmentAmount Whether to show the per-installment amount in the installment dropdown.
 */
@Parcelize
data class InstallmentConfiguration(
    val defaultOptions: InstallmentOptions? = null,
    val cardBasedOptions: Map<CardBrand, InstallmentOptions> = emptyMap(),
    val showInstallmentAmount: Boolean = false,
) : Parcelable

/**
 * Defines the available installment options details.
 *
 * Note: All values in [values] must be greater than 1.
 *
 * @param values List of available installment counts (e.g. [2, 3, 6]).
 * @param plans The plan types to offer. Defaults to [Plan.REGULAR] only.
 * @param preselectedValue The installment count pre-selected in the UI.
 */
@Parcelize
data class InstallmentOptions(
    val values: List<Int>,
    val plans: List<Plan> = listOf(Plan.REGULAR),
    val preselectedValue: Int? = null,
) : Parcelable {

    /** An installment plan type. */
    enum class Plan { REGULAR, REVOLVING }

    /**
     * @param maxInstallments Maximum number of installments
     * @param plans see [InstallmentOptions.plans].
     * @param preselectedValue see [InstallmentOptions.preselectedValue].
     *
     * Creates an [InstallmentOptions] instance with values in range [2, maxInstallments]
     */
    constructor(
        maxInstallments: Int,
        plans: List<Plan> = listOf(Plan.REGULAR),
        preselectedValue: Int? = null,
    ) : this((2..maxInstallments).toList(), plans, preselectedValue)
}

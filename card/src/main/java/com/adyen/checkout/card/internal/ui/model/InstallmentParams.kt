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

internal data class InstallmentParams(
    val defaultOptions: InstallmentOptionsParams? = null,
    val cardBasedOptions: Map<CardBrand, InstallmentOptionsParams> = emptyMap(),
    val amount: Amount? = null,
    val showInstallmentAmount: Boolean = false,
)

internal data class InstallmentOptionsParams(
    val values: List<Int>,
    val plans: List<InstallmentPlan>,
    val preselectedValue: Int? = null,
)

internal fun InstallmentParams.mapToInstallmentModels(
    cardBrand: CardBrand? = null,
): List<InstallmentModel> {
    val hasOptionsForBrand = cardBrand != null &&
        cardBasedOptions.containsKey(cardBrand)

    val availableInstallmentOptions = when {
        hasOptionsForBrand -> cardBasedOptions[cardBrand]
        defaultOptions != null -> defaultOptions
        else -> null
    }

    return availableInstallmentOptions
        ?.mapToInstallmentModels(amount, showInstallmentAmount)
        ?: emptyList()
}

private fun InstallmentOptionsParams.mapToInstallmentModels(
    amount: Amount?,
    showInstallmentAmount: Boolean,
): List<InstallmentModel> {
    return buildList {
        add(InstallmentModel.OneTime)

        if (plans.contains(InstallmentPlan.REVOLVING)) {
            add(InstallmentModel.Revolving)
        }

        values.mapTo(this) { numberOfInstallments ->
            val amountPerInstallment = when {
                numberOfInstallments <= 0 -> null
                else -> amount?.let {
                    amount.copy(value = amount.value / numberOfInstallments)
                }
            }
            InstallmentModel.Regular(
                numberOfInstallments = numberOfInstallments,
                amountPerInstallment = amountPerInstallment,
                showAmount = showInstallmentAmount,
            )
        }
    }
}

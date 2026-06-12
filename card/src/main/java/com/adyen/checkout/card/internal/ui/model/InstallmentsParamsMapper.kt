/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentOptionsParams

internal fun SessionInstallmentConfiguration.mapToInstallmentParams(): InstallmentParams {
    var defaultOptions: InstallmentOptionsParams? = null
    val cardBasedOptions = mutableMapOf<CardBrand, InstallmentOptionsParams>()

    installmentOptions?.forEach { (key, value) ->
        val params = value?.toParams()
        if (key == DEFAULT_INSTALLMENT_OPTION) {
            defaultOptions = params
        } else {
            params?.let { cardBasedOptions[CardBrand(key)] = it }
        }
    }

    return InstallmentParams(
        defaultOptions = defaultOptions,
        cardBasedOptions = cardBasedOptions,
        showInstallmentAmount = showInstallmentAmount ?: false,
    )
}

internal fun InstallmentConfiguration.mapToInstallmentParams(): InstallmentParams {
    return InstallmentParams(
        defaultOptions = defaultOptions?.toParams(),
        cardBasedOptions = cardBasedOptions.mapValues { (_, options) ->
            options.toParams()
        },
        showInstallmentAmount = showInstallmentAmount,
    )
}

private fun SessionInstallmentOptionsParams.toParams() = InstallmentOptionsParams(
    values = values ?: emptyList(),
    plans = plans?.mapNotNull { it.toInstallmentPlan() } ?: listOf(InstallmentPlan.REGULAR),
    preselectedValue = preselectedValue,
)

private fun String.toInstallmentPlan() = when (this) {
    InstallmentPlan.REGULAR.type -> InstallmentPlan.REGULAR
    InstallmentPlan.REVOLVING.type -> InstallmentPlan.REVOLVING
    else -> null
}

private fun InstallmentOptions.toParams() = InstallmentOptionsParams(
    values = values,
    plans = plans.map { it.toInstallmentPlan() },
    preselectedValue = preselectedValue,
)

private fun InstallmentOptions.Plan.toInstallmentPlan() = when (this) {
    InstallmentOptions.Plan.REGULAR -> InstallmentPlan.REGULAR
    InstallmentOptions.Plan.REVOLVING -> InstallmentPlan.REVOLVING
}

private const val DEFAULT_INSTALLMENT_OPTION = "card"

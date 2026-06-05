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
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentOptionsParams
import java.util.Locale

internal class InstallmentsParamsMapper {

    fun mapToInstallmentParams(
        installmentConfiguration: InstallmentConfiguration?,
        amount: Amount?,
        shopperLocale: Locale,
    ): InstallmentParams? {
        installmentConfiguration ?: return null
        return InstallmentParams(
            defaultOptions = installmentConfiguration.defaultOptions?.toParams(),
            cardBasedOptions = installmentConfiguration.cardBasedOptions.mapValues { (_, options) ->
                options.toParams()
            },
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = installmentConfiguration.showInstallmentAmount,
        )
    }

    fun mapToInstallmentParams(
        sessionInstallmentConfiguration: SessionInstallmentConfiguration?,
        amount: Amount?,
        shopperLocale: Locale,
    ): InstallmentParams? {
        sessionInstallmentConfiguration?.installmentOptions ?: return null

        val showInstallmentAmount = sessionInstallmentConfiguration.showInstallmentAmount ?: false
        var defaultOptions: InstallmentOptionParams? = null
        val cardBasedOptions = mutableMapOf<CardBrand, InstallmentOptionParams>()

        sessionInstallmentConfiguration.installmentOptions?.forEach { (key, value) ->
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
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = showInstallmentAmount,
        )
    }

    private fun InstallmentOptions.toParams() = InstallmentOptionParams(
        values = values,
        plans = plans.map { it.toInstallmentOption() },
        preselectedValue = preselectedValue,
    )

    private fun SessionInstallmentOptionsParams.toParams() = InstallmentOptionParams(
        values = values ?: emptyList(),
        plans = plans?.mapNotNull { it.toInstallmentOption() } ?: listOf(InstallmentOption.REGULAR),
        preselectedValue = preselectedValue,
    )

    private fun InstallmentOptions.Plan.toInstallmentOption() = when (this) {
        InstallmentOptions.Plan.REGULAR -> InstallmentOption.REGULAR
        InstallmentOptions.Plan.REVOLVING -> InstallmentOption.REVOLVING
    }

    private fun String.toInstallmentOption() = when (this) {
        InstallmentOption.REGULAR.type -> InstallmentOption.REGULAR
        InstallmentOption.REVOLVING.type -> InstallmentOption.REVOLVING
        else -> null
    }

    companion object {
        private const val DEFAULT_INSTALLMENT_OPTION = "card"
    }
}

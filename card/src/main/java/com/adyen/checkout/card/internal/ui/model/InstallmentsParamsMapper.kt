/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 22/2/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams

internal class InstallmentsParamsMapper {

    internal fun mapToInstallmentParams(
        sessionInstallmentOptions: Map<String, SessionInstallmentOptionsParams?>
    ): InstallmentParams {
        var defaultOptions: InstallmentOptionParams.DefaultInstallmentOptions? = null
        val cardBasedOptionsList = mutableListOf<InstallmentOptionParams.CardBasedInstallmentOptions>()
        sessionInstallmentOptions.forEach { (key, value) ->
            if (key == DEFAULT_INSTALLMENT_OPTION) {
                defaultOptions = value.mapToDefaultInstallmentOptions()
            } else {
                cardBasedOptionsList.add(value.mapToCardBasedInstallmentOptions(key))
            }
        }
        return InstallmentParams(defaultOptions, cardBasedOptionsList)
    }

    internal fun mapToInstallmentParams(
        installmentConfiguration: InstallmentConfiguration
    ): InstallmentParams {
        return InstallmentParams(
            defaultOptions = installmentConfiguration.defaultOptions?.mapToDefaultInstallmentOptionsParam(),
            cardBasedOptions = installmentConfiguration.cardBasedOptions.map { option ->
                option.mapToCardBasedInstallmentOptionsParams()
            }
        )
    }

    private fun InstallmentOptions.DefaultInstallmentOptions?.mapToDefaultInstallmentOptionsParam() =
        InstallmentOptionParams.DefaultInstallmentOptions(
            this?.values ?: emptyList(),
            this?.includeRevolving ?: false
        )

    private fun InstallmentOptions.CardBasedInstallmentOptions.mapToCardBasedInstallmentOptionsParams() =
        InstallmentOptionParams.CardBasedInstallmentOptions(values, includeRevolving, cardBrand)

    private fun SessionInstallmentOptionsParams?.mapToDefaultInstallmentOptions() =
        InstallmentOptionParams.DefaultInstallmentOptions(
            values = this?.values ?: emptyList(),
            includeRevolving = this?.plans?.contains(InstallmentOption.REVOLVING.type) ?: false
        )

    private fun SessionInstallmentOptionsParams?.mapToCardBasedInstallmentOptions(txVariant: String) =
        InstallmentOptionParams.CardBasedInstallmentOptions(
            values = this?.values ?: emptyList(),
            includeRevolving = this?.plans?.contains(InstallmentOption.REVOLVING.type) ?: false,
            cardBrand = CardBrand(txVariant)
        )

    companion object {
        private const val DEFAULT_INSTALLMENT_OPTION = "card"
    }
}

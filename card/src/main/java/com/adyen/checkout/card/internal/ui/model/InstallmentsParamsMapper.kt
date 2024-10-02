/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 22/2/2023.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams
import com.adyen.checkout.core.CardBrand
import java.util.Locale

internal class InstallmentsParamsMapper {

    internal fun mapToInstallmentParams(
        installmentConfiguration: SessionInstallmentConfiguration?,
        amount: Amount?,
        shopperLocale: Locale
    ): InstallmentParams? {
        installmentConfiguration?.installmentOptions ?: return null

        val showInstallmentAmount = installmentConfiguration.showInstallmentAmount ?: false
        var defaultOptions: InstallmentOptionParams.DefaultInstallmentOptions? = null
        val cardBasedOptionsList = mutableListOf<InstallmentOptionParams.CardBasedInstallmentOptions>()
        installmentConfiguration.installmentOptions?.forEach { (key, value) ->
            if (key == DEFAULT_INSTALLMENT_OPTION) {
                defaultOptions = value.mapToDefaultInstallmentOptions()
            } else {
                cardBasedOptionsList.add(value.mapToCardBasedInstallmentOptions(key))
            }
        }

        return InstallmentParams(
            defaultOptions = defaultOptions,
            cardBasedOptions = cardBasedOptionsList,
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = showInstallmentAmount,
        )
    }

    internal fun mapToInstallmentParams(
        installmentConfiguration: InstallmentConfiguration?,
        amount: Amount?,
        shopperLocale: Locale
    ): InstallmentParams? {
        installmentConfiguration ?: return null
        return InstallmentParams(
            defaultOptions = installmentConfiguration.defaultOptions?.mapToDefaultInstallmentOptionsParam(),
            cardBasedOptions = installmentConfiguration.cardBasedOptions.map { option ->
                option.mapToCardBasedInstallmentOptionsParams()
            },
            amount = amount,
            shopperLocale = shopperLocale,
            showInstallmentAmount = installmentConfiguration.showInstallmentAmount,
        )
    }

    private fun InstallmentOptions.DefaultInstallmentOptions?.mapToDefaultInstallmentOptionsParam() =
        InstallmentOptionParams.DefaultInstallmentOptions(
            this?.values ?: emptyList(),
            this?.includeRevolving ?: false,
        )

    private fun InstallmentOptions.CardBasedInstallmentOptions.mapToCardBasedInstallmentOptionsParams() =
        InstallmentOptionParams.CardBasedInstallmentOptions(values, includeRevolving, cardBrand)

    private fun SessionInstallmentOptionsParams?.mapToDefaultInstallmentOptions() =
        InstallmentOptionParams.DefaultInstallmentOptions(
            values = this?.values ?: emptyList(),
            includeRevolving = this?.plans?.contains(InstallmentOption.REVOLVING.type) ?: false,
        )

    private fun SessionInstallmentOptionsParams?.mapToCardBasedInstallmentOptions(txVariant: String) =
        InstallmentOptionParams.CardBasedInstallmentOptions(
            values = this?.values ?: emptyList(),
            includeRevolving = this?.plans?.contains(InstallmentOption.REVOLVING.type) ?: false,
            cardBrand = CardBrand(txVariant),
        )

    companion object {
        private const val DEFAULT_INSTALLMENT_OPTION = "card"
    }
}

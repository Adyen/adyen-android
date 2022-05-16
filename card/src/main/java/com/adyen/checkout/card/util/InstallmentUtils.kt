/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/10/2021.
 */

package com.adyen.checkout.card.util

import android.content.Context
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentModel
import com.adyen.checkout.card.InstallmentOption
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.R
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.model.payments.request.Installments

private const val REVOLVING_INSTALLMENT_VALUE = 1

internal object InstallmentUtils {

    /**
     * Create a list of installment options from [InstallmentConfiguration].
     */
    fun makeInstallmentOptions(
        configuration: InstallmentConfiguration?,
        cardType: CardType?,
        isCardTypeReliable: Boolean
    ): List<InstallmentModel> {
        val hasCardBasedInstallmentOptions = configuration?.cardBasedOptions != null
        val hasDefaultInstallmentOptions = configuration?.defaultOptions != null
        val hasOptionsForCardType = hasCardBasedInstallmentOptions &&
            isCardTypeReliable &&
            (configuration?.cardBasedOptions?.any { it.cardType == cardType } ?: false)

        return when {
            hasOptionsForCardType -> {
                makeInstallmentModelList(configuration?.cardBasedOptions?.firstOrNull { it.cardType == cardType })
            }
            hasDefaultInstallmentOptions -> {
                makeInstallmentModelList(configuration?.defaultOptions)
            }
            else -> {
                emptyList()
            }
        }
    }

    private fun makeInstallmentModelList(installmentOptions: InstallmentOptions?): List<InstallmentModel> {
        if (installmentOptions == null) return emptyList()
        val installmentOptionsList = mutableListOf<InstallmentModel>()
        val oneTimeOption = InstallmentModel(
            textResId = R.string.checkout_card_installments_option_one_time,
            value = null,
            option = InstallmentOption.ONE_TIME
        )
        installmentOptionsList.add(oneTimeOption)

        if (installmentOptions.includeRevolving) {
            val revolvingOption = InstallmentModel(
                textResId = R.string.checkout_card_installments_option_revolving,
                value = REVOLVING_INSTALLMENT_VALUE,
                option = InstallmentOption.REVOLVING
            )
            installmentOptionsList.add(revolvingOption)
        }

        val regularOptions = installmentOptions.values.map {
            InstallmentModel(
                textResId = R.string.checkout_card_installments_option_regular,
                value = it,
                option = InstallmentOption.REGULAR
            )
        }
        installmentOptionsList.addAll(regularOptions)
        return installmentOptionsList
    }

    /**
     * Get the text to be shown for different types of [InstallmentOption].
     */
    fun getTextForInstallmentOption(context: Context, installmentModel: InstallmentModel?): String {
        return when (installmentModel?.option) {
            InstallmentOption.REGULAR -> context.getString(installmentModel.textResId, installmentModel.value)
            InstallmentOption.REVOLVING, InstallmentOption.ONE_TIME -> context.getString(installmentModel.textResId)
            else -> ""
        }
    }

    /**
     * Populate the [Installments] model object from [InstallmentModel].
     */
    fun makeInstallmentModelObject(installmentModel: InstallmentModel?): Installments? {
        return when (installmentModel?.option) {
            InstallmentOption.REGULAR, InstallmentOption.REVOLVING -> {
                Installments(installmentModel.option.type, installmentModel.value)
            }
            else -> null
        }
    }

    /**
     * Check whether the card based options contain only one option defined per card type.
     */
    fun isCardBasedOptionsValid(
        cardBasedInstallmentOptions: List<InstallmentOptions.CardBasedInstallmentOptions>?
    ): Boolean {
        val hasMultipleOptionsForSameCard = cardBasedInstallmentOptions
            ?.groupBy { it.cardType }
            ?.values
            ?.any { it.size > 1 } ?: false
        return !hasMultipleOptionsForSameCard
    }

    /**
     * Check whether [InstallmentOptions.values] in installment options defined in
     * [InstallmentConfiguration] are valid (i.e. all the values are greater than 1).
     */
    fun areInstallmentValuesValid(installmentConfiguration: InstallmentConfiguration): Boolean {
        val installmentOptions = mutableListOf<InstallmentOptions?>()
        installmentOptions.add(installmentConfiguration.defaultOptions)
        installmentOptions.addAll(installmentConfiguration.cardBasedOptions)
        val hasInvalidValue = installmentOptions.filterNotNull().any { it.values.any { it <= 1 } }
        return !hasInvalidValue
    }
}

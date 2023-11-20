/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/10/2021.
 */

package com.adyen.checkout.card.internal.util

import android.content.Context
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.model.InstallmentOption
import com.adyen.checkout.card.internal.ui.model.InstallmentOptionParams
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.Installments
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import java.text.NumberFormat
import java.util.Locale

private const val REVOLVING_INSTALLMENT_VALUE = 1

internal object InstallmentUtils {

    /**
     * Create a list of installment options from [InstallmentParams].
     */
    fun makeInstallmentOptions(
        installmentParams: InstallmentParams?,
        cardBrand: CardBrand?,
        isCardTypeReliable: Boolean
    ): List<InstallmentModel> = installmentParams?.let { params ->
        val hasCardBasedInstallmentOptions = params.cardBasedOptions.isNotEmpty()
        val hasDefaultInstallmentOptions = params.defaultOptions != null
        val hasOptionsForCardType = hasCardBasedInstallmentOptions &&
            isCardTypeReliable &&
            params.cardBasedOptions.any { it.cardBrand == cardBrand }

        return when {
            hasOptionsForCardType -> {
                makeInstallmentModelList(
                    installmentOptions = params.cardBasedOptions.firstOrNull { it.cardBrand == cardBrand },
                    amount = params.amount,
                    shopperLocale = params.shopperLocale,
                    showAmount = params.showInstallmentAmount
                )
            }

            hasDefaultInstallmentOptions -> {
                makeInstallmentModelList(
                    installmentOptions = params.defaultOptions,
                    amount = params.amount,
                    shopperLocale = params.shopperLocale,
                    showAmount = params.showInstallmentAmount
                )
            }

            else -> {
                emptyList()
            }
        }
    } ?: emptyList()

    private fun makeInstallmentModelList(
        installmentOptions: InstallmentOptionParams?,
        amount: Amount?,
        shopperLocale: Locale,
        showAmount: Boolean
    ): List<InstallmentModel> {
        if (installmentOptions == null) return emptyList()
        val installmentOptionsList = mutableListOf<InstallmentModel>()
        val oneTimeOption = InstallmentModel(
            textResId = R.string.checkout_card_installments_option_one_time,
            numberOfInstallments = null,
            option = InstallmentOption.ONE_TIME,
            amount = amount,
            shopperLocale = shopperLocale,
            showAmount = showAmount
        )
        installmentOptionsList.add(oneTimeOption)

        if (installmentOptions.includeRevolving) {
            val revolvingOption = InstallmentModel(
                textResId = R.string.checkout_card_installments_option_revolving,
                numberOfInstallments = REVOLVING_INSTALLMENT_VALUE,
                option = InstallmentOption.REVOLVING,
                amount = amount,
                shopperLocale = shopperLocale,
                showAmount = showAmount
            )
            installmentOptionsList.add(revolvingOption)
        }

        val regularOptionTextResId = if (showAmount && amount != null) {
            R.string.checkout_card_installments_option_regular_with_price
        } else {
            R.string.checkout_card_installments_option_regular
        }
        val regularOptions = installmentOptions.values.map { numberOfInstallments ->
            InstallmentModel(
                textResId = regularOptionTextResId,
                numberOfInstallments = numberOfInstallments,
                option = InstallmentOption.REGULAR,
                amount = amount,
                shopperLocale = shopperLocale,
                showAmount = showAmount
            )
        }
        installmentOptionsList.addAll(regularOptions)
        return installmentOptionsList
    }

    /**
     * Get the text to be shown for different types of [InstallmentOption].
     */
    fun getTextForInstallmentOption(context: Context, installmentModel: InstallmentModel?): String =
        with(installmentModel) {
            return when (this?.option) {
                InstallmentOption.REGULAR -> {
                    val numberOfInstallments = numberOfInstallments ?: 1
                    val installmentAmount = amount?.copy(value = amount.value / numberOfInstallments)
                    val formattedNumberOfInstallments =
                        NumberFormat.getInstance(shopperLocale).format(numberOfInstallments)
                    if (installmentAmount != null) {
                        val formattedAmount = CurrencyUtils.formatAmount(installmentAmount, shopperLocale)
                        context.getString(textResId, formattedNumberOfInstallments, formattedAmount)
                    } else {
                        context.getString(textResId, formattedNumberOfInstallments)
                    }
                }

                InstallmentOption.REVOLVING, InstallmentOption.ONE_TIME -> context.getString(textResId)
                else -> ""
            }
        }

    /**
     * Populate the [Installments] model object from [InstallmentModel].
     */
    fun makeInstallmentModelObject(installmentModel: InstallmentModel?): Installments? {
        return when (installmentModel?.option) {
            InstallmentOption.REGULAR, InstallmentOption.REVOLVING -> {
                Installments(installmentModel.option.type, installmentModel.numberOfInstallments)
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
            ?.groupBy { it.cardBrand }
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

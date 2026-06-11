/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.InstallmentOptionParams
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentPlan
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.data.model.Amount
import java.util.Locale

internal object InstallmentUtils {

    fun makeInstallmentOptions(
        installmentParams: InstallmentParams?,
        cardBrand: CardBrand? = null,
        isCardTypeReliable: Boolean = false,
    ): List<InstallmentModel> {
        installmentParams ?: return emptyList()

        val hasOptionsForBrand = cardBrand != null &&
            isCardTypeReliable &&
            installmentParams.cardBasedOptions.containsKey(cardBrand)

        val options = when {
            hasOptionsForBrand -> installmentParams.cardBasedOptions[cardBrand]
            !installmentParams.defaultOptions?.values.isNullOrEmpty() -> installmentParams.defaultOptions
            else -> return emptyList()
        }

        return makeInstallmentModelList(
            installmentOptions = options,
            amount = installmentParams.amount,
            shopperLocale = installmentParams.shopperLocale,
            showAmount = installmentParams.showInstallmentAmount,
        )
    }

    private fun makeInstallmentModelList(
        installmentOptions: InstallmentOptionParams?,
        amount: Amount?,
        shopperLocale: Locale,
        showAmount: Boolean,
    ): List<InstallmentModel> {
        installmentOptions ?: return emptyList()

        val result = mutableListOf<InstallmentModel>()

        result.add(
            InstallmentModel(
                numberOfInstallments = null,
                plan = InstallmentPlan.ONE_TIME,
                amountPerInstallment = null,
                showAmount = showAmount,
                shopperLocale = shopperLocale,
            ),
        )

        if (installmentOptions.plans.contains(InstallmentPlan.REVOLVING)) {
            result.add(
                InstallmentModel(
                    numberOfInstallments = REVOLVING_INSTALLMENT_VALUE,
                    plan = InstallmentPlan.REVOLVING,
                    amountPerInstallment = null,
                    showAmount = showAmount,
                    shopperLocale = shopperLocale,
                ),
            )
        }

        installmentOptions.values.mapTo(result) { count ->
            InstallmentModel(
                numberOfInstallments = count,
                plan = InstallmentPlan.REGULAR,
                amountPerInstallment = getInstallmentPerAmount(amount, count),
                showAmount = showAmount,
                shopperLocale = shopperLocale,
            )
        }

        return result
    }

    private fun getInstallmentPerAmount(totalAmount: Amount?, numberOfInstallments: Int): Amount? {
        if (totalAmount == null || numberOfInstallments <= 0) {
            return null
        }
        return totalAmount.copy(value = totalAmount.value / numberOfInstallments)
    }

    private const val REVOLVING_INSTALLMENT_VALUE = 1
}

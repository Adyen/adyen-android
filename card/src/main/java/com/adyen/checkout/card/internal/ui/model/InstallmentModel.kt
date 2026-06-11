/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.compose.runtime.Composable
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.format
import java.util.Locale

internal data class InstallmentModel(
    val plan: InstallmentPlan,
    val numberOfInstallments: Int?,
    val amount: Amount?,
    val showAmount: Boolean,
    val shopperLocale: Locale,
)

@Composable
internal fun InstallmentModel.toDisplayText(): String {
    return when (plan) {
        InstallmentPlan.ONE_TIME ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_ONE_TIME)

        InstallmentPlan.REVOLVING ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REVOLVING)

        InstallmentPlan.REGULAR -> {
            val count = numberOfInstallments ?: 1

            val amountValue = amount?.value ?: 0L
            val amountPerInstallment: Amount? = when {
                amountValue > 0 -> amount?.copy(value = amountValue.div(count))
                else -> null
            }

            if (showAmount && amountPerInstallment != null) {
                resolveString(
                    CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR_WITH_PRICE,
                    count,
                    amountPerInstallment.format(shopperLocale),
                )
            } else {
                resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR, count)
            }
        }
    }
}

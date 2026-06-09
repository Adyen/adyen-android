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
    val numberOfInstallments: Int?,
    val option: InstallmentOption,
    val amount: Amount?,
    val shopperLocale: Locale,
    val showAmount: Boolean,
)

@Composable
internal fun InstallmentModel.toDisplayText(): String {
    return when (option) {
        InstallmentOption.ONE_TIME ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_ONE_TIME)

        InstallmentOption.REVOLVING ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REVOLVING)

        InstallmentOption.REGULAR -> {
            val count = numberOfInstallments ?: 1
            if (showAmount && amount != null) {
                resolveString(
                    CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR_WITH_PRICE,
                    count,
                    amount.format(shopperLocale),
                )
            } else {
                resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR, count)
            }
        }
    }
}

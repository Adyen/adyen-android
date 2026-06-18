/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 16/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.compose.runtime.Composable
import com.adyen.checkout.core.common.internal.helper.LocalLocale
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.data.model.format

@Composable
internal fun InstallmentModel.toDisplayText(): String {
    return when (this) {
        InstallmentModel.OneTime ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_ONE_TIME)

        InstallmentModel.Revolving ->
            resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REVOLVING)

        is InstallmentModel.Regular -> {
            if (showAmount && amountPerInstallment != null) {
                resolveString(
                    CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR_WITH_PRICE,
                    numberOfInstallments,
                    amountPerInstallment.format(LocalLocale.current),
                )
            } else {
                resolveString(CheckoutLocalizationKey.CARD_INSTALLMENTS_REGULAR, numberOfInstallments)
            }
        }
    }
}

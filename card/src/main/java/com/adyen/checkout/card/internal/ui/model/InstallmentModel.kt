/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.components.data.model.Amount

internal sealed interface InstallmentModel {
    data object OneTime : InstallmentModel
    data object Revolving : InstallmentModel
    data class Regular(
        val numberOfInstallments: Int,
        val amountPerInstallment: Amount?,
        val showAmount: Boolean,
    ) : InstallmentModel
}

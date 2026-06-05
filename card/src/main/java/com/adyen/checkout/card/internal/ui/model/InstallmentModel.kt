/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/6/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.components.data.model.Amount
import java.util.Locale

internal data class InstallmentModel(
    val numberOfInstallments: Int?,
    val option: InstallmentOption,
    val amount: Amount?,
    val shopperLocale: Locale,
    val showAmount: Boolean,
)

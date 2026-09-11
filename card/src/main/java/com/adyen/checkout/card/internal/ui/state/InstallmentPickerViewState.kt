/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 16/6/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.card.internal.ui.model.InstallmentModel

/**
 * What the installments screen shows.
 *
 * @param installmentOptions The installments the shopper can choose from, in the order they are shown.
 * @param selectedInstallment The current choice, or null if the shopper has not made one.
 */
@Immutable
internal data class InstallmentPickerViewState(
    val installmentOptions: List<InstallmentModel>,
    val selectedInstallment: InstallmentModel?,
)

internal fun InstallmentState.toPickerViewState(): InstallmentPickerViewState? {
    if (installmentOptions.isEmpty()) return null
    return InstallmentPickerViewState(
        installmentOptions = installmentOptions,
        selectedInstallment = selectedInstallment,
    )
}

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

@Immutable
internal data class InstallmentViewState(
    val installmentOptions: List<InstallmentModel>,
    val selectedInstallment: InstallmentModel?,
)

internal fun InstallmentState.toViewState(): InstallmentViewState? {
    if (installmentOptions.isEmpty()) return null
    return InstallmentViewState(
        installmentOptions = installmentOptions,
        selectedInstallment = selectedInstallment,
    )
}

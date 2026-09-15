/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/6/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.InstallmentViewState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun CardSecondaryContent(
    modifier: Modifier,
    identifier: String,
    viewState: StateFlow<CardViewState>,
    onInstallmentClick: (InstallmentModel) -> Unit,
) {
    val viewState by viewState.collectAsStateWithLifecycle()

    when (identifier) {
        CardSecondaryContentEntry.INSTALLMENTS -> {
            Installments(
                modifier = modifier,
                installmentViewState = viewState.installmentViewState,
                onItemClick = onInstallmentClick,
            )
        }
    }
}

@Composable
private fun Installments(
    modifier: Modifier,
    installmentViewState: InstallmentViewState?,
    onItemClick: (InstallmentModel) -> Unit,
) {
    if (installmentViewState == null) return
    InstallmentPicker(
        installmentOptions = installmentViewState.installmentOptions,
        selectedInstallment = installmentViewState.selectedInstallment,
        onItemClick = onItemClick,
        modifier = modifier,
    )
}

internal object CardSecondaryContentEntry {
    const val INSTALLMENTS: String = "INSTALLMENTS"
}

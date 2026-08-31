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
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.InstallmentPickerViewState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun CardSecondaryContent(
    modifier: Modifier,
    identifier: String,
    viewState: StateFlow<CardViewState>,
    onIntent: (CardIntent) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val viewState by viewState.collectAsStateWithLifecycle()

    when (identifier) {
        CardSecondaryContentEntry.INSTALLMENTS -> {
            Installments(
                modifier = modifier,
                installmentPickerViewState = viewState.installmentPickerViewState,
                onIntent = onIntent,
                onDismissRequest = onDismissRequest,
            )
        }
    }
}

@Composable
private fun Installments(
    modifier: Modifier,
    installmentPickerViewState: InstallmentPickerViewState?,
    onIntent: (CardIntent) -> Unit,
    onDismissRequest: () -> Unit,
) {
    // Unreachable: with no installments the row that opens this screen is not on the card form either.
    if (installmentPickerViewState == null) return
    InstallmentPicker(
        installmentOptions = installmentPickerViewState.installmentOptions,
        selectedInstallment = installmentPickerViewState.selectedInstallment,
        onItemClick = { installment ->
            onIntent(CardIntent.UpdateInstallment(installment))
            onDismissRequest()
        },
        modifier = modifier,
    )
}

internal object CardSecondaryContentEntry {
    const val INSTALLMENTS: String = "INSTALLMENTS"
}

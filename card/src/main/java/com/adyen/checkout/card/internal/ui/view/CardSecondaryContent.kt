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
import com.adyen.checkout.card.internal.ui.CardComponent
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.InstallmentViewState
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
        CardComponent.INSTALLMENTS_IDENTIFIER -> {
            Installments(
                modifier = modifier,
                installmentViewState = viewState.installmentViewState,
                onIntent = onIntent,
                onDismissRequest = onDismissRequest,
            )
        }
    }
}

@Composable
internal fun Installments(
    modifier: Modifier,
    installmentViewState: InstallmentViewState?,
    onIntent: (CardIntent) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (installmentViewState == null) return
    InstallmentPicker(
        installmentOptions = installmentViewState.installmentOptions,
        selectedInstallment = installmentViewState.selectedInstallment,
        onItemClick = { installment ->
            onIntent(CardIntent.UpdateInstallment(installment))
            onDismissRequest()
        },
        modifier = modifier,
    )
}

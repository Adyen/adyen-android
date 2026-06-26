/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/1/2026.
 */

package com.adyen.checkout.blik.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.blik.internal.ui.state.StoredBlikViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.button.PayButton
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun StoredBlikContent(
    viewStateFlow: StateFlow<StoredBlikViewState>,
    onSubmitClick: () -> Unit,
    modifier: Modifier,
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()
    StoredBlikContent(
        viewState = viewState,
        onSubmitClick = onSubmitClick,
        modifier = modifier,
    )
}

@Composable
private fun StoredBlikContent(
    viewState: StoredBlikViewState,
    onSubmitClick: () -> Unit,
    modifier: Modifier,
) {
    ComponentScaffold(
        footer = {
            PayButton(
                isLoading = viewState.isLoading,
                onClick = onSubmitClick,
            )
        },
        modifier = modifier,
    ) {
        // No content needed - stored Blik just shows a Pay button
    }
}

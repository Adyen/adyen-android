/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.blik.internal.ui.state.BlikIntent
import com.adyen.checkout.blik.internal.ui.state.BlikViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.PayButton
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun BlikContent(
    viewStateFlow: StateFlow<BlikViewState>,
    onSubmitClick: () -> Unit,
    onIntent: (BlikIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()
    BlikContent(
        viewState = viewState,
        onSubmitClick = onSubmitClick,
        onIntent = onIntent,
        modifier = modifier,
    )
}

@Composable
private fun BlikContent(
    viewState: BlikViewState,
    onSubmitClick: () -> Unit,
    onIntent: (BlikIntent) -> Unit,
    modifier: Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = viewState.isLoading,
        footer = {
            PayButton(onClick = onSubmitClick, isLoading = viewState.isLoading)
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        ) {
            // Helper text
            Body(text = resolveString(CheckoutLocalizationKey.BLIK_HELPER_TEXT))

            if (viewState.blikCode != null) {
                BlikCodeField(
                    blikCodeState = viewState.blikCode,
                    onValueChange = { onIntent(BlikIntent.UpdateBlikCode(it)) },
                    onFocusChange = { onIntent(BlikIntent.UpdateBlikCodeFocus(it)) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlikContentPreview() {
    BlikContent(
        viewState = BlikViewState(
            blikCode = TextInputViewState(),
            isLoading = false,
        ),
        onIntent = {},
        onSubmitClick = {},
        modifier = Modifier,
    )
}

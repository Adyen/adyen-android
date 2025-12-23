/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.card.internal.ui.state.StoredCardIntent
import com.adyen.checkout.card.internal.ui.state.StoredCardViewState
import com.adyen.checkout.card.internal.ui.state.isAmex
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.button.PayButton
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun StoredCardComponent(
    viewState: StoredCardViewState,
    onIntent: (StoredCardIntent) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        footer = {
            PayButton(onClick = onSubmitClick, isLoading = viewState.isLoading)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.ExtraLarge),
        ) {
            StoredCardSecurityCodeField(
                securityCodeState = viewState.securityCode,
                onIntent = onIntent,
                isAmex = viewState.isAmex,
            )
        }
    }
}

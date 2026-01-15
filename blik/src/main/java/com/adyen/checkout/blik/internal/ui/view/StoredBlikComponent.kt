/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/1/2026.
 */

package com.adyen.checkout.blik.internal.ui.view

import androidx.compose.runtime.Composable
import com.adyen.checkout.blik.internal.ui.state.StoredBlikViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.button.PayButton

@Composable
internal fun StoredBlikComponent(
    viewState: StoredBlikViewState,
    onSubmitClick: () -> Unit,
) {
    ComponentScaffold(
        footer = {
            PayButton(
                isLoading = viewState.isLoading,
                onClick = onSubmitClick,
            )
        },
    ) {
        // No content needed - stored Blik just shows a Pay button
    }
}

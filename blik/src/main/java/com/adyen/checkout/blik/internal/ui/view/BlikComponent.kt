/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.view

import androidx.compose.runtime.Composable
import com.adyen.checkout.blik.internal.ui.state.BlikIntent
import com.adyen.checkout.blik.internal.ui.state.BlikViewState

@Composable
internal fun BlikComponent(
    viewState: BlikViewState,
    onSubmitClick: () -> Unit,
    onIntent: (BlikIntent) -> Unit,
) {
    // TODO: Implement UI
}
